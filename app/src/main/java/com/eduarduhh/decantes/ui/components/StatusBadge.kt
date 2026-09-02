package com.eduarduhh.decantes.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eduarduhh.decantes.data.relation.StatusPerfume
import com.eduarduhh.decantes.ui.theme.AlertaAmarelo
import com.eduarduhh.decantes.ui.theme.ErroVermelho
import com.eduarduhh.decantes.ui.theme.SucessoVerde
import com.eduarduhh.decantes.ui.theme.TextoBadgeEscuro

@Composable
fun StatusBadge(status: StatusPerfume, modifier: Modifier = Modifier) {
    val (texto, corFundo, corTexto) = when (status) {
        StatusPerfume.PENDENTE -> Triple("Pendente", ErroVermelho, TextoBadgeEscuro)
        StatusPerfume.PARCIAL -> Triple("Parcial", AlertaAmarelo, TextoBadgeEscuro)
        StatusPerfume.QUITADO -> Triple("Quitado", SucessoVerde, Color.White)
    }
    Text(
        text = texto,
        modifier = modifier
            .background(corFundo, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall.copy(color = corTexto)
    )
}
