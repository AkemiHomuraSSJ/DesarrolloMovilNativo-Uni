package com.example.redsociallocal

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "publicaciones")
data class Publicacion(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val titulo: String,
    val descripcion: String,
    val fotoUri: String,
    val fecha: Long
)

@Dao
interface PublicacionDao {
    @Insert
    suspend fun insertar(publicacion: Publicacion)

    @Query("SELECT * FROM publicaciones ORDER BY fecha DESC")
    fun obtenerTodas(): Flow<List<Publicacion>>
}

@Database(entities = [Publicacion::class], version = 1, exportSchema = false)
abstract class BaseDatos : RoomDatabase() {

    abstract fun publicacionDao(): PublicacionDao

    companion object {
        @Volatile
        private var instancia: BaseDatos? = null

        fun obtener(contexto: Context): BaseDatos {
            return instancia ?: synchronized(this) {
                val nueva = Room.databaseBuilder(
                    contexto.applicationContext,
                    BaseDatos::class.java,
                    "red_social.db"
                ).build()
                instancia = nueva
                nueva
            }
        }
    }
}