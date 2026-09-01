package com.eduarduhh.decantes.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fretes",
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
data class Frete(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val grupoId: Long,
    val valor: Double,
    val data: Long
)
