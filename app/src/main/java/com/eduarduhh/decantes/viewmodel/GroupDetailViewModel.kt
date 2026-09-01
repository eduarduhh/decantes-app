package com.eduarduhh.decantes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduarduhh.decantes.data.entity.Frete
import com.eduarduhh.decantes.data.entity.Grupo
import com.eduarduhh.decantes.data.entity.Perfume
import com.eduarduhh.decantes.data.relation.PerfumeComPagamentos
import com.eduarduhh.decantes.data.relation.StatusPerfume
import com.eduarduhh.decantes.data.repository.DecantesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class OrdenacaoPerfume {
    NOME, VALOR, SALDO
}

data class GroupDetailUiState(
    val grupo: Grupo? = null,
    val perfumes: List<PerfumeComPagamentos> = emptyList(),
    val ordenacao: OrdenacaoPerfume = OrdenacaoPerfume.NOME,
    val mostrarQuitados: Boolean = true,
    val termoBusca: String = "",
    val totalGrupo: Double = 0.0,
    val totalPagoGrupo: Double = 0.0,
    val saldoGrupo: Double = 0.0,
    val fretes: List<Frete> = emptyList(),
    val totalFrete: Double = 0.0,
    val totalPerfumesCadastrados: Int = 0,
    val quantidadePorCategoria: Map<String, Int> = emptyMap(),
    val marcasSugeridas: List<String> = emptyList(),
    val carregando: Boolean = true,
    val grupoExcluido: Boolean = false
)

private data class Filtros(
    val ordenacao: OrdenacaoPerfume = OrdenacaoPerfume.NOME,
    val mostrarQuitados: Boolean = true,
    val termoBusca: String = ""
)

class GroupDetailViewModel(
    private val repository: DecantesRepository,
    private val grupoId: Long
) : ViewModel() {

    private val _grupoExcluido = MutableStateFlow(false)
    private val _filtros = MutableStateFlow(Filtros())

    val uiState: StateFlow<GroupDetailUiState> = combine(
        repository.observarGrupoComPerfumes(grupoId),
        repository.observarPerfumesComPagamentosDoGrupo(grupoId),
        _grupoExcluido,
        _filtros,
        repository.observarMarcas()
    ) { grupoComPerfumes, perfumes, excluido, filtros, marcas ->
        val termo = filtros.termoBusca.trim()
        val visiveis = perfumes
            .let { lista -> if (filtros.mostrarQuitados) lista else lista.filter { it.status != StatusPerfume.QUITADO } }
            .let { lista ->
                if (termo.isEmpty()) {
                    lista
                } else {
                    lista.filter {
                        it.perfume.nome.contains(termo, ignoreCase = true) ||
                            it.perfume.marca.contains(termo, ignoreCase = true)
                    }
                }
            }
        val ordenados = when (filtros.ordenacao) {
            OrdenacaoPerfume.NOME -> visiveis.sortedBy { it.perfume.nome.lowercase() }
            OrdenacaoPerfume.VALOR -> visiveis.sortedByDescending { it.perfume.valorTotal }
            OrdenacaoPerfume.SALDO -> visiveis.sortedByDescending { it.saldoRestante }
        }

        val fretes = grupoComPerfumes?.fretes.orEmpty()
        val totalFrete = fretes.sumOf { it.valor }

        GroupDetailUiState(
            grupo = grupoComPerfumes?.grupo,
            perfumes = ordenados,
            ordenacao = filtros.ordenacao,
            mostrarQuitados = filtros.mostrarQuitados,
            termoBusca = filtros.termoBusca,
            totalGrupo = perfumes.sumOf { it.perfume.valorTotal } + totalFrete,
            totalPagoGrupo = perfumes.sumOf { it.valorPago } + totalFrete,
            saldoGrupo = perfumes.sumOf { it.saldoRestante },
            fretes = fretes.sortedByDescending { it.data },
            totalFrete = totalFrete,
            totalPerfumesCadastrados = perfumes.size,
            quantidadePorCategoria = perfumes.groupingBy { it.perfume.categoria }.eachCount(),
            marcasSugeridas = marcas,
            carregando = false,
            grupoExcluido = excluido
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GroupDetailUiState())

    fun mudarOrdenacao(nova: OrdenacaoPerfume) {
        _filtros.value = _filtros.value.copy(ordenacao = nova)
    }

    fun alternarMostrarQuitados() {
        _filtros.value = _filtros.value.copy(mostrarQuitados = !_filtros.value.mostrarQuitados)
    }

    fun mudarTermoBusca(texto: String) {
        _filtros.value = _filtros.value.copy(termoBusca = texto)
    }

    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro.asStateFlow()

    fun adicionarPerfume(nome: String, marca: String, mlTexto: String, valorTexto: String, categoria: String) {
        val nomeTratado = nome.trim()
        val marcaTratada = marca.trim()
        val ml = mlTexto.trim().toIntOrNull()
        val valor = valorTexto.trim().replace(",", ".").toDoubleOrNull()

        if (nomeTratado.isEmpty()) {
            _erro.value = "Informe o nome do perfume"
            return
        }
        if (ml == null || ml <= 0) {
            _erro.value = "Informe uma quantidade em ml válida"
            return
        }
        if (valor == null || valor <= 0.0) {
            _erro.value = "Informe um valor total válido"
            return
        }
        viewModelScope.launch {
            repository.criarPerfume(grupoId, nomeTratado, marcaTratada, ml, valor, categoria)
        }
    }

    fun editarPerfume(perfume: Perfume, nome: String, marca: String, mlTexto: String, valorTexto: String, categoria: String) {
        val nomeTratado = nome.trim()
        val marcaTratada = marca.trim()
        val ml = mlTexto.trim().toIntOrNull()
        val valor = valorTexto.trim().replace(",", ".").toDoubleOrNull()

        if (nomeTratado.isEmpty()) {
            _erro.value = "Informe o nome do perfume"
            return
        }
        if (ml == null || ml <= 0) {
            _erro.value = "Informe uma quantidade em ml válida"
            return
        }
        if (valor == null || valor <= 0.0) {
            _erro.value = "Informe um valor total válido"
            return
        }
        viewModelScope.launch {
            repository.atualizarPerfume(perfume.copy(nome = nomeTratado, marca = marcaTratada, ml = ml, valorTotal = valor, categoria = categoria))
        }
    }

    fun excluirPerfume(perfume: Perfume) {
        viewModelScope.launch {
            repository.excluirPerfume(perfume)
        }
    }

    fun lancarFrete(valor: Double, data: Long) {
        if (valor <= 0.0) {
            _erro.value = "Informe um valor de frete válido"
            return
        }
        viewModelScope.launch {
            repository.lancarFrete(grupoId, valor, data)
        }
    }

    fun editarFrete(frete: Frete, valor: Double, data: Long) {
        if (valor <= 0.0) {
            _erro.value = "Informe um valor de frete válido"
            return
        }
        viewModelScope.launch {
            repository.atualizarFrete(frete.copy(valor = valor, data = data))
        }
    }

    fun excluirFrete(frete: Frete) {
        viewModelScope.launch {
            repository.excluirFrete(frete)
        }
    }

    fun editarGrupo(novoNome: String) {
        val nomeTratado = novoNome.trim()
        if (nomeTratado.isEmpty()) {
            _erro.value = "Informe um nome para o grupo"
            return
        }
        val grupoAtual = uiState.value.grupo ?: return
        viewModelScope.launch {
            repository.atualizarGrupo(grupoAtual.copy(nome = nomeTratado))
        }
    }

    fun excluirGrupo() {
        val grupoAtual = uiState.value.grupo ?: return
        viewModelScope.launch {
            repository.excluirGrupo(grupoAtual)
            _grupoExcluido.value = true
        }
    }

    fun limparErro() {
        _erro.value = null
    }
}
