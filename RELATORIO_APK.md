# Relatório de análise do APK mostrado no vídeo

**Vídeo analisado:** [YouTube — referência fornecida](https://youtu.be/oc4r9PpRYME?si=kZt2cvsV5U7wb0qi)  
**Aplicativo identificado na análise:** **CAT RESOLUTION PRO**  
**Objetivo deste documento:** registrar, em texto, a interface, os componentes visíveis, as telas e os fluxos demonstrados no vídeo, para servir como referência de produto e implementação.

> **Nota de precisão:** esta é uma análise visual automatizada do vídeo, não uma transcrição literal quadro a quadro. Alguns textos pequenos, nomes de ícones e detalhes técnicos podem exigir conferência manual no vídeo original. Os itens classificados como “inferência” não foram necessariamente escritos ou explicados explicitamente na interface.

## 1. Identidade visual e estrutura geral

O APK usa um tema predominantemente escuro, com fundo em tons de preto ou roxo muito escuro. A cor de destaque é o roxo, acompanhada por lilás, branco e alguns indicadores verdes e vermelhos.

No topo aparece uma identidade visual formada por uma cabeça de gato estilizada em roxo, com olhos brancos, ao lado do nome **“CAT RESOLUTION PRO”**.

A navegação principal é feita por uma barra fixa na parte inferior da tela, com quatro seções:

| Seção | Ícone observado | Função aparente |
|---|---|---|
| **Jogos** | Controle de videogame | Gerenciar jogos e configuração de resolução |
| **Overlay** | Camadas sobrepostas | Acessar ou configurar o painel flutuante |
| **Histórico** | Relógio | Consultar registros ou alterações anteriores |
| **Config** | Engrenagem | Configurações, suporte e diagnóstico |

A interface utiliza cartões, botões arredondados, divisores discretos e controles com destaque roxo para ações primárias.

## 2. Tela principal — aba “Jogos”

A tela principal é apresentada como a área de controle da função de tela esticada/resolução.

### 2.1 Cabeçalho

O título principal aparece em letras maiúsculas:

**TELA ESTICADA**

Abaixo dele há um texto explicativo semelhante a:

> “Alongamento inteligente para seus jogos, mantenha a proporção precisa e controle o suporte.”

### 2.2 Painel do multiplicador

O painel central mostra o estado atual do multiplicador de resolução. O rótulo observado é:

**MULTIPLICADOR ATUAL**

Ao lado ou abaixo do rótulo aparece um valor numérico, mostrado no exemplo como:

**1,19x**

Há um controle deslizante horizontal para ajustar o valor. A faixa observada vai de:

- **1.00x**;
- até **1.30x**.

O aplicativo também apresenta informações de resolução, separando a resolução original da resolução resultante da projeção:

- **NATIVA:** exemplo observado de **2688 x 1216**;
- **PROJEÇÃO:** exemplo observado de **3198 x 1216**.

A resolução de projeção parece ser recalculada conforme o multiplicador é alterado.

### 2.3 Botões principais

O painel contém dois botões de ação:

- **ATIVAR:** botão principal roxo, com texto branco;
- **RESTAURAR:** botão secundário em cinza ou branco, com texto escuro.

A função aparente de **ATIVAR** é aplicar a projeção/multiplicador selecionado. A função de **RESTAURAR** é retornar a tela ao estado original.

### 2.4 Estado do Shizuku

Existe um cartão de status indicando:

**SHIZUKU AUTORIZADO**

O cartão apresenta um ícone de confirmação verde, sugerindo que o aplicativo recebeu acesso privilegiado por meio do Shizuku.

### 2.5 Presets rápidos

Abaixo do controle principal há uma linha de botões menores para selecionar valores predefinidos rapidamente:

- **1,05x**;
- **1,08x**;
- **1,1x**;
- **1,12x**;
- **1,15x**.

Esses botões funcionam como atalhos para configurar o multiplicador sem precisar arrastar o slider manualmente.

### 2.6 Recursos destacados

A tela também apresenta três recursos com ícones circulares ou cartões de destaque:

1. **Overlay Flutuante** — permite controlar o multiplicador por uma janela sobre outros aplicativos, inclusive durante um jogo;
2. **Projeção em Tempo Real** — indica que a alteração pode ser aplicada e visualizada imediatamente;
3. **Aplicar / Restaurar** — representa as ações de ativar a projeção e retornar à configuração anterior.

### 2.7 Lista de jogos

Na parte inferior aparece uma lista de jogos configurados ou disponíveis para configuração. Foram identificados exemplos como:

- **Blood Strike**;
- **Free Fire**.

Os itens possuem ícones dos aplicativos e ações como:

- **ABRIR**, para iniciar o jogo;
- **ADICIONAR**, para incluir um aplicativo na lista do APK.

## 3. Tela de configurações — aba “Config”

A aba de configurações apresenta uma lista vertical de opções, organizada em blocos relacionados a conta, acesso técnico, projeção, idioma, aparência, suporte e informações do aplicativo.

### 3.1 Perfil do usuário

No topo da tela aparece um perfil com o nome:

**O INSANO**

Também é exibido um endereço de e-mail associado ao desenvolvedor ou à licença. A área sugere informações sobre o usuário e o status da licença.

### 3.2 Acessos técnicos

A tela mostra o estado do **Shizuku**, indicando se a autorização está ativa.

Também existe uma opção relacionada a **Root**, com uma ação semelhante a:

**ATIVAR**

A finalidade aparente é solicitar ou habilitar acesso root para os recursos que dependem de permissões mais elevadas.

### 3.3 Modo de projeção

Há dois seletores para escolher como a imagem será projetada:

#### ALONGAR

Descrição observada: mantém a conexão ou a proporção da imagem enquanto aplica o alongamento.

#### CORTE LATERAL

Descrição observada: a região central ocupa o painel inteiro, com possível corte das laterais.

No vídeo, o modo **CORTE LATERAL** é associado ao resultado em que aparecem bordas roxas sólidas nas laterais após a aplicação do multiplicador.

### 3.4 Idioma

A tela oferece opções de idioma por meio de bandeiras ou seletores visuais. Foram identificados:

- Português, selecionado;
- Inglês;
- Japonês;
- Chinês.

### 3.5 Tema escuro

Há um botão de alternância para ligar ou desligar o **Tema Escuro**. O vídeo apresenta o aplicativo já utilizando a aparência escura.

### 3.6 Suporte e diagnóstico

A seção de suporte inclui as seguintes opções:

- **Relatar um problema:** envia informações como modelo do aparelho e histórico técnico;
- **Captura de problemas:** inicia um processo de coleta de logs para diagnosticar falhas;
- **Sobre o Tela Esticada:** exibe informações do aplicativo;
- **Canal oficial no YouTube:** direciona para o canal oficial.

A versão identificada na tela de informações é:

**v.1.1.0**

## 4. Fluxo de captura de problemas

O fluxo de captura é demonstrado aproximadamente entre **01:13 e 02:16** do vídeo.

### 4.1 Janela de consentimento

Ao selecionar a captura de problemas, aparece um pop-up com o título:

**ANTES DE COMEÇAR**

O texto informa que o APK poderá registrar dados técnicos, incluindo:

- logs técnicos;
- estado do Shizuku;
- abertura de jogos;
- mensagens de erro;
- etapas relacionadas ao funcionamento do aplicativo.

O aviso também deixa claro que o aplicativo **não grava**:

- a tela;
- áudio;
- senhas;
- conversas.

Para continuar, o usuário precisa marcar um checkbox com texto semelhante a:

**Li e concordo com o uso dos dados para fins técnicos**

Depois, deve tocar no botão:

**INICIAR CAPTURA**

### 4.2 Captura em andamento

Durante a captura, o item correspondente na tela de configurações muda para um estado semelhante a:

**Capturando e funcionando — [cronômetro]**

Um cronômetro mostra a duração da captura. Também aparece um botão vermelho:

**PARAR**

Esse botão encerra a coleta de informações.

### 4.3 Revisão da captura

Ao finalizar, é aberta uma tela com o título:

**REVISAR CAPTURA**

Nessa tela aparecem:

- a duração da captura;
- as etapas registradas;
- um campo opcional para descrever o ocorrido;
- o campo com texto orientativo semelhante a **“O que aconteceu?”**.

Os botões disponíveis são:

- **APAGAR:** descarta a captura;
- **AGORA NÃO:** adia a decisão ou sai sem enviar;
- **ENVIAR CAPTURA:** encaminha os dados coletados para diagnóstico.

## 5. Tela para adicionar aplicativos

A tela de adição aparece como uma sobreposição ou tela separada contendo os aplicativos instalados no dispositivo.

### 5.1 Busca

No topo existe uma barra de pesquisa com o texto:

**Buscar aplicativo**

### 5.2 Lista de aplicativos

A lista é vertical e apresenta o nome e o ícone de cada aplicativo. Foram observados exemplos como:

- Blood Strike;
- Free Fire;
- Agenda;
- AnyDesk;
- Authenticator;
- Calculadora;
- Carteira do Google;
- Chrome;
- Gmail;
- Instagram;
- e outros aplicativos instalados.

Cada linha possui um botão roxo:

**ADICIONAR**

A ação adiciona o aplicativo selecionado à lista de jogos ou aplicativos gerenciados pelo APK.

## 6. Overlay flutuante durante o jogo

O overlay é demonstrado aproximadamente entre **04:06 e 06:10**, durante a execução do **PUBG Mobile**.

### 6.1 Aparência

O painel flutuante é pequeno e aparece sobre a tela do jogo. Ele possui:

- fundo semitransparente;
- bordas ou contornos roxos;
- aparência compacta;
- possibilidade de permanecer sobre o jogo enquanto ele está sendo executado.

### 6.2 Elementos internos

Dentro do painel aparecem:

- o título **TELA ESTICADA**;
- um slider para ajuste rápido do multiplicador;
- o valor atual do multiplicador;
- o botão **APLICAR**;
- o botão **RESTAURAR**.

### 6.3 Ação demonstrada

O usuário movimenta o slider até **1,30x** e toca em **APLICAR**.

Depois da aplicação, a imagem do jogo sofre uma alteração horizontal imediata. A interface parece achatada ou esticada, e surgem bordas roxas sólidas nas laterais da tela. Esse comportamento é associado ao modo de projeção **CORTE LATERAL**.

O botão **RESTAURAR** serve para desfazer a alteração e retornar à visualização anterior.

## 7. Fluxo de uso reconstruído

Com base nas telas observadas, o fluxo principal do APK pode ser descrito assim:

1. O usuário abre o **CAT RESOLUTION PRO**;
2. Acessa a aba **Jogos**;
3. Seleciona um multiplicador pelo slider ou por um preset rápido;
4. Confere a resolução nativa e a resolução de projeção calculada;
5. Verifica se o **Shizuku** está autorizado;
6. Toca em **ATIVAR**;
7. Opcionalmente, configura ou abre um jogo da lista;
8. Usa o **Overlay Flutuante** para alterar o multiplicador durante o jogo;
9. Toca em **APLICAR** para aplicar o novo valor;
10. Usa **RESTAURAR** para desfazer a projeção;
11. Acessa **Config** para escolher o modo de projeção, idioma e tema;
12. Usa a captura de problemas caso precise registrar falhas técnicas.

## 8. Componentes necessários para recriar uma interface semelhante

Para construir um APK com a mesma estrutura visual, a implementação precisaria conter pelo menos:

- navegação inferior com quatro abas;
- tema escuro configurável;
- logotipo e nome do aplicativo no cabeçalho;
- tela de jogos com slider de 1.00x a 1.30x;
- cálculo e exibição de resolução nativa e projetada;
- botões **ATIVAR** e **RESTAURAR**;
- indicador visual do estado do Shizuku;
- presets rápidos de multiplicador;
- lista de aplicativos/jogos;
- busca de aplicativos instalados;
- botões **ADICIONAR** e **ABRIR**;
- tela de configurações;
- seleção de modo **ALONGAR** ou **CORTE LATERAL**;
- seleção de idioma;
- toggle de tema escuro;
- overlay flutuante sobre outros aplicativos;
- fluxo de consentimento para captura de problemas;
- cronômetro de captura;
- tela de revisão e envio de diagnóstico;
- tela “Sobre” com versão do aplicativo;
- integração com Shizuku e, opcionalmente, suporte a root.

## 9. Inferências técnicas — separadas do que foi visualmente observado

A partir do comportamento apresentado, o aplicativo parece manipular parâmetros de exibição do Android para produzir um efeito de tela esticada em jogos. A análise sugere o uso de comandos de sistema ou ADB mediados pelo **Shizuku**, com possibilidade adicional de uso de **Root**.

Essa conclusão é uma inferência técnica baseada na presença dos controles de resolução, no status do Shizuku, na opção Root e na mudança visual aplicada durante o jogo. O vídeo não é suficiente, por si só, para confirmar quais comandos, APIs ou bibliotecas foram usados internamente.

Também não é possível afirmar apenas pela análise visual:

- quais permissões exatas o APK solicita;
- quais comandos ADB são executados;
- se o efeito altera DPI, resolução, densidade, janela ou viewport;
- como o overlay obtém a permissão de exibição sobre outros aplicativos;
- se o suporte root é realmente funcional ou apenas uma opção visual;
- quais dados são enviados ao suporte;
- se todos os nomes e textos pequenos estão grafados exatamente como aparecem.

## 10. Resumo visual para briefing de desenvolvimento

O produto é um aplicativo Android de tema escuro voltado a configurar uma projeção de tela esticada para jogos. Sua identidade utiliza roxo escuro, lilás, preto e um mascote de gato. A tela inicial centraliza um multiplicador ajustável, valores de resolução e os comandos **ATIVAR** e **RESTAURAR**. A navegação inferior organiza as áreas de jogos, overlay, histórico e configurações.

A experiência inclui seleção de jogos instalados, presets rápidos, status do Shizuku, modos de projeção, seleção de idioma, tema escuro e uma ferramenta de diagnóstico com consentimento, captura temporizada, revisão e envio. Durante o jogo, um painel flutuante compacto permite alterar rapidamente o multiplicador e aplicar ou restaurar a projeção.
