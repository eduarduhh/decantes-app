package com.eduarduhh.decantes.ui.perfumedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import android.widget.Toast
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eduarduhh.decantes.data.entity.Pagamento
import com.eduarduhh.decantes.data.repository.DecantesRepository
import com.eduarduhh.decantes.ui.components.CategoriaIcon
import com.eduarduhh.decantes.ui.components.ConfirmDialog
import com.eduarduhh.decantes.ui.components.PagamentoDialog
import com.eduarduhh.decantes.ui.components.StatusBadge
import com.eduarduhh.decantes.ui.components.formatarData
import com.eduarduhh.decantes.ui.components.formatarMoeda
import com.eduarduhh.decantes.viewmodel.PerfumeDetailViewModel
import com.eduarduhh.decantes.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfumeDetailScreen(
    repository: DecantesRepository,
    perfumeId: Long,
    onVoltar: () -> Unit
) {
    val viewModel: PerfumeDetailViewModel =
        viewModel(factory = ViewModelFactory(repository, perfumeId = perfumeId))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var mostrarDialogoPagamento by remember { mutableStateOf(false) }
    var pagamentoParaEditar by remember { mutableStateOf<Pagamento?>(null) }
    var pagamentoParaExcluir by remember { mutableStateOf<Pagamento?>(null) }
    var mostrarConfirmExcluirPerfume by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.perfumeExcluido) {
        if (uiState.perfumeExcluido) onVoltar()
    }

    val item = uiState.perfumeComPagamentos
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val perfume = item?.perfume
                    val titulo = when {
                        perfume == null -> "Perfume"
                        perfume.marca.isBlank() -> perfume.nome
                        else -> "${perfume.marca} ${perfume.nome}"
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (perfume != null) CategoriaIcon(categoria = perfume.categoria)
                        Text(titulo)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    val perfume = item?.perfume
                    if (perfume != null) {
                        IconButton(onClick = {
                            val texto = if (perfume.marca.isBlank()) {
                                perfume.nome
                            } else {
                                "${perfume.marca} - ${perfume.nome}"
                            }
                            clipboardManager.setText(AnnotatedString(texto))
                            Toast.makeText(context, "Copiado: $texto", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = "Copiar marca e nome")
                        }
                    }
                    IconButton(onClick = { mostrarConfirmExcluirPerfume = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Excluir perfume")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { mostrarDialogoPagamento = true }) {
                Text("Lançar pagamento")
            }
        }
    ) { padding ->
        if (uiState.carregando || item == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${item.perfume.ml} ml", style = MaterialTheme.typography.titleMedium)
                            StatusBadge(status = item.status)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        val progresso = if (item.perfume.valorTotal > 0) {
                            (item.valorPago / item.perfume.valorTotal).toFloat().coerceIn(0f, 1f)
                        } else 0f
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { progresso },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(androidx.compose.foundation.shape.RoundedCornerShape(4.dp)),
                            trackColor = MaterialTheme.colorScheme.surface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Valor total: ${formatarMoeda(item.perfume.valorTotal)}")
                        Text("Valor pago: ${formatarMoeda(item.valorPago)}")
                        Text(
                            "Saldo restante: ${formatarMoeda(item.saldoRestante)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                Text(
                    "Histórico de pagamentos",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                if (item.pagamentos.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.HourglassEmpty,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Nenhum pagamento lançado ainda",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(item.pagamentos.sortedByDescending { it.data }, key = { it.id }) { pagamento ->
                            PagamentoItem(
                                pagamento = pagamento,
                                onEditar = { pagamentoParaEditar = pagamento },
                                onExcluir = { pagamentoParaExcluir = pagamento }
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoPagamento) {
        PagamentoDialog(
            titulo = "Lançar pagamento",
            onDismiss = { mostrarDialogoPagamento = false },
            onConfirmar = { valor, data ->
                viewModel.lancarPagamento(valor, data)
                mostrarDialogoPagamento = false
            }
        )
    }

    pagamentoParaEditar?.let { pagamento ->
        PagamentoDialog(
            titulo = "Editar pagamento",
            valorInicial = pagamento.valor,
            dataInicialMillis = pagamento.data,
            onDismiss = { pagamentoParaEditar = null },
            onConfirmar = { valor, data ->
                viewModel.editarPagamento(pagamento, valor, data)
                pagamentoParaEditar = null
            }
        )
    }

    pagamentoParaExcluir?.let { pagamento ->
        ConfirmDialog(
            titulo = "Excluir pagamento",
            mensagem = "Tem certeza que deseja excluir o pagamento de ${formatarMoeda(pagamento.valor)} em ${formatarData(pagamento.data)}?",
            textoConfirmar = "Excluir",
            onDismiss = { pagamentoParaExcluir = null },
            onConfirmar = {
                viewModel.excluirPagamento(pagamento)
                pagamentoParaExcluir = null
            }
        )
    }

    if (mostrarConfirmExcluirPerfume) {
        ConfirmDialog(
            titulo = "Excluir perfume",
            mensagem = "Tem certeza que deseja excluir este perfume e todos os pagamentos lançados nele?",
            textoConfirmar = "Excluir",
            onDismiss = { mostrarConfirmExcluirPerfume = false },
            onConfirmar = {
                viewModel.excluirPerfume()
                mostrarConfirmExcluirPerfume = false
            }
        )
    }
}

@Composable
private fun PagamentoItem(
    pagamento: Pagamento,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(formatarMoeda(pagamento.valor), style = MaterialTheme.typography.titleSmall)
                Text(formatarData(pagamento.data), style = MaterialTheme.typography.bodySmall)
            }
            Row {
                androidx.compose.material3.TextButton(onClick = onEditar) { Text("Editar") }
                androidx.compose.material3.TextButton(onClick = onExcluir) { Text("Excluir") }
            }
        }
    }
}
