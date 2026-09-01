package com.eduarduhh.decantes.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta fria e leve: azul suave como protagonista, verde-azulado como
// acento e um terracota suave reservado para erro/aviso — dá contraste com
// a família fria sem recair em vermelho vivo. Troca da paleta quente
// (âmbar/laranja) anterior a pedido do usuário, que queria tons mais frios
// e leves.
val AzulSuave = Color(0xFF6C93B0)
val AzulAcinzentado = Color(0xFFA8C0CE)
val VerdeAzulado = Color(0xFF8FB3A8)
val TerracotaSuave = Color(0xFFB0705A)

// --- Tema claro ---
val PrimaryLight = AzulSuave
val OnPrimaryLight = Color.White
val PrimaryContainerLight = Color(0xFFD6E4EC)
val OnPrimaryContainerLight = Color(0xFF1F3A4D)

val SecondaryLight = Color(0xFF5C7E93)
val OnSecondaryLight = Color.White
val SecondaryContainerLight = AzulAcinzentado
val OnSecondaryContainerLight = Color(0xFF2A4653)

val TertiaryLight = Color(0xFF4F7A6D)
val OnTertiaryLight = Color.White
val TertiaryContainerLight = Color(0xFFD9EAE4)
val OnTertiaryContainerLight = Color(0xFF1E3A32)

val BackgroundLight = Color(0xFFF5F8FA)
val SurfaceLight = Color(0xFFF5F8FA)
val ErrorLight = TerracotaSuave
val OnErrorLight = Color.White

// --- Tema escuro ---
val PrimaryDark = AzulSuave
val OnPrimaryDark = Color(0xFF122531)
val PrimaryContainerDark = Color(0xFF2D4A5E)
val OnPrimaryContainerDark = Color(0xFFCFE3EC)

val SecondaryDark = AzulAcinzentado
val OnSecondaryDark = Color(0xFF17303B)
val SecondaryContainerDark = Color(0xFF3B5766)
val OnSecondaryContainerDark = Color(0xFFD6E4EC)

val TertiaryDark = VerdeAzulado
val OnTertiaryDark = Color(0xFF163229)
val TertiaryContainerDark = Color(0xFF335247)
val OnTertiaryContainerDark = Color(0xFFD9EAE4)

val BackgroundDark = Color(0xFF10181E)
val SurfaceDark = Color(0xFF10181E)
val ErrorDark = Color(0xFFD9A38C)
val OnErrorDark = Color(0xFF3F2013)

// Cores de status (badges pendente/parcial/quitado).
// Pendente usa o mesmo terracota do error (só aqui e no badge), parcial usa
// um âmbar dessaturado (mantém o significado universal de "atenção" mesmo
// fora da família fria do resto do app), quitado continua verde fixo por
// clareza semântica.
val SucessoVerde = Color(0xFF2E7D32)
val AlertaAmarelo = Color(0xFFC79A3C)
val ErroVermelho = TerracotaSuave
