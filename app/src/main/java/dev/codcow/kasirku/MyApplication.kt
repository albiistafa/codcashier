package dev.codcow.kasirku

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import dev.codcow.kasirku.core.data.di.BASE_URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {

    // Lazy so building the client (and Gson, TLS setup) never runs on the main thread at launch
    @Inject lateinit var http: dagger.Lazy<OkHttpClient>

    override fun onCreate() {
        super.onCreate()
        // Open the HTTPS connection while the splash plays, so the first menu/beranda load skips
        // DNS + TLS. OkHttp keeps it pooled for 5 minutes; any failure just means a normal cold call.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching { http.get().newCall(Request.Builder().url(BASE_URL).head().build()).execute().close() }
        }
    }
}
