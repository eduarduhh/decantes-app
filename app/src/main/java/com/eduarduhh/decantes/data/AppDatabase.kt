package com.eduarduhh.decantes.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eduarduhh.decantes.data.dao.FreteDao
import com.eduarduhh.decantes.data.dao.GrupoDao
import com.eduarduhh.decantes.data.dao.PagamentoDao
import com.eduarduhh.decantes.data.dao.PerfumeDao
import com.eduarduhh.decantes.data.entity.Frete
import com.eduarduhh.decantes.data.entity.Grupo
import com.eduarduhh.decantes.data.entity.Pagamento
import com.eduarduhh.decantes.data.entity.Perfume

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE perfumes ADD COLUMN marca TEXT NOT NULL DEFAULT ''")
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE perfumes ADD COLUMN categoria TEXT NOT NULL DEFAULT 'Compartilhável'")
    }
}

private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE grupos ADD COLUMN frete REAL NOT NULL DEFAULT 0.0")
    }
}

private val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS fretes (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "grupoId INTEGER NOT NULL, " +
                "valor REAL NOT NULL, " +
                "data INTEGER NOT NULL, " +
                "FOREIGN KEY(grupoId) REFERENCES grupos(id) ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_fretes_grupoId ON fretes(grupoId)")
        db.execSQL(
            "INSERT INTO fretes (grupoId, valor, data) " +
                "SELECT id, frete, dataCriacao FROM grupos WHERE frete > 0"
        )
        db.execSQL(
            "CREATE TABLE grupos_new (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "nome TEXT NOT NULL, " +
                "dataCriacao INTEGER NOT NULL)"
        )
        db.execSQL("INSERT INTO grupos_new (id, nome, dataCriacao) SELECT id, nome, dataCriacao FROM grupos")
        db.execSQL("DROP TABLE grupos")
        db.execSQL("ALTER TABLE grupos_new RENAME TO grupos")
    }
}

@Database(
    entities = [Grupo::class, Perfume::class, Pagamento::class, Frete::class],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun grupoDao(): GrupoDao
    abstract fun perfumeDao(): PerfumeDao
    abstract fun pagamentoDao(): PagamentoDao
    abstract fun freteDao(): FreteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "decantes.db"
                ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).build().also { INSTANCE = it }
            }
        }
    }
}
