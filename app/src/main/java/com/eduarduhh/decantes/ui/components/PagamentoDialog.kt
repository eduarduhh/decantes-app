package com.eduarduhh.decantes.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PagamentoDialog(
    titulo: String = "Lançar pagamento",
    labelValor: String = "Valor pago (R$)",
    valorInicial: Double? = null,
    dataInicialMillis: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit,
    onConfirmar: (valor: Double, dataMillis: Long) -> Unit
) {
    var valorTexto by rememberSaveable { mutableStateOf(valorInicial?.let { "%.2f".format(it) } ?: "") }
    var valorErro by remember { mutableStateOf<String?>(null) }
    var dataMillis by rememberSaveable { mutableStateOf(dataInicialMillis) }
    var mostrarDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(titulo) },
        text = {
            Column {
                OutlinedTextField(
                    value = valorTexto,
                    onValueChange = {
                        valorTexto = it
                        valorErro = null
                    },
                    label = { Text(labelValor) },
                    isError = valorErro != null,
                    supportingText = { valorErro?.let { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(
                    onClick = { mostrarDatePicker = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    Text("Data: ${formatarData(dataMillis)}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val valor = parseValorDecimal(valorTexto)
                if (valor == null || valor <= 0.0) {
                    valorErro = "Informe um valor válido maior que zero"
                    return@TextButton
                }
                onConfirmar(valor, dataMillis)
            }) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (mostrarDatePicker) {
        val estado = rememberDatePickerState(initialSelectedDateMillis = dataMillis)
        DatePickerDialog(
            onDismissRequest = { mostrarDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    estado.selectedDateMillis?.let { dataMillis = it }
                    mostrarDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estado)
        }
    }
}
