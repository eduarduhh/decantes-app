package com.eduarduhh.decantes.data.repository

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.eduarduhh.decantes.data.AppDatabase
import com.eduarduhh.decantes.data.backup.BackupData
import com.eduarduhh.decantes.data.backup.BackupFrete
import com.eduarduhh.decantes.data.backup.BackupGrupo
import com.eduarduhh.decantes.data.backup.BackupPagamento
import com.eduarduhh.decantes.data.backup.BackupPerfume
import com.eduarduhh.decantes.data.entity.Frete
import com.eduarduhh.decantes.data.entity.Grupo
import com.eduarduhh.decantes.data.entity.Pagamento
import com.eduarduhh.decantes.data.entity.Perfume
import com.eduarduhh.decantes.data.relation.GrupoComPerfumes
import com.eduarduhh.decantes.data.relation.PerfumeComPagamentos
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupArquivo(
    val uri: Uri,
    val nome: String,
    val dataModificacao: Long
)

class DecantesRepository(
    private val context: Context,
    private val db: AppDatabase
) {
    private val grupoDao = db.grupoDao()
    private val perfumeDao = db.perfumeDao()
    private val pagamentoDao = db.pagamentoDao()
    private val freteDao = db.freteDao()

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    // Grupos
    fun observarGruposComPerfumes(): Flow<List<GrupoComPerfumes>> =
        grupoDao.observarGruposComPerfumes()

    fun observarGrupoComPerfumes(grupoId: Long): Flow<GrupoComPerfumes?> =
        grupoDao.observarGrupoComPerfumes(grupoId)

    suspend fun criarGrupo(nome: String): Long = grupoDao.inserir(Grupo(nome = nome))

    suspend fun atualizarGrupo(grupo: Grupo) = grupoDao.atualizar(grupo)

    suspend fun excluirGrupo(grupo: Grupo) = grupoDao.excluir(grupo)

    // Perfumes
    fun observarPerfumesComPagamentosDoGrupo(grupoId: Long): Flow<List<PerfumeComPagamentos>> =
        perfumeDao.observarPerfumesComPagamentosDoGrupo(grupoId)

    fun observarTodosPerfumesComPagamentos(): Flow<List<PerfumeComPagamentos>> =
        perfumeDao.observarTodosComPagamentos()

    fun observarPerfumeComPagamentos(perfumeId: Long): Flow<PerfumeComPagamentos?> =
        perfumeDao.observarPerfumeComPagamentos(perfumeId)

    fun observarMarcas(): Flow<List<String>> = perfumeDao.observarMarcas()

    suspend fun criarPerfume(grupoId: Long, nome: String, marca: String, ml: Int, valorTotal: Double, categoria: String): Long =
        perfumeDao.inserir(Perfume(grupoId = grupoId, nome = nome, marca = marca, ml = ml, valorTotal = valorTotal, categoria = categoria))

    suspend fun atualizarPerfume(perfume: Perfume) = perfumeDao.atualizar(perfume)

    suspend fun excluirPerfume(perfume: Perfume) = perfumeDao.excluir(perfume)

    // Pagamentos
    suspend fun lancarPagamento(perfumeId: Long, valor: Double, data: Long): Long =
        pagamentoDao.inserir(Pagamento(perfumeId = perfumeId, valor = valor, data = data))

    suspend fun atualizarPagamento(pagamento: Pagamento) = pagamentoDao.atualizar(pagamento)

    suspend fun excluirPagamento(pagamento: Pagamento) = pagamentoDao.excluir(pagamento)

    // Fretes
    suspend fun lancarFrete(grupoId: Long, valor: Double, data: Long): Long =
        freteDao.inserir(Frete(grupoId = grupoId, valor = valor, data = data))

    suspend fun atualizarFrete(frete: Frete) = freteDao.atualizar(frete)

    suspend fun excluirFrete(frete: Frete) = freteDao.excluir(frete)

    // Backup / Restore
    // Grava direto na pasta Downloads via MediaStore, sem abrir o seletor de
    // arquivos do sistema (SAF): em aparelhos sob pressão de memória, o app
    // pode ser morto pelo Android enquanto o seletor está em primeiro plano,
    // e o callback com o conteúdo a escrever nunca chega de volta — o
    // arquivo fica criado (0 bytes) mas vazio, sem erro nem crash visível.
    // Escrever direto, sem sair do processo do app, elimina essa janela.
    suspend fun exportarParaDownloads(): String {
        val grupos = grupoDao.listarTodos()
        val perfumes = perfumeDao.listarTodos()
        val pagamentos = pagamentoDao.listarTodos()
        val fretes = freteDao.listarTodos()

        val backup = BackupData(
            grupos = grupos.map { BackupGrupo(it.id, it.nome, it.dataCriacao) },
            perfumes = perfumes.map { BackupPerfume(it.id, it.grupoId, it.nome, it.marca, it.ml, it.valorTotal, it.categoria) },
            pagamentos = pagamentos.map { BackupPagamento(it.id, it.perfumeId, it.valor, it.data) },
            fretes = fretes.map { BackupFrete(it.id, it.grupoId, it.valor, it.data) }
        )
        val conteudo = json.encodeToString(BackupData.serializer(), backup)
        val bytes = conteudo.toByteArray(Charsets.UTF_8)
        val formato = SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale("pt", "BR"))
        val nomeArquivo = "perfumes_backup_${formato.format(Date())}.json"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, nomeArquivo)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: throw IllegalStateException("Não foi possível criar o arquivo em Downloads")
            context.contentResolver.openOutputStream(uri)?.use { output ->
                output.write(bytes)
            } ?: throw IllegalStateException("Não foi possível abrir o arquivo para escrita")
        } else {
            @Suppress("DEPRECATION")
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!dir.exists()) dir.mkdirs()
            File(dir, nomeArquivo).writeBytes(bytes)
        }

        return nomeArquivo
    }

    // Lista os backups já salvos em Downloads direto pelo app, sem abrir o
    // seletor de arquivos do sistema (mesma motivação do exportarParaDownloads:
    // evitar o vai-e-volta pra outro app que pode ser interrompido se o
    // Android matar o processo por pressão de memória).
    suspend fun listarBackupsEmDownloads(): List<BackupArquivo> {
        val resultado = mutableListOf<BackupArquivo>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val colecao = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            val projecao = arrayOf(
                MediaStore.MediaColumns._ID,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.DATE_MODIFIED
            )
            context.contentResolver.query(
                colecao,
                projecao,
                "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?",
                arrayOf("perfumes_backup_%.json"),
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nomeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val dataCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                while (cursor.moveToNext()) {
                    val uri = ContentUris.withAppendedId(colecao, cursor.getLong(idCol))
                    resultado.add(
                        BackupArquivo(
                            uri = uri,
                            nome = cursor.getString(nomeCol),
                            dataModificacao = cursor.getLong(dataCol) * 1000
                        )
                    )
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            dir.listFiles { f -> f.name.startsWith("perfumes_backup_") && f.name.endsWith(".json") }
                ?.sortedByDescending { it.lastModified() }
                ?.forEach { resultado.add(BackupArquivo(Uri.fromFile(it), it.name, it.lastModified())) }
        }

        return resultado
    }

    suspend fun restaurarDeUri(uri: Uri) {
        val conteudo = context.contentResolver.openInputStream(uri)?.use { input ->
            input.readBytes().toString(Charsets.UTF_8)
        } ?: throw IllegalStateException("Não foi possível abrir o arquivo para leitura")

        val backup = json.decodeFromString(BackupData.serializer(), conteudo)

        db.withTransaction {
            db.clearAllTables()
            backup.grupos.forEach {
                grupoDao.inserir(Grupo(id = it.id, nome = it.nome, dataCriacao = it.dataCriacao))
            }
            backup.perfumes.forEach {
                perfumeDao.inserir(
                    Perfume(id = it.id, grupoId = it.grupoId, nome = it.nome, marca = it.marca, ml = it.ml, valorTotal = it.valorTotal, categoria = it.categoria)
                )
            }
            backup.pagamentos.forEach {
                pagamentoDao.inserir(
                    Pagamento(id = it.id, perfumeId = it.perfumeId, valor = it.valor, data = it.data)
                )
            }
            backup.fretes.forEach {
                freteDao.inserir(
                    Frete(id = it.id, grupoId = it.grupoId, valor = it.valor, data = it.data)
                )
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: DecantesRepository? = null

        fun getInstance(context: Context): DecantesRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DecantesRepository(
                    context.applicationContext,
                    AppDatabase.getInstance(context)
                ).also { INSTANCE = it }
            }
        }
    }
}
