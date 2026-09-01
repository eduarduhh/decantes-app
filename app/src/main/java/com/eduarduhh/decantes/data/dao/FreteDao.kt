package com.eduarduhh.decantes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.eduarduhh.decantes.data.entity.Frete

@Dao
interface FreteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(frete: Frete): Long

    @Update
    suspend fun atualizar(frete: Frete)

    @Delete
    suspend fun excluir(frete: Frete)

    @Query("SELECT * FROM fretes")
    suspend fun listarTodos(): List<Frete>
}
