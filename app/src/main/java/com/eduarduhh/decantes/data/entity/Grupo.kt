package com.eduarduhh.decantes.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "grupos")
data class Grupo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nome: String,
    val dataCriacao: Long = System.currentTimeMillis()
)
