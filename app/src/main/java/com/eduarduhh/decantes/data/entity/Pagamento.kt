package com.eduarduhh.decantes.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pagamentos",
    foreignKeys = [
        ForeignKey(
            entity = Perfume::class,
            parentColumns = ["id"],
            childColumns = ["perfumeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("perfumeId")]
)
data class Pagamento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val perfumeId: Long,
    val valor: Double,
    val data: Long
)
