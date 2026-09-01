package com.eduarduhh.decantes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.eduarduhh.decantes.data.entity.Perfume
import com.eduarduhh.decantes.data.relation.PerfumeComPagamentos
import kotlinx.coroutines.flow.Flow

@Dao
interface PerfumeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(perfume: Perfume): Long

    @Update
    suspend fun atualizar(perfume: Perfume)

    @Delete
    suspend fun excluir(perfume: Perfume)

    @Transaction
    @Query("SELECT * FROM perfumes WHERE grupoId = :grupoId ORDER BY id ASC")
    fun observarPerfumesComPagamentosDoGrupo(grupoId: Long): Flow<List<PerfumeComPagamentos>>

    @Transaction
    @Query("SELECT * FROM perfumes ORDER BY id ASC")
    fun observarTodosComPagamentos(): Flow<List<PerfumeComPagamentos>>

    @Transaction
    @Query("SELECT * FROM perfumes WHERE id = :perfumeId")
    fun observarPerfumeComPagamentos(perfumeId: Long): Flow<PerfumeComPagamentos?>

    @Query("SELECT * FROM perfumes")
    suspend fun listarTodos(): List<Perfume>

    @Query("SELECT DISTINCT marca FROM perfumes WHERE marca != '' ORDER BY marca COLLATE NOCASE ASC")
    fun observarMarcas(): Flow<List<String>>
}
