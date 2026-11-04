package dev.codcow.kasirku.core.data.local

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/** The warung's one QRIS image. Single row (id = 1); the picture itself lives in the app's files folder. */
@Entity(tableName = "qris")
data class QrisEntity(
    @PrimaryKey val id: Int = 1,
    val path: String,
    val updatedAt: Long
)

@Dao
interface QrisDao {
    @Query("SELECT * FROM qris WHERE id = 1")
    fun observe(): Flow<QrisEntity?>

    @Query("SELECT * FROM qris WHERE id = 1")
    suspend fun get(): QrisEntity?

    @Upsert
    suspend fun upsert(qris: QrisEntity)

    @Query("DELETE FROM qris")
    suspend fun clear()
}

@Database(entities = [QrisEntity::class], version = 1, exportSchema = false)
abstract class KasirkuDatabase : RoomDatabase() {
    abstract fun qrisDao(): QrisDao
}

/**
 * Keeps the QRIS on this phone only (decision 19 Okt). Server sync is not built yet,
 * so a QRIS uploaded here does not appear on a pegawai's phone.
 */
@Singleton
class QrisStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: QrisDao
) {
    /** Path of the saved QRIS image, or null when none is saved (or its file went missing). */
    val path: Flow<String?> = dao.observe().map { row -> row?.path?.takeIf { File(it).exists() } }

    /** Copies the picked image into app storage. Throws [NotAnImageException] when it can't be read as a picture. */
    suspend fun save(uri: Uri) = withContext(Dispatchers.IO) {
        // A new file name each time, so image caches never show the previous QRIS
        val file = File(context.filesDir, "qris_${System.currentTimeMillis()}.img")
        context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { input.copyTo(it) }
        } ?: throw NotAnImageException()
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            file.delete()
            throw NotAnImageException()
        }
        val old = dao.get()
        dao.upsert(QrisEntity(path = file.absolutePath, updatedAt = System.currentTimeMillis()))
        old?.let { File(it.path).delete() }
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        dao.get()?.let { File(it.path).delete() }
        dao.clear()
    }
}

class NotAnImageException : Exception("Not an image")
