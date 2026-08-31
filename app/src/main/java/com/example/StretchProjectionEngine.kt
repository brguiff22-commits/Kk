package com.example

import android.content.Context
import android.graphics.Rect
import android.os.IBinder
import android.os.SystemClock
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

/** Implementação nativa da Tela Esticada extraída do fluxo do APK de referência. */
internal object StretchProjectionEngine {
    private const val REQUEST_CODE = 2408
    private const val MIN_PHYSICAL_SHORT = 480
    private const val MIN_LOGICAL_LONG = 320
    private const val MAX_MULTIPLIER = 2.01

    fun isReady(context: Context): Boolean {
        return try {
            if (!Shizuku.pingBinder()) return hasRoot()
            if (Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED) true
            else { Shizuku.requestPermission(REQUEST_CODE); false }
        } catch (_: Throwable) { hasRoot() }
    }

    fun apply(context: Context, multiplier: Float): Boolean {
        return try {
            if (!isReady(context)) return false
            val physical = readPhysicalSize()
            val logicalLong = (physical.longAxis / multiplier.coerceIn(1f, 1.3f)).toInt()
            require(physical.shortAxis >= MIN_PHYSICAL_SHORT)
            require(logicalLong >= MIN_LOGICAL_LONG && logicalLong < physical.longAxis)
            require(physical.longAxis.toDouble() / logicalLong <= MAX_MULTIPLIER)
            val before = shell("wm size")
            require(!before.contains("Override size", ignoreCase = true))

            shell("wm size ${physical.shortAxis}x$logicalLong")
            require(shell("wm size").contains(
                "Override size: ${physical.shortAxis}x$logicalLong", ignoreCase = true,
            ))
            SystemClock.sleep(650)

            val projection = Projection(1, logicalLong, physical.shortAxis, physical.longAxis, physical.shortAxis)
            var verified = false
            repeat(6) {
                applyProjection(projection)
                SystemClock.sleep(140)
                if (wmOverrideIsActive(logicalLong, physical.shortAxis)) {
                    SystemClock.sleep(360)
                    if (wmOverrideIsActive(logicalLong, physical.shortAxis)) verified = true
                }
            }
            check(verified)
            toast(context, "Tela Esticada ativada: ${physical.shortAxis} x $logicalLong")
            true
        } catch (t: Throwable) {
            restore(context)
            toast(context, "Falha ao ativar Tela Esticada: ${t.message ?: "erro desconhecido"}")
            false
        }
    }

    fun restore(context: Context): Boolean {
        return try {
            val physical = readPhysicalSize()
            applyProjection(Projection(1, physical.longAxis, physical.shortAxis, physical.longAxis, physical.shortAxis))
            SystemClock.sleep(240)
            val result = command("wm size reset")
            val success = result.exitCode == 0
            toast(context, if (success) "Tela Esticada restaurada" else "Não foi possível restaurar a tela")
            success
        } catch (t: Throwable) {
            toast(context, "Erro ao restaurar Tela Esticada: ${t.message ?: "erro desconhecido"}")
            false
        }
    }

    private data class PhysicalSize(val longAxis: Int, val shortAxis: Int)
    private data class Projection(val rotation: Int, val sourceLong: Int, val sourceShort: Int, val destinationLong: Int, val destinationShort: Int)
    private data class Result(val exitCode: Int, val output: String)

    private fun readPhysicalSize(): PhysicalSize {
        val output = shell("wm size")
        val match = Regex("Physical size:\\s*(\\d+)x(\\d+)", RegexOption.IGNORE_CASE).find(output)
            ?: error("Tamanho físico não encontrado: $output")
        val a = match.groupValues[1].toInt()
        val b = match.groupValues[2].toInt()
        return if (a >= b) PhysicalSize(a, b) else PhysicalSize(b, a)
    }

    private fun wmOverrideIsActive(logicalLong: Int, logicalShort: Int): Boolean =
        shell("wm size").contains("Override size: $logicalShort\\x$logicalLong", ignoreCase = true)

    private fun shell(command: String): String {
        val result = command(command)
        check(result.exitCode == 0) { result.output.ifBlank { "comando recusado" } }
        return result.output
    }

    private fun command(command: String): Result {
        val process = try {
            Runtime.getRuntime().exec(arrayOf("su", "-c", command))
        } catch (_: Throwable) {
            val method = Shizuku::class.java.getDeclaredMethod(
                "newProcess", Array<String>::class.java, Array<String>::class.java, String::class.java,
            ).apply { isAccessible = true }
            method.invoke(null, arrayOf("sh", "-c", command), null, null)
        }
        val stdout = BufferedReader(InputStreamReader(process.javaClass.getMethod("getInputStream").invoke(process) as java.io.InputStream)).readText()
        val stderr = BufferedReader(InputStreamReader(process.javaClass.getMethod("getErrorStream").invoke(process) as java.io.InputStream)).readText()
        val exit = process.javaClass.getMethod("waitFor").invoke(process) as Int
        return Result(exit, stdout + stderr)
    }

    private fun applyProjection(projection: Projection) {
        val sm = Class.forName("android.os.ServiceManager")
        val getService = sm.getDeclaredMethod("getService", String::class.java).apply { isAccessible = true }
        val flinger = getService.invoke(null, "SurfaceFlingerAIDL") as? IBinder
            ?: error("SurfaceFlingerAIDL indisponível")
        val token = findInternalDisplayToken(flinger)
        val transactionClass = Class.forName("android.view.SurfaceControl\$Transaction")
        val transaction = transactionClass.getDeclaredConstructor().newInstance()
        val setProjection = transactionClass.getDeclaredMethod(
            "setDisplayProjection", IBinder::class.java, Int::class.javaPrimitiveType,
            Rect::class.java, Rect::class.java,
        ).apply { isAccessible = true }
        setProjection.invoke(
            transaction, token, projection.rotation,
            Rect(0, 0, projection.sourceLong, projection.sourceShort),
            Rect(0, 0, projection.destinationLong, projection.destinationShort),
        )
        transactionClass.getDeclaredMethod("apply").apply { isAccessible = true }.invoke(transaction)
    }

    private fun findInternalDisplayToken(flinger: IBinder): IBinder {
        val surfaceControl = Class.forName("android.view.SurfaceControl")
        val method = surfaceControl.declaredMethods.firstOrNull {
            it.name.contains("internalDisplayToken", ignoreCase = true) && it.parameterTypes.isEmpty()
        } ?: error("Token do display interno indisponível")
        method.isAccessible = true
        return method.invoke(null) as IBinder
    }

    private fun hasRoot(): Boolean = runCatching { Runtime.getRuntime().exec(arrayOf("which", "su")).waitFor() == 0 }.getOrDefault(false)

    private fun toast(context: Context, message: String) =
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
}
