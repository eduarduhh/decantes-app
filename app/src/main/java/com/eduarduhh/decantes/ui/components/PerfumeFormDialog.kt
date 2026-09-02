package com.eduarduhh.decantes.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfumeFormDialog(
    titulo: String,
    nomeInicial: String = "",
    marcaInicial: String = "",
    mlInicial: String = "",
    valorInicial: String = "",
    categoriaInicial: String = CATEGORIAS_PERFUME.last(),
    marcasSugeridas: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onConfirmar: (nome: String, marca: String, ml: String, valor: String, categoria: String) -> Unit
) {
    var nome by rememberSaveable { mutableStateOf(nomeInicial) }
    var marca by rememberSaveable { mutableStateOf(marcaInicial) }
    var ml by rememberSaveable { mutableStateOf(mlInicial) }
    var valor by rememberSaveable { mutableStateOf(valorInicial) }
    var categoria by rememberSaveable { mutableStateOf(categoriaInicial) }
    var nomeErro by remember { mutableStateOf<String?>(null) }
    var mlErro by remember { mutableStateOf<String?>(null) }
    var valorErro by remember { mutableStateOf<String?>(null) }
    var menuCategoriaExpandido by remember { mutableStateOf(false) }
    var menuMarcaExpandido by remember { mutableStateOf(false) }
    val marcasFiltradas = remember(marca, marcasSugeridas) {
        if (marca.isBlank()) {
            marcasSugeridas
        } else {
            marcasSugeridas.filter { it.contains(marca, ignoreCase = true) && !it.equals(marca, ignoreCase = true) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = menuMarcaExpandido && marcasFiltradas.isNotEmpty(),
                    onExpandedChange = { menuMarcaExpandido = it }
                ) {
                    OutlinedTextField(
                        value = marca,
                        onValueChange = {
                            marca = it
                            menuMarcaExpandido = true
                        },
                        label = { Text("Marca (opcional)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryEditable)
                    )
                    DropdownMenu(
                        expanded = menuMarcaExpandido && marcasFiltradas.isNotEmpty(),
                        onDismissRequest = { menuMarcaExpandido = false },
                        modifier = Modifier.exposedDropdownSize()
                    ) {
                        marcasFiltradas.forEach { sugestao ->
                            DropdownMenuItem(
                                text = { Text(sugestao) },
                                onClick = {
                                    marca = sugestao
                                    menuMarcaExpandido = false
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = nome,
                    onValueChange = {
                        nome = it
                        nomeErro = null
                    },
                    label = { Text("Nome do perfume") },
                    isError = nomeErro != null,
                    supportingText = { nomeErro?.let { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = ml,
                    onValueChange = {
                        ml = it
                        mlErro = null
                    },
                    label = { Text("Quantidade (ml)") },
                    isError = mlErro != null,
                    supportingText = { mlErro?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = valor,
                    onValueChange = {
                        valor = it
                        valorErro = null
                    },
                    label = { Text("Valor total (R$)") },
                    isError = valorErro != null,
                    supportingText = { valorErro?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = menuCategoriaExpandido,
                    onExpandedChange = { menuCategoriaExpandido = it },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    DropdownMenu(
                        expanded = menuCategoriaExpandido,
                        onDismissRequest = { menuCategoriaExpandido = false },
                        modifier = Modifier.exposedDropdownSize()
                    ) {
                        CATEGORIAS_PERFUME.forEach { opcao ->
                            DropdownMenuItem(
                                text = { Text(opcao) },
                                onClick = {
                                    categoria = opcao
                                    menuCategoriaExpandido = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val mlValido = ml.trim().toIntOrNull()
                val valorValido = valor.trim().replace(",", ".").toDoubleOrNull()
                var valido = true
                if (nome.trim().isEmpty()) {
                    nomeErro = "Informe o nome do perfume"
                    valido = false
                }
                if (mlValido == null || mlValido <= 0) {
                    mlErro = "Informe uma quantidade válida"
                    valido = false
                }
                if (valorValido == null || valorValido <= 0.0) {
                    valorErro = "Informe um valor válido"
                    valido = false
                }
                if (valido) onConfirmar(nome, marca, ml, valor, categoria)
            }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
