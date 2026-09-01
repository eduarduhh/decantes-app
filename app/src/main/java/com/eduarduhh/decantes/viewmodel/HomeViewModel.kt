package com.eduarduhh.decantes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduarduhh.decantes.data.relation.PerfumeComPagamentos
import com.eduarduhh.decantes.data.repository.DecantesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class GrupoPendenciaUi(
    val grupoId: Long,
    val grupoNome: String,
    val perfumes: List<PerfumeComPagamentos>
) {
    val saldoGrupo: Double get() = perfumes.sumOf { it.saldoRestante }
}

data class HomeUiState(
    val totalDevido: Double = 0.0,
    val totalPago: Double = 0.0,
    val gruposComPendencias: List<GrupoPendenciaUi> = emptyList(),
    val carregando: Boolean = true
)

class HomeViewModel(private val repository: DecantesRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        repository.observarGruposComPerfumes(),
        repository.observarTodosPerfumesComPagamentos()
    ) { grupos, perfumesComPagamentos ->
        val perfumesPorGrupo = perfumesComPagamentos
            .filter { it.saldoRestante > 0.0 }
            .groupBy { it.perfume.grupoId }

        val gruposPendentes = grupos.mapNotNull { grupoComPerfumes ->
            val pendentes = perfumesPorGrupo[grupoComPerfumes.grupo.id] ?: return@mapNotNull null
            if (pendentes.isEmpty()) return@mapNotNull null
            GrupoPendenciaUi(
                grupoId = grupoComPerfumes.grupo.id,
                grupoNome = grupoComPerfumes.grupo.nome,
                perfumes = pendentes
            )
        }

        val totalDevido = gruposPendentes.sumOf { grupo -> grupo.perfumes.sumOf { it.saldoRestante } }
        val totalPago = perfumesComPagamentos.sumOf { it.valorPago } + grupos.sumOf { it.totalFrete }

        HomeUiState(
            totalDevido = totalDevido,
            totalPago = totalPago,
            gruposComPendencias = gruposPendentes,
            carregando = false
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())
}
