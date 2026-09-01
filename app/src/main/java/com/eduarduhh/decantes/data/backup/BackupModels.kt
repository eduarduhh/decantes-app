package com.eduarduhh.decantes.data.backup

import kotlinx.serialization.Serializable

@Serializable
data class BackupGrupo(
    val id: Long,
    val nome: String,
    val dataCriacao: Long
)

@Serializable
data class BackupPerfume(
    val id: Long,
    val grupoId: Long,
    val nome: String,
    val marca: String = "",
    val ml: Int,
    val valorTotal: Double,
    val categoria: String = "Compartilhável"
)

@Serializable
data class BackupPagamento(
    val id: Long,
    val perfumeId: Long,
    val valor: Double,
    val data: Long
)

@Serializable
data class BackupFrete(
    val id: Long,
    val grupoId: Long,
    val valor: Double,
    val data: Long
)

@Serializable
data class BackupData(
    val versao: Int = 1,
    val grupos: List<BackupGrupo>,
    val perfumes: List<BackupPerfume>,
    val pagamentos: List<BackupPagamento>,
    val fretes: List<BackupFrete> = emptyList()
)
