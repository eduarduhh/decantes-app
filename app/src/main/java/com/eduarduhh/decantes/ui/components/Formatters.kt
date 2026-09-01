package com.eduarduhh.decantes.ui.components

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val localePtBr = Locale("pt", "BR")
private val moedaFormat: NumberFormat = NumberFormat.getCurrencyInstance(localePtBr)
private val dataFormat = SimpleDateFormat("dd/MM/yyyy", localePtBr)

fun formatarMoeda(valor: Double): String = moedaFormat.format(valor)

fun formatarMoedaOuOculto(valor: Double, oculto: Boolean): String =
    if (oculto) "R$ ••••••" else formatarMoeda(valor)

fun formatarData(timestampMillis: Long): String = dataFormat.format(Date(timestampMillis))

fun parseValorDecimal(texto: String): Double? {
    val normalizado = texto.trim().replace(",", ".")
    if (normalizado.isEmpty()) return null
    val valor = normalizado.toDoubleOrNull() ?: return null
    return if (valor < 0) null else valor
}

fun parseInteiro(texto: String): Int? {
    val valor = texto.trim().toIntOrNull() ?: return null
    return if (valor < 0) null else valor
}
