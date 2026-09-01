package com.eduarduhh.decantes.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val mensagem: BackupMensagem? = null
)

class BackupViewModel(private val repository: DecantesRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    fun exportar(uri: Uri) {
        _uiState.value = BackupUiState(processando = true)
        viewModelScope.launch {
            try {
                repository.exportarParaUri(uri)
                _uiState.value = BackupUiState(
                    mensagem = BackupMensagem.Sucesso("Backup exportado com sucesso")
                )
            } catch (e: Exception) {
                _uiState.value = BackupUiState(
                    mensagem = BackupMensagem.Erro("Erro ao exportar: ${e.message ?: "desconhecido"}")
                )
            }
        }
    }

    fun restaurar(uri: Uri) {
        _uiState.value = BackupUiState(processando = true)
        viewModelScope.launch {
            try {
                repository.restaurarDeUri(uri)
                _uiState.value = BackupUiState(
                    mensagem = BackupMensagem.Sucesso("Dados restaurados com sucesso")
                )
            } catch (e: Exception) {
                _uiState.value = BackupUiState(
                    mensagem = BackupMensagem.Erro("Erro ao restaurar: ${e.message ?: "arquivo inválido"}")
                )
            }
        }
    }

    fun limparMensagem() {
        _uiState.value = _uiState.value.copy(mensagem = null)
    }
}
