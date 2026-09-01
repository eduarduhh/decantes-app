package com.eduarduhh.decantes.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.Female
import androidx.compose.material.icons.filled.Male
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Pequeno ícone que indica se um perfume é masculino, feminino ou compartilhável. */
@Composable
fun CategoriaIcon(categoria: String, modifier: Modifier = Modifier) {
    val (icone, descricao) = when (categoria) {
        "Masculino" -> Icons.Filled.Male to "Masculino"
        "Feminino" -> Icons.Filled.Female to "Feminino"
        else -> Icons.Filled.Diversity3 to "Compartilhável"
    }
    Icon(
        imageVector = icone,
        contentDescription = descricao,
        tint = LocalContentColor.current,
        modifier = modifier.size(18.dp)
    )
}
