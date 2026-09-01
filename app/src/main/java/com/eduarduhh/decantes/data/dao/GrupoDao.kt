package com.eduarduhh.decantes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.eduarduhh.decantes.data.entity.Grupo
import com.eduarduhh.decantes.data.relation.GrupoComPerfumes
import kotlinx.coroutines.flow.Flow

@Dao
interface GrupoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(grupo: Grupo): Long

    @Update
    suspend fun atualizar(grupo: Grupo)

    @Delete
    suspend fun excluir(grupo: Grupo)

    @Transaction
    @Query("SELECT * FROM grupos ORDER BY dataCriacao DESC")
    fun observarGruposComPerfumes(): Flow<List<GrupoComPerfumes>>

    @Transaction
    @Query("SELECT * FROM grupos WHERE id = :grupoId")
    fun observarGrupoComPerfumes(grupoId: Long): Flow<GrupoComPerfumes?>

    @Query("SELECT * FROM grupos ORDER BY dataCriacao DESC")
    suspend fun listarTodos(): List<Grupo>

    @Query("SELECT * FROM grupos WHERE id = :grupoId")
    suspend fun buscarPorId(grupoId: Long): Grupo?
}
