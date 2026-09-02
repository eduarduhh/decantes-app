package com.eduarduhh.decantes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduarduhh.decantes.data.entity.Pagamento
import com.eduarduhh.decantes.data.relation.PerfumeComPagamentos
import com.eduarduhh.decantes.data.repository.DecantesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PerfumeDetailUiState(
    val perfumeComPagamentos: PerfumeComPagamentos? = null,
    val marcasSugeridas: List<String> = emptyList(),
    val carregando: Boolean = true,
    val perfumeExcluido: Boolean = false
)

class PerfumeDetailViewModel(
    private val repository: DecantesRepository,
    private val perfumeId: Long
) : ViewModel() {

    private val _perfumeExcluido = MutableStateFlow(false)
    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro.asStateFlow()

    val uiState: StateFlow<PerfumeDetailUiState> = combine(
        repository.observarPerfumeComPagamentos(perfumeId),
        _perfumeExcluido,
        repository.observarMarcas()
    ) { perfume, excluido, marcas ->
        PerfumeDetailUiState(
            perfumeComPagamentos = perfume,
            marcasSugeridas = marcas,
            carregando = false,
            perfumeExcluido = excluido
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PerfumeDetailUiState())

    fun lancarPagamento(valor: Double, dataMillis: Long) {
        viewModelScope.launch {
            repository.lancarPagamento(perfumeId, valor, dataMillis)
        }
    }

    fun editarPerfume(nome: String, marca: String, mlTexto: String, valorTexto: String, categoria: String) {
        val perfume = uiState.value.perfumeComPagamentos?.perfume ?: return
        val nomeTratado = nome.trim()
        val marcaTratada = marca.trim()
        val ml = mlTexto.trim().toIntOrNull()
        val valor = valorTexto.trim().replace(",", ".").toDoubleOrNull()

        if (nomeTratado.isEmpty() || ml == null || ml <= 0 || valor == null || valor <= 0.0) {
            _erro.value = "Dados inválidos"
            return
        }
        viewModelScope.launch {
            repository.atualizarPerfume(
                perfume.copy(nome = nomeTratado, marca = marcaTratada, ml = ml, valorTotal = valor, categoria = categoria)
            )
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
