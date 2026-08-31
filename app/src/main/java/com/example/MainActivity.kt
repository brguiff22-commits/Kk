package com.example

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MinhaIATheme
import java.util.Locale
import rikka.shizuku.Shizuku

private val Purple = Color(0xFF9B5CFF)
private val DeepPurple = Color(0xFF160D22)
private val Panel = Color(0xFF241731)
private val Muted = Color(0xFFB9AAC7)
private val Green = Color(0xFF4ADE80)

class MainActivity : ComponentActivity() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val binderListener = Shizuku.OnBinderReceivedListener {
        mainHandler.post { ShizukuBridge.requestPermissionIfNeeded() }
    }
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { _, _ ->
        mainHandler.post { recreate() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching {
            Shizuku.addBinderReceivedListenerSticky(binderListener)
            Shizuku.addRequestPermissionResultListener(permissionListener)
        }
        setContent { MinhaIATheme { CatResolutionApp() } }
        // Solicita a confirmação assim que a tela inicial estiver pronta.
        mainHandler.postDelayed({ ShizukuBridge.requestPermissionIfNeeded() }, 350L)
    }

    override fun onResume() {
        super.onResume()
        mainHandler.postDelayed({ ShizukuBridge.requestPermissionIfNeeded() }, 250L)
    }

    override fun onDestroy() {
        mainHandler.removeCallbacksAndMessages(null)
        runCatching {
            Shizuku.removeBinderReceivedListener(binderListener)
            Shizuku.removeRequestPermissionResultListener(permissionListener)
        }
        super.onDestroy()
    }
}

private enum class Tab(val label: String) {
    GAMES("Jogos"), OVERLAY("Overlay"), HISTORY("Histórico"), SETTINGS("Config")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatResolutionApp() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { context.getSharedPreferences("cat_resolution", Context.MODE_PRIVATE) }
    var tab by remember { mutableStateOf(Tab.GAMES) }
    var multiplier by remember { mutableStateOf(1.19f) }
    var projectionMode by remember { mutableStateOf("ALONGAR") }
    var darkMode by remember { mutableStateOf(preferences.getBoolean("dark_mode", true)) }
    var applied by remember { mutableStateOf(false) }
    var shizukuReady by remember { mutableStateOf(ShizukuBridge.isReady(context)) }
    LaunchedEffect(darkMode) { preferences.edit().putBoolean("dark_mode", darkMode).apply() }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCaptureDialog by remember { mutableStateOf(false) }
    var captureRunning by remember { mutableStateOf(false) }
    var captured by remember { mutableStateOf(false) }

    MaterialTheme(colorScheme = if (darkMode) androidx.compose.material3.darkColorScheme(primary = Purple, background = DeepPurple, surface = Panel) else androidx.compose.material3.lightColorScheme(primary = Color(0xFF6F32B5), background = Color(0xFFF8F4FC), surface = Color.White)) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Scaffold(
            containerColor = DeepPurple,
            topBar = { AppHeader() },
            bottomBar = {
                NavigationBar(containerColor = Color(0xFF20132C)) {
                    Tab.values().forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = {
                                Icon(
                                    imageVector = when (item) {
                                        Tab.GAMES -> Icons.Default.Gamepad
                                        Tab.OVERLAY -> Icons.Default.Layers
                                        Tab.HISTORY -> Icons.Default.History
                                        Tab.SETTINGS -> Icons.Default.Settings
                                    },
                                    contentDescription = item.label
                                )
                            },
                            label = { Text(item.label, fontSize = 11.sp) },
                            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                                selectedIconColor = Purple,
                                selectedTextColor = Purple,
                                indicatorColor = Color(0xFF38204B),
                                unselectedIconColor = Muted,
                                unselectedTextColor = Muted
                            )
                        )
                    }
                }
            }
        ) { padding ->
            when (tab) {
                Tab.GAMES -> GamesScreen(padding, multiplier, { multiplier = it }, { showAddDialog = true }, applied, { applied = ShizukuBridge.apply(context, multiplier); shizukuReady = ShizukuBridge.isReady(context) }, { applied = false; ShizukuBridge.restore(context) }, shizukuReady)
                Tab.OVERLAY -> OverlayScreen(padding, multiplier, { multiplier = it })
                Tab.HISTORY -> HistoryScreen(padding)
                Tab.SETTINGS -> SettingsScreen(
                    padding = padding,
                    projectionMode = projectionMode,
                    onProjectionModeChange = { projectionMode = it },
                    darkMode = darkMode,
                    onDarkModeChange = { darkMode = it },
                    onCapture = { showCaptureDialog = true }
                )
            }
        }
    }
    }

    if (showAddDialog) AddAppDialog(onDismiss = { showAddDialog = false })
    if (showCaptureDialog) CaptureDialog(
        running = captureRunning,
        captured = captured,
        onDismiss = { showCaptureDialog = false },
        onStart = { captureRunning = true },
        onStop = { captureRunning = false; captured = true },
        onReset = { captured = false; captureRunning = false }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppHeader() {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).clip(CircleShape).background(Purple),
                    contentAlignment = Alignment.Center
                ) { Text("🐱", fontSize = 24.sp) }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("CAT RESOLUTION", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("PRO", color = Purple, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepPurple)
    )
}

@Composable
private fun GamesScreen(padding: PaddingValues, value: Float, onValue: (Float) -> Unit, onAdd: () -> Unit, applied: Boolean, onActivate: () -> Unit, onRestore: () -> Unit, shizukuReady: Boolean) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(padding),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("TELA ESTICADA", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold)
            Text("Alongamento inteligente para seus jogos", color = Muted, fontSize = 13.sp)
        }
        item { MultiplierCard(value, onValue, onActivate, onRestore, applied) }
        item { ShizukuCard(shizukuReady) }
        item { FeatureRow() }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("MEUS JOGOS", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                TextButton(onClick = onAdd) { Icon(Icons.Default.Add, null, tint = Purple); Spacer(Modifier.width(4.dp)); Text("ADICIONAR", color = Purple) }
            }
        }
        items(listOf("Blood Strike", "Free Fire", "PUBG Mobile")) { GameItem(it) }
    }
}

@Composable
private fun MultiplierCard(value: Float, onValue: (Float) -> Unit, onActivate: () -> Unit, onRestore: () -> Unit, applied: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(18.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("MULTIPLICADOR ATUAL", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold); Text(formatMultiplier(value), color = Purple, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold) }
                Icon(Icons.Default.Tune, null, tint = Purple, modifier = Modifier.size(30.dp))
            }
            Slider(value = value, onValueChange = onValue, valueRange = 1f..1.3f, colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = Purple, activeTrackColor = Purple, inactiveTrackColor = Color(0xFF513769)))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("1.00x", color = Muted, fontSize = 11.sp); Text("1.30x", color = Muted, fontSize = 11.sp) }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ResolutionBox("NATIVA", "2688 x 1216", Modifier.weight(1f))
                ResolutionBox("PROJEÇÃO", "${(2688f / value).toInt()} x 1216", Modifier.weight(1f))
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(onClick = onActivate, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = Purple), shape = RoundedCornerShape(11.dp)) { Icon(Icons.Default.PlayArrow, null); Spacer(Modifier.width(5.dp)); Text(if (applied) "ATIVADO" else "ATIVAR", fontWeight = FontWeight.Bold) }
                OutlinedButton(onClick = { onValue(1.0f); onRestore() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(11.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Icon(Icons.Default.Restore, null); Spacer(Modifier.width(5.dp)); Text("RESTAURAR") }
            }
            Spacer(Modifier.height(15.dp))
            Text("PRESETS RÁPIDOS", color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) { listOf(1.05f, 1.08f, 1.10f, 1.12f, 1.15f).forEach { preset -> PresetButton(formatMultiplier(preset), { onValue(preset) }) } }
        }
    }
}

@Composable private fun ResolutionBox(label: String, text: String, modifier: Modifier) { Column(modifier.clip(RoundedCornerShape(10.dp)).background(Color(0xFF30203F)).padding(10.dp)) { Text(label, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold); Text(text, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold) } }
@Composable private fun PresetButton(text: String, onClick: () -> Unit) { TextButton(onClick = onClick, modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF38204B)), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) { Text(text, color = Purple, fontSize = 11.sp) } }

@Composable private fun ShizukuCard(ready: Boolean) { Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF172D27)), shape = RoundedCornerShape(14.dp)) { Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.CheckCircle, null, tint = if (ready) Green else Color(0xFFFFB86B), modifier = Modifier.size(25.dp)); Spacer(Modifier.width(10.dp)); Column { Text(if (ready) "SHIZUKU AUTORIZADO" else "SHIZUKU NÃO AUTORIZADO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(if (ready) "Pronto para aplicar alterações" else "Abra o Shizuku e autorize o aplicativo", color = if (ready) Green else Color(0xFFFFB86B), fontSize = 11.sp) } } } }
@Composable private fun FeatureRow() { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(Icons.Default.Layers to "Overlay\nFlutuante", Icons.Default.Tune to "Projeção em\nTempo Real", Icons.Default.Restore to "Aplicar /\nRestaurar").forEach { (icon, label) -> Card(Modifier.weight(1f), colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(12.dp)) { Column(Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) { Icon(icon, null, tint = Purple, modifier = Modifier.size(23.dp)); Spacer(Modifier.height(5.dp)); Text(label, color = Muted, fontSize = 10.sp, textAlign = TextAlign.Center) } } } } }
@Composable private fun GameItem(name: String) { Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(13.dp)) { Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Color(0xFF49305B)), contentAlignment = Alignment.Center) { Icon(Icons.Default.Apps, null, tint = Purple) }; Spacer(Modifier.width(12.dp)); Text(name, color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f)); TextButton(onClick = {}) { Text("ABRIR", color = Purple, fontWeight = FontWeight.Bold) } } } }

@Composable private fun OverlayScreen(padding: PaddingValues, value: Float, onValue: (Float) -> Unit) { Column(Modifier.fillMaxSize().padding(padding).padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) { Text("OVERLAY FLUTUANTE", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold); Text("Controle o multiplicador por cima do seu jogo.", color = Muted); Card(colors = CardDefaults.cardColors(containerColor = Color(0xCC241731)), shape = RoundedCornerShape(18.dp)) { Column(Modifier.padding(20.dp)) { Text("TELA ESTICADA", color = Color.White, fontWeight = FontWeight.Bold); Text(formatMultiplier(value), color = Purple, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold); Slider(value, onValueChange = onValue, valueRange = 1f..1.3f, colors = androidx.compose.material3.SliderDefaults.colors(thumbColor = Purple, activeTrackColor = Purple)); Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { Button(onClick = {}, colors = ButtonDefaults.buttonColors(containerColor = Purple), modifier = Modifier.weight(1f)) { Text("APLICAR") }; OutlinedButton(onClick = { onValue(1f) }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)) { Text("RESTAURAR") } } } }; Spacer(Modifier.height(8.dp)); Text("O overlay permanece disponível sobre aplicativos compatíveis quando a permissão de sobreposição estiver ativa.", color = Muted, fontSize = 12.sp) } }
@Composable private fun HistoryScreen(padding: PaddingValues) { Column(Modifier.fillMaxSize().padding(padding).padding(18.dp)) { Text("HISTÓRICO", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold); Spacer(Modifier.height(14.dp)); Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(15.dp)) { Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.History, null, tint = Purple); Spacer(Modifier.width(12.dp)); Column { Text("Nenhuma alteração registrada", color = Color.White, fontWeight = FontWeight.Bold); Text("Suas ativações aparecerão aqui.", color = Muted, fontSize = 12.sp) } } } } }

@Composable private fun SettingsScreen(padding: PaddingValues, projectionMode: String, onProjectionModeChange: (String) -> Unit, darkMode: Boolean, onDarkModeChange: (Boolean) -> Unit, onCapture: () -> Unit) { LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) { item { Text("CONFIGURAÇÕES", color = Color.White, fontSize = 25.sp, fontWeight = FontWeight.ExtraBold); Text("O INSANO", color = Purple, fontWeight = FontWeight.Bold); Text("configuração local do dispositivo", color = Muted, fontSize = 12.sp) }; item { SettingsSection("ACESSOS TÉCNICOS") { SettingRow(Icons.Default.Shield, "Shizuku", "Autorizado", true); SettingRow(Icons.Default.Settings, "Root", "Ativar", false) } }; item { SettingsSection("MODO DE PROJEÇÃO") { ModeRow("ALONGAR", "Mantém a proporção enquanto aplica o alongamento.", projectionMode == "ALONGAR") { onProjectionModeChange("ALONGAR") }; ModeRow("CORTE LATERAL", "A região central ocupa o painel inteiro.", projectionMode == "CORTE LATERAL") { onProjectionModeChange("CORTE LATERAL") } } }; item { SettingsSection("PREFERÊNCIAS") { SettingRow(Icons.Default.Translate, "Idioma", "Português", false); Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.DarkMode, null, tint = Purple); Spacer(Modifier.width(12.dp)); Text("Tema escuro", color = Color.White, modifier = Modifier.weight(1f)); Switch(checked = darkMode, onCheckedChange = onDarkModeChange) } } }; item { SettingsSection("SUPORTE E DIAGNÓSTICO") { SettingRow(Icons.Default.BugReport, "Relatar um problema", "Enviar informações técnicas", false, onClick = onCapture); SettingRow(Icons.Default.Timer, "Captura de problemas", "Registrar etapas para diagnóstico", false, onClick = onCapture); SettingRow(Icons.Default.Info, "Sobre o Tela Esticada", "v.1.1.0", false) } } } }
@Composable private fun SettingsSection(title: String, content: @Composable () -> Unit) { Text(title, color = Muted, fontSize = 11.sp, fontWeight = FontWeight.Bold); Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(15.dp)) { Column(Modifier.padding(horizontal = 14.dp, vertical = 5.dp)) { content() } } }
@Composable private fun SettingRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String, positive: Boolean, onClick: () -> Unit = {}) { Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) { Icon(icon, null, tint = if (positive) Green else Purple); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(title, color = Color.White, fontWeight = FontWeight.SemiBold); Text(subtitle, color = if (positive) Green else Muted, fontSize = 11.sp) }; if (!positive) Icon(Icons.Default.OpenInNew, null, tint = Muted, modifier = Modifier.size(18.dp)) } }
@Composable private fun ModeRow(title: String, desc: String, selected: Boolean, onClick: () -> Unit) { Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(20.dp).clip(CircleShape).background(if (selected) Purple else Color.Transparent).clickable(onClick = onClick).padding(5.dp)); Spacer(Modifier.width(12.dp)); Column { Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp); Text(desc, color = Muted, fontSize = 11.sp) } } }

@Composable private fun AddAppDialog(onDismiss: () -> Unit) { var query by remember { mutableStateOf("") }; val apps = listOf("Blood Strike", "Free Fire", "Agenda", "AnyDesk", "Authenticator", "Calculadora", "Chrome", "Gmail", "Instagram").filter { it.contains(query, true) }; AlertDialog(onDismissRequest = onDismiss, containerColor = Panel, title = { Text("ADICIONAR APLICATIVO", color = Color.White) }, text = { Column { Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Color(0xFF38204B)).padding(10.dp), verticalAlignment = Alignment.CenterVertically) { Icon(Icons.Default.Search, null, tint = Muted); Spacer(Modifier.width(8.dp)); androidx.compose.material3.OutlinedTextField(value = query, onValueChange = { query = it }, placeholder = { Text("Buscar aplicativo") }, singleLine = true) }; Spacer(Modifier.height(8.dp)); apps.forEach { app -> Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) { Text(app, color = Color.White, modifier = Modifier.weight(1f)); TextButton(onClick = onDismiss) { Text("ADICIONAR", color = Purple, fontSize = 11.sp) } } } } }, confirmButton = { TextButton(onClick = onDismiss) { Text("FECHAR", color = Purple) } }) }

@Composable private fun CaptureDialog(running: Boolean, captured: Boolean, onDismiss: () -> Unit, onStart: () -> Unit, onStop: () -> Unit, onReset: () -> Unit) { var consent by remember { mutableStateOf(false) }; AlertDialog(onDismissRequest = onDismiss, containerColor = Panel, title = { Text(if (captured) "REVISAR CAPTURA" else if (running) "CAPTURA EM ANDAMENTO" else "ANTES DE COMEÇAR", color = Color.White) }, text = { Column { if (captured) { Text("A captura foi finalizada.", color = Color.White); Text("Duração e etapas registradas disponíveis para revisão.", color = Muted, fontSize = 12.sp); Spacer(Modifier.height(12.dp)); Text("O que aconteceu? (opcional)", color = Muted, fontSize = 12.sp) } else if (running) { Text("Capturando e funcionando — 00:42", color = Green, fontWeight = FontWeight.Bold); Text("Logs técnicos e etapas do aplicativo estão sendo registrados.", color = Muted, fontSize = 12.sp) } else { Text("O APK poderá registrar logs técnicos, estado do Shizuku, abertura de jogos e mensagens de erro.", color = Color.White, fontSize = 13.sp); Spacer(Modifier.height(8.dp)); Text("Não gravamos tela, áudio, senhas ou conversas.", color = Muted, fontSize = 12.sp); Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(consent, { consent = it }); Text("Li e concordo com o uso técnico dos dados", color = Color.White, fontSize = 11.sp) } } } }, confirmButton = { if (captured) { Row { TextButton(onClick = { onReset(); onDismiss() }) { Text("APAGAR", color = Color(0xFFFF6B6B)) }; TextButton(onClick = onDismiss) { Text("AGORA NÃO", color = Muted) }; Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text("ENVIAR CAPTURA") } } } else if (running) Button(onClick = onStop, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC4545))) { Text("PARAR") } else Button(onClick = onStart, enabled = consent, colors = ButtonDefaults.buttonColors(containerColor = Purple)) { Text("INICIAR CAPTURA") } }) }

private fun formatMultiplier(value: Float): String = String.format(Locale.US, "%.2fx", value)

private object ShizukuBridge {
    fun isReady(context: Context): Boolean = StretchProjectionEngine.isReady(context)

    fun requestPermissionIfNeeded(): Boolean =
        StretchProjectionEngine.requestPermissionIfNeeded()

    fun apply(context: Context, multiplier: Float): Boolean =
        StretchProjectionEngine.apply(context, multiplier)

    fun restore(context: Context): Boolean =
        StretchProjectionEngine.restore(context)
}
