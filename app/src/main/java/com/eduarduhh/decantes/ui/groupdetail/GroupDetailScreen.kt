package com.eduarduhh.decantes.ui.groupdetail

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalFlorist
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.eduarduhh.decantes.data.entity.Frete
import com.eduarduhh.decantes.data.entity.Perfume
import com.eduarduhh.decantes.data.relation.PerfumeComPagamentos
import com.eduarduhh.decantes.data.repository.DecantesRepository
import com.eduarduhh.decantes.ui.components.CATEGORIAS_PERFUME
import com.eduarduhh.decantes.ui.components.CategoriaIcon
import com.eduarduhh.decantes.ui.components.ConfirmDialog
import com.eduarduhh.decantes.ui.components.PagamentoDialog
import com.eduarduhh.decantes.ui.components.PerfumeFormDialog
import com.eduarduhh.decantes.ui.components.StatusBadge
import com.eduarduhh.decantes.ui.components.formatarData
import com.eduarduhh.decantes.ui.components.formatarMoeda
import com.eduarduhh.decantes.ui.components.formatarMoedaOuOculto
import com.eduarduhh.decantes.viewmodel.GroupDetailViewModel
import com.eduarduhh.decantes.viewmodel.OrdenacaoPerfume
import com.eduarduhh.decantes.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    repository: DecantesRepository,
    grupoId: Long,
    onVoltar: () -> Unit,
    onAbrirPerfume: (Long) -> Unit
) {
    val viewModel: GroupDetailViewModel =
        viewModel(factory = ViewModelFactory(repository, grupoId = grupoId))
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var mostrarDialogoNovoPerfume by remember { mutableStateOf(false) }
    var mostrarDialogoEditarGrupo by remember { mutableStateOf(false) }
    var mostrarDialogoFrete by remember { mutableStateOf(false) }
    var mostrarConfirmExcluirGrupo by remember { mutableStateOf(false) }
    var perfumeParaEditar by remember { mutableStateOf<Perfume?>(null) }
    var perfumeParaExcluir by remember { mutableStateOf<Perfume?>(null) }
    var freteParaEditar by remember { mutableStateOf<Frete?>(null) }
    var freteParaExcluir by remember { mutableStateOf<Frete?>(null) }
    var menuAberto by remember { mutableStateOf(false) }
    var valoresOcultos by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.grupoExcluido) {
        if (uiState.grupoExcluido) onVoltar()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.grupo?.nome ?: "Grupo") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { menuAberto = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Opções")
                    }
                    DropdownMenu(expanded = menuAberto, onDismissRequest = { menuAberto = false }) {
                        DropdownMenuItem(
                            text = { Text("Editar grupo") },
                            onClick = {
                                menuAberto = false
                                mostrarDialogoEditarGrupo = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Lançar frete") },
                            onClick = {
                                menuAberto = false
                                mostrarDialogoFrete = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Excluir grupo") },
                            onClick = {
                                menuAberto = false
                                mostrarConfirmExcluirGrupo = true
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { mostrarDialogoNovoPerfume = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Novo perfume")
            }
        }
    ) { padding ->
        if (uiState.carregando) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.totalPerfumesCadastrados == 0) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.LocalFlorist,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Nenhum perfume cadastrado",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Toque em + para adicionar o primeiro",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                GrupoTotalizador(
                    totalPago = uiState.totalPagoGrupo,
                    totalGrupo = uiState.totalGrupo,
                    saldoGrupo = uiState.saldoGrupo,
                    frete = uiState.totalFrete,
                    valoresOcultos = valoresOcultos,
                    onAlternarValoresOcultos = { valoresOcultos = !valoresOcultos },
                    onLancarFrete = { mostrarDialogoFrete = true }
                )
                CategoriaTotalizador(quantidadePorCategoria = uiState.quantidadePorCategoria)
                if (uiState.fretes.isNotEmpty()) {
                    FretesSection(
                        fretes = uiState.fretes,
                        onEditar = { freteParaEditar = it },
                        onExcluir = { freteParaExcluir = it }
                    )
                }
                FiltroEOrdenacao(
                    ordenacao = uiState.ordenacao,
                    mostrarQuitados = uiState.mostrarQuitados,
                    termoBusca = uiState.termoBusca,
                    onMudarOrdenacao = viewModel::mudarOrdenacao,
                    onAlternarMostrarQuitados = viewModel::alternarMostrarQuitados,
                    onMudarTermoBusca = viewModel::mudarTermoBusca
                )
                if (uiState.perfumes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Nenhum perfume encontrado com esse filtro",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        items(uiState.perfumes, key = { it.perfume.id }) { item ->
                            PerfumeItem(
                                item = item,
                                onClick = { onAbrirPerfume(item.perfume.id) },
                                onEditar = { perfumeParaEditar = item.perfume },
                                onExcluir = { perfumeParaExcluir = item.perfume }
                            )
                        }
                    }
                }
            }
        }
    }

    if (mostrarDialogoNovoPerfume) {
        PerfumeFormDialog(
            titulo = "Novo perfume",
            marcasSugeridas = uiState.marcasSugeridas,
            onDismiss = { mostrarDialogoNovoPerfume = false },
            onConfirmar = { nome, marca, ml, valor, categoria ->
                viewModel.adicionarPerfume(nome, marca, ml, valor, categoria)
                mostrarDialogoNovoPerfume = false
            }
        )
    }

    perfumeParaEditar?.let { perfume ->
        PerfumeFormDialog(
            titulo = "Editar perfume",
            nomeInicial = perfume.nome,
            marcaInicial = perfume.marca,
            mlInicial = perfume.ml.toString(),
            valorInicial = "%.2f".format(perfume.valorTotal),
            categoriaInicial = perfume.categoria,
            marcasSugeridas = uiState.marcasSugeridas,
            onDismiss = { perfumeParaEditar = null },
            onConfirmar = { nome, marca, ml, valor, categoria ->
                viewModel.editarPerfume(perfume, nome, marca, ml, valor, categoria)
                perfumeParaEditar = null
            }
        )
    }

    perfumeParaExcluir?.let { perfume ->
        ConfirmDialog(
            titulo = "Excluir perfume",
            mensagem = "Tem certeza que deseja excluir \"${perfume.nome}\"? Os pagamentos lançados também serão excluídos.",
            textoConfirmar = "Excluir",
            onDismiss = { perfumeParaExcluir = null },
            onConfirmar = {
                viewModel.excluirPerfume(perfume)
                perfumeParaExcluir = null
            }
        )
    }

    if (mostrarDialogoEditarGrupo) {
        EditarGrupoDialog(
            nomeInicial = uiState.grupo?.nome.orEmpty(),
            onDismiss = { mostrarDialogoEditarGrupo = false },
            onConfirmar = { novoNome ->
                viewModel.editarGrupo(novoNome)
                mostrarDialogoEditarGrupo = false
            }
        )
    }

    if (mostrarDialogoFrete) {
        PagamentoDialog(
            titulo = "Lançar frete",
            labelValor = "Valor do frete (R$)",
            onDismiss = { mostrarDialogoFrete = false },
            onConfirmar = { valor, data ->
                viewModel.lancarFrete(valor, data)
                mostrarDialogoFrete = false
            }
        )
    }

    freteParaEditar?.let { frete ->
        PagamentoDialog(
            titulo = "Editar frete",
            labelValor = "Valor do frete (R$)",
            valorInicial = frete.valor,
            dataInicialMillis = frete.data,
            onDismiss = { freteParaEditar = null },
            onConfirmar = { valor, data ->
                viewModel.editarFrete(frete, valor, data)
                freteParaEditar = null
            }
        )
    }

    freteParaExcluir?.let { frete ->
        ConfirmDialog(
            titulo = "Excluir frete",
            mensagem = "Tem certeza que deseja excluir o frete de ${formatarMoeda(frete.valor)} em ${formatarData(frete.data)}?",
            textoConfirmar = "Excluir",
            onDismiss = { freteParaExcluir = null },
            onConfirmar = {
                viewModel.excluirFrete(frete)
                freteParaExcluir = null
            }
        )
    }

    if (mostrarConfirmExcluirGrupo) {
        ConfirmDialog(
            titulo = "Excluir grupo",
            mensagem = "Tem certeza que deseja excluir este grupo? Todos os perfumes e pagamentos dele serão excluídos.",
            textoConfirmar = "Excluir",
            onDismiss = { mostrarConfirmExcluirGrupo = false },
            onConfirmar = {
                viewModel.excluirGrupo()
                mostrarConfirmExcluirGrupo = false
            }
        )
    }
}

@Composable
private fun GrupoTotalizador(
    totalPago: Double,
    totalGrupo: Double,
    saldoGrupo: Double,
    frete: Double,
    valoresOcultos: Boolean,
    onAlternarValoresOcultos: () -> Unit,
    onLancarFrete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "${formatarMoedaOuOculto(totalPago, valoresOcultos)} pago de ${formatarMoedaOuOculto(totalGrupo, valoresOcultos)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Saldo do grupo: ${formatarMoedaOuOculto(saldoGrupo, valoresOcultos)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if (frete > 0.0) {
                    Text(
                        "Frete: ${formatarMoedaOuOculto(frete, valoresOcultos)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Row {
                IconButton(onClick = onLancarFrete) {
                    Icon(
                        Icons.Filled.LocalShipping,
                        contentDescription = "Lançar frete",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                IconButton(onClick = onAlternarValoresOcultos) {
                    Icon(
                        if (valoresOcultos) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (valoresOcultos) "Mostrar valores" else "Ocultar valores",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoriaTotalizador(quantidadePorCategoria: Map<String, Int>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 12.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceEvenly
        ) {
            CATEGORIAS_PERFUME.forEach { categoria ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        (quantidadePorCategoria[categoria] ?: 0).toString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        categoria,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FretesSection(
    fretes: List<Frete>,
    onEditar: (Frete) -> Unit,
    onExcluir: (Frete) -> Unit
) {
    Column(modifier = Modifier.padding(top = 12.dp)) {
        Text(
            "Fretes lançados",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            fretes.forEach { frete ->
                FreteItem(
                    frete = frete,
                    onEditar = { onEditar(frete) },
                    onExcluir = { onExcluir(frete) }
                )
            }
        }
    }
}

@Composable
private fun FreteItem(
    frete: Frete,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Column {
                Text(formatarMoeda(frete.valor), style = MaterialTheme.typography.titleSmall)
                Text(formatarData(frete.data), style = MaterialTheme.typography.bodySmall)
            }
            Row {
                TextButton(onClick = onEditar) { Text("Editar") }
                TextButton(
                    onClick = onExcluir,
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Excluir") }
            }
        }
    }
}

@Composable
private fun FiltroEOrdenacao(
    ordenacao: OrdenacaoPerfume,
    mostrarQuitados: Boolean,
    termoBusca: String,
    onMudarOrdenacao: (OrdenacaoPerfume) -> Unit,
    onAlternarMostrarQuitados: () -> Unit,
    onMudarTermoBusca: (String) -> Unit
) {
    var menuOrdenacaoAberto by remember { mutableStateOf(false) }
    var buscaAberta by remember { mutableStateOf(termoBusca.isNotEmpty()) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mostrar quitados", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(checked = mostrarQuitados, onCheckedChange = { onAlternarMostrarQuitados() })
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (buscaAberta) onMudarTermoBusca("")
                    buscaAberta = !buscaAberta
                }) {
                    Icon(
                        if (buscaAberta) Icons.Filled.Close else Icons.Filled.Search,
                        contentDescription = if (buscaAberta) "Fechar busca" else "Buscar por nome"
                    )
                }
                Box {
                    IconButton(onClick = { menuOrdenacaoAberto = true }) {
                        Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Ordenar")
                    }
                    DropdownMenu(expanded = menuOrdenacaoAberto, onDismissRequest = { menuOrdenacaoAberto = false }) {
                        DropdownMenuItem(
                            text = { Text("Nome") },
                            onClick = {
                                onMudarOrdenacao(OrdenacaoPerfume.NOME)
                                menuOrdenacaoAberto = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Valor total") },
                            onClick = {
                                onMudarOrdenacao(OrdenacaoPerfume.VALOR)
                                menuOrdenacaoAberto = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Saldo devedor") },
                            onClick = {
                                onMudarOrdenacao(OrdenacaoPerfume.SALDO)
                                menuOrdenacaoAberto = false
                            }
                        )
                    }
                }
            }
        }

        if (buscaAberta) {
            OutlinedTextField(
                value = termoBusca,
                onValueChange = onMudarTermoBusca,
                label = { Text("Buscar por nome ou marca") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun PerfumeItemPreview() {
    com.eduarduhh.decantes.ui.theme.DecantesTheme {
        PerfumeItem(
            item = PerfumeComPagamentos(
                perfume = Perfume(
                    id = 1,
                    grupoId = 1,
                    nome = "Sauvage",
                    marca = "Dior",
                    ml = 10,
                    valorTotal = 150.0,
                    categoria = "Masculino"
                ),
                pagamentos = listOf(com.eduarduhh.decantes.data.entity.Pagamento(id = 1, perfumeId = 1, valor = 60.0, data = 0))
            ),
            onClick = {},
            onEditar = {},
            onExcluir = {}
        )
    }
}

@Composable
private fun PerfumeItem(
    item: PerfumeComPagamentos,
    onClick: () -> Unit,
    onEditar: () -> Unit,
    onExcluir: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (item.perfume.marca.isNotBlank()) {
                        Text(
                            item.perfume.marca,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
                    ) {
                        CategoriaIcon(categoria = item.perfume.categoria)
                        Text(
                            "${item.perfume.nome} · ${item.perfume.ml}ml",
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
                StatusBadge(status = item.status)
            }
            Spacer(modifier = Modifier.height(10.dp))
            val progresso = if (item.perfume.valorTotal > 0) {
                (item.valorPago / item.perfume.valorTotal).toFloat().coerceIn(0f, 1f)
            } else 0f
            androidx.compose.material3.LinearProgressIndicator(
                progress = { progresso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(3.dp)),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text("Total: ${formatarMoeda(item.perfume.valorTotal)} · Pago: ${formatarMoeda(item.valorPago)}")
            Text("Saldo: ${formatarMoeda(item.saldoRestante)}")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onEditar) { Text("Editar") }
                TextButton(
                    onClick = onExcluir,
                    colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) { Text("Excluir") }
                Spacer(modifier = Modifier.weight(1f))
                val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
                val context = androidx.compose.ui.platform.LocalContext.current
                IconButton(onClick = {
                    val texto = if (item.perfume.marca.isBlank()) {
                        item.perfume.nome
                    } else {
                        "${item.perfume.marca} - ${item.perfume.nome}"
                    }
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(texto))
                    android.widget.Toast.makeText(context, "Copiado: $texto", android.widget.Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copiar marca e nome")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditarGrupoDialog(
    nomeInicial: String,
    onDismiss: () -> Unit,
    onConfirmar: (String) -> Unit
) {
    var nome by rememberSaveable { mutableStateOf(nomeInicial) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar grupo") },
        text = {
            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome do grupo") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirmar(nome) }) { Text("Salvar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

