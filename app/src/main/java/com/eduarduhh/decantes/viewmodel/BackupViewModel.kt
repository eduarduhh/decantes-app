package com.eduarduhh.decantes.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduarduhh.decantes.data.repository.BackupArquivo
import com.eduarduhh.decantes.data.repository.DecantesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BackupMensagem {
    data class Sucesso(val texto: String) : BackupMensagem()
    data class Erro(val texto: String) : BackupMensagem()
}

data class BackupUiState(
    val processando: Boolean = false,
    val mensagem: BackupMensagem? = null,
    val backupsEmDownloads: List<BackupArquivo> = emptyList()
)

class BackupViewModel(private val repository: DecantesRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    init {
        carregarBackups()
    }

    fun carregarBackups() {
        viewModelScope.launch {
            val lista = repository.listarBackupsEmDownloads()
            _uiState.value = _uiState.value.copy(backupsEmDownloads = lista)
        }
    }

    fun exportar() {
        _uiState.value = _uiState.value.copy(processando = true, mensagem = null)
        viewModelScope.launch {
            try {
                val nomeArquivo = repository.exportarParaDownloads()
                _uiState.value = _uiState.value.copy(
                    processando = false,
                    mensagem = BackupMensagem.Sucesso("Backup salvo em Downloads/$nomeArquivo")
                )
                carregarBackups()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    processando = false,
                    mensagem = BackupMensagem.Erro("Erro ao exportar: ${e.message ?: "desconhecido"}")
                )
            }
        }
    }

    fun restaurar(uri: Uri) {
        _uiState.value = _uiState.value.copy(processando = true, mensagem = null)
        viewModelScope.launch {
            try {
                repository.restaurarDeUri(uri)
                _uiState.value = _uiState.value.copy(
                    processando = false,
                    mensagem = BackupMensagem.Sucesso("Dados restaurados com sucesso")
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    processando = false,
                    mensagem = BackupMensagem.Erro("Erro ao restaurar: ${e.message ?: "arquivo inválido"}")
                )
            }
        }
    }

    fun limparMensagem() {
        _uiState.value = _uiState.value.copy(mensagem = null)
    }
}
