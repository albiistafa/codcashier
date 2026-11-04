package dev.codcow.kasirku.core.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The last list the server sent (menus, categories, subcategories), kept on this phone so Kasir
 * opens instantly and still works on a bad connection. A plain JSON file per list in the app's
 * cache folder: Room stays untouched (no migration near the QRIS data), and the OS may clear it,
 * which only means one network load. Cleared on login and logout so one warung never sees another's.
 */
@Singleton
class ListCache(private val dir: File, private val io: CoroutineDispatcher = Dispatchers.IO) {
    @Inject constructor(@ApplicationContext context: Context) : this(File(context.cacheDir, "lists"))

    private val gson = Gson()

    suspend inline fun <reified T> read(key: String): List<T>? =
        read(key, TypeToken.getParameterized(List::class.java, T::class.java).type)

    suspend fun <T> read(key: String, type: java.lang.reflect.Type): List<T>? = withContext(io) {
        // A missing or unreadable file is just "no copy yet": the caller goes to the server
        runCatching { File(dir, "$key.json").takeIf { it.exists() }?.reader()?.use { gson.fromJson<List<T>>(it, type) } }.getOrNull()
    }

    suspend fun write(key: String, list: List<Any>) = withContext(io) {
        runCatching {
            dir.mkdirs()
            // Write then rename, so a crash mid-write never leaves half a menu behind
            val tmp = File(dir, "$key.tmp").apply { writeText(gson.toJson(list)) }
            tmp.renameTo(File(dir, "$key.json"))
        }
        Unit
    }

    fun clear() {
        dir.deleteRecursively()
    }

    companion object {
        const val MENUS = "menus"
        const val CATEGORIES = "categories"
        const val SUBCATEGORIES = "subcategories"
    }
}
