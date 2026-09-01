package com.eduarduhh.decantes.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.eduarduhh.decantes.data.entity.Pagamento

@Dao
interface PagamentoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserir(pagamento: Pagamento): Long

    @Update
    suspend fun atualizar(pagamento: Pagamento)

    @Delete
    suspend fun excluir(pagamento: Pagamento)

    @Query("SELECT * FROM pagamentos")
    suspend fun listarTodos(): List<Pagamento>
}
