package com.eduarduhh.decantes.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.eduarduhh.decantes.data.entity.Frete
import com.eduarduhh.decantes.data.entity.Grupo
import com.eduarduhh.decantes.data.entity.Perfume

data class GrupoComPerfumes(
    @Embedded
    val grupo: Grupo,
    @Relation(
        parentColumn = "id",
        entityColumn = "grupoId"
    )
    val perfumes: List<Perfume>,
    @Relation(
        parentColumn = "id",
        entityColumn = "grupoId"
    )
    val fretes: List<Frete> = emptyList()
) {
    val totalFrete: Double get() = fretes.sumOf { it.valor }
}
