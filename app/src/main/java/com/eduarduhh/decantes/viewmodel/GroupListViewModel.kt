package com.eduarduhh.decantes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduarduhh.decantes.data.entity.Grupo
import com.eduarduhh.decantes.data.repository.DecantesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GrupoResumoUi(
    val grupo: Grupo,
    val totalGrupo: Double,
    val totalPago: Double
)

data class GroupListUiState(
    val grupos: List<GrupoResumoUi> = emptyList(),
    val carregando: Boolean = true
)

class GroupListViewModel(private val repository: DecantesRepository) : ViewModel() {

    val uiState: StateFlow<GroupListUiState> = combine(
        repository.observarGruposComPerfumes(),
        repository.observarTodosPerfumesComPagamentos()
    ) { grupos, perfumesComPagamentos ->
        val porGrupo = perfumesComPagamentos.groupBy { it.perfume.grupoId }
        val resumos = grupos.map { grupoComPerfumes ->
            val perfumes = porGrupo[grupoComPerfumes.grupo.id] ?: emptyList()
            val frete = grupoComPerfumes.totalFrete
            GrupoResumoUi(
                grupo = grupoComPerfumes.grupo,
                totalGrupo = perfumes.sumOf { it.perfume.valorTotal } + frete,
                totalPago = perfumes.sumOf { it.valorPago } + frete
            )
        }
        GroupListUiState(grupos = resumos, carregando = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GroupListUiState())

    fun criarGrupo(nome: String) {
        val nomeTratado = nome.trim()
        if (nomeTratado.isEmpty()) return
        viewModelScope.launch {
            repository.criarGrupo(nomeTratado)
        }
    }
}
