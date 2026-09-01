package com.eduarduhh.decantes.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "perfumes",
    foreignKeys = [
        ForeignKey(
            entity = Grupo::class,
            parentColumns = ["id"],
            childColumns = ["grupoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("grupoId")]
)
data class Perfume(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val grupoId: Long,
    val nome: String,
    val marca: String = "",
    val ml: Int,
    val valorTotal: Double,
    val categoria: String = "Compartilhável"
)
