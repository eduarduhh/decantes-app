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
import androidx.compose.ui.unit.sp
import com.eduarduhh.decantes.data.relation.StatusPerfume
import com.eduarduhh.decantes.ui.theme.AlertaAmarelo
import com.eduarduhh.decantes.ui.theme.ErroVermelho
import com.eduarduhh.decantes.ui.theme.SucessoVerde

@Composable
fun StatusBadge(status: StatusPerfume, modifier: Modifier = Modifier) {
    val (texto, cor) = when (status) {
        StatusPerfume.PENDENTE -> "Pendente" to ErroVermelho
        StatusPerfume.PARCIAL -> "Parcial" to AlertaAmarelo
        StatusPerfume.QUITADO -> "Quitado" to SucessoVerde
    }
    Text(
        text = texto,
        color = Color.White,
        fontSize = 12.sp,
        modifier = modifier
            .background(cor, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        style = MaterialTheme.typography.labelSmall.copy(color = Color.White)
    )
}
