package com.eduarduhh.decantes.data.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.eduarduhh.decantes.data.entity.Pagamento
import com.eduarduhh.decantes.data.entity.Perfume

data class PerfumeComPagamentos(
    @Embedded
    val perfume: Perfume,
    @Relation(
        parentColumn = "id",
        entityColumn = "perfumeId"
    )
    val pagamentos: List<Pagamento>
) {
    val valorPago: Double get() = pagamentos.sumOf { it.valor }
    val saldoRestante: Double get() = (perfume.valorTotal - valorPago).coerceAtLeast(0.0)
    val status: StatusPerfume
        get() = when {
            valorPago <= 0.0 -> StatusPerfume.PENDENTE
            valorPago < perfume.valorTotal -> StatusPerfume.PARCIAL
            else -> StatusPerfume.QUITADO
        }
}

enum class StatusPerfume {
    PENDENTE, PARCIAL, QUITADO
}
