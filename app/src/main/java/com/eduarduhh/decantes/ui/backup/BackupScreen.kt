package com.eduarduhh.decantes.ui.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eduarduhh.decantes.data.repository.DecantesRepository
import com.eduarduhh.decantes.ui.components.ConfirmDialog
import com.eduarduhh.decantes.viewmodel.BackupMensagem
import com.eduarduhh.decantes.viewmodel.BackupViewModel
import com.eduarduhh.decantes.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    repository: DecantesRepository,
    onVoltar: () -> Unit
) {
    val viewModel: BackupViewModel = viewModel(factory = ViewModelFactory(repository))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var mostrarConfirmRestore by remember { mutableStateOf(false) }
    var uriRestoreSelecionado by remember { mutableStateOf<android.net.Uri?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val escopo = rememberCoroutineScope()

    val nomeArquivoSugerido = remember {
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale("pt", "BR"))
        "perfumes_backup_${formato.format(Date())}.json"
    }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportar(it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            uriRestoreSelecionado = uri
            mostrarConfirmRestore = true
        }
    }

    LaunchedEffect(uiState.mensagem) {
        val mensagem = uiState.mensagem
        if (mensagem != null) {
            val texto = when (mensagem) {
                is BackupMensagem.Sucesso -> mensagem.texto
                is BackupMensagem.Erro -> mensagem.texto
            }
            escopo.launch { snackbarHostState.showSnackbar(texto) }
            viewModel.limparMensagem()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup e Restauração") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text("Backup", style = MaterialTheme.typography.titleMedium)
            Text(
                "Exporta todos os grupos, perfumes e pagamentos para um arquivo JSON que você pode salvar onde quiser.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
            )
            Button(
                onClick = { exportLauncher.launch(nomeArquivoSugerido) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.processando
            ) {
                Text("Exportar backup")
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Restaurar", style = MaterialTheme.typography.titleMedium)
            Text(
                "Importa um arquivo de backup JSON. Isso substitui todos os dados atuais do app.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 12.dp, top = 4.dp)
            )
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.processando
            ) {
                Text("Selecionar arquivo de backup")
            }

            if (uiState.processando) {
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(modifier = Modifier.padding(top = 16.dp))
            }
        }
    }

    if (mostrarConfirmRestore) {
        ConfirmDialog(
            titulo = "Restaurar backup",
            mensagem = "Isso vai substituir todos os dados atuais do app pelos dados do arquivo selecionado. Essa ação não pode ser desfeita. Deseja continuar?",
            textoConfirmar = "Restaurar",
            onDismiss = {
                mostrarConfirmRestore = false
                uriRestoreSelecionado = null
            },
            onConfirmar = {
                uriRestoreSelecionado?.let { viewModel.restaurar(it) }
                mostrarConfirmRestore = false
                uriRestoreSelecionado = null
            }
        )
    }
}
