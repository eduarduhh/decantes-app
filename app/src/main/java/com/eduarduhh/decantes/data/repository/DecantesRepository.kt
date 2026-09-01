package com.eduarduhh.decantes.data.repository

import android.content.Context
import android.net.Uri
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
    suspend fun exportarParaUri(uri: Uri) {
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

        context.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(conteudo.toByteArray(Charsets.UTF_8))
        } ?: throw IllegalStateException("Não foi possível abrir o arquivo para escrita")
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
