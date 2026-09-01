package com.eduarduhh.decantes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduarduhh.decantes.data.entity.Pagamento
import com.eduarduhh.decantes.data.relation.PerfumeComPagamentos
import com.eduarduhh.decantes.data.repository.DecantesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PerfumeDetailUiState(
    val perfumeComPagamentos: PerfumeComPagamentos? = null,
    val carregando: Boolean = true,
    val perfumeExcluido: Boolean = false
)

class PerfumeDetailViewModel(
    private val repository: DecantesRepository,
    private val perfumeId: Long
) : ViewModel() {

    private val _perfumeExcluido = MutableStateFlow(false)

    val uiState: StateFlow<PerfumeDetailUiState> = repository
        .observarPerfumeComPagamentos(perfumeId)
        .let { flow ->
            kotlinx.coroutines.flow.combine(flow, _perfumeExcluido) { perfume, excluido ->
                PerfumeDetailUiState(
                    perfumeComPagamentos = perfume,
                    carregando = false,
                    perfumeExcluido = excluido
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PerfumeDetailUiState())

    fun lancarPagamento(valor: Double, dataMillis: Long) {
        viewModelScope.launch {
            repository.lancarPagamento(perfumeId, valor, dataMillis)
        }
    }

    fun editarPagamento(pagamento: Pagamento, valor: Double, dataMillis: Long) {
        viewModelScope.launch {
            repository.atualizarPagamento(pagamento.copy(valor = valor, data = dataMillis))
        }
    }

    fun excluirPagamento(pagamento: Pagamento) {
        viewModelScope.launch {
            repository.excluirPagamento(pagamento)
        }
    }

    fun excluirPerfume() {
        val perfume = uiState.value.perfumeComPagamentos?.perfume ?: return
        viewModelScope.launch {
            repository.excluirPerfume(perfume)
            _perfumeExcluido.value = true
        }
    }
}
