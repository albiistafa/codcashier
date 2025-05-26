package dev.codcow.kasirku.core.data.di

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.codcow.kasirku.core.data.remote.DepositApiService
import dev.codcow.kasirku.core.data.remote.KategoriApiService
import dev.codcow.kasirku.core.data.remote.LoginApiService
import dev.codcow.kasirku.core.data.repository.LoginRepoImpl
import dev.codcow.kasirku.core.domain.repository.LoginRepository
import dev.codcow.kasirku.core.data.remote.MenuApiService
import dev.codcow.kasirku.core.data.remote.PegawaiApiService
import dev.codcow.kasirku.core.data.remote.SubkategoriApiService
import dev.codcow.kasirku.core.data.remote.TransaksiApiService
import dev.codcow.kasirku.core.data.remote.WarungApiService
import dev.codcow.kasirku.core.data.repository.DepositRepoImpl
import dev.codcow.kasirku.core.data.repository.KategoriRepoImpl
import dev.codcow.kasirku.core.data.repository.MenuRepoImpl
import dev.codcow.kasirku.core.data.repository.PegawaiRepoImpl
import dev.codcow.kasirku.core.data.repository.SubKategoriIRepoImpl
import dev.codcow.kasirku.core.data.repository.TokenManagerImpl
import dev.codcow.kasirku.core.data.repository.TransaksiRepoImpl
import dev.codcow.kasirku.core.data.repository.WarungRepoImpl
import dev.codcow.kasirku.core.domain.repository.DepositRepository
import dev.codcow.kasirku.core.domain.repository.KategoriRepository
import dev.codcow.kasirku.core.domain.repository.PegawaiRepository
import dev.codcow.kasirku.core.domain.repository.SubKategoriRepository
import dev.codcow.kasirku.core.domain.repository.TokenRepository
import dev.codcow.kasirku.core.domain.repository.TransaksiRepository
import dev.codcow.kasirku.core.domain.repository.WarungRepository
import dev.codcow.kasirku.data.repository.MenuRepository
import dev.codcow.kasirku.features.warung.ui.screens.WarungPreferences
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import okhttp3.logging.HttpLoggingInterceptor


const val BASE_URL = "https://e-cashier-prod-production.up.railway.app/"
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@InstallIn(SingletonComponent::class)
@Module
object DataModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext appContext: Context): Context {
        return appContext
    }

    @Module
    @InstallIn(SingletonComponent::class)
    object SharedPreferencesModule {

        private const val PREFERENCES_NAME = "kasirku_preferences"

        @Provides
        @Singleton
        fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
            return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        }
    }

    @Provides
    @Singleton
    fun provideWarungPreferences(@ApplicationContext context: Context): WarungPreferences {
        return WarungPreferences(context)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS) // Contoh: 30 detik untuk connect
            .readTimeout(60, TimeUnit.SECONDS)    // Contoh: 60 detik untuk read
            .writeTimeout(60, TimeUnit.SECONDS)   // Contoh: 30 detik untuk write
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder().baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    fun provideLoginApiService(retrofit: Retrofit): LoginApiService {
        return retrofit.create(LoginApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideTokenManager(dataStore: DataStore<Preferences>): TokenRepository {
        return TokenManagerImpl(dataStore)
    }

    @Provides
    fun provideLoginRepo(
        loginApiService: LoginApiService,
        tokenManager: TokenRepository
    ): LoginRepository {
        return LoginRepoImpl(loginApiService, tokenManager)
    }

    @Provides
    fun provideMenuApiService(retrofit: Retrofit): MenuApiService {
        return retrofit.create(MenuApiService::class.java)
    }

    @Provides
    fun provideMenuRepo(
        menuApiService: MenuApiService,
        tokenManager: TokenRepository
    ): MenuRepository {
        return MenuRepoImpl(menuApiService, tokenManager)
    }

    @Provides
    fun provideKategoriApiService(retrofit: Retrofit): KategoriApiService {
        return retrofit.create(KategoriApiService::class.java)
    }

    @Provides
    fun provideKategoriRepo(
        kategoriApiService: KategoriApiService,
        tokenManager: TokenRepository
    ): KategoriRepository {
        return KategoriRepoImpl(kategoriApiService, tokenManager)
    }

    @Provides
    fun provideSubKategoriApiService(retrofit: Retrofit): SubkategoriApiService {
        return retrofit.create(SubkategoriApiService::class.java)
    }

    @Provides
    fun provideSubKategoriRepo(
        subkategoriApiService: SubkategoriApiService,
        tokenManager: TokenRepository
    ): SubKategoriRepository {
        return SubKategoriIRepoImpl(subkategoriApiService, tokenManager)
    }

    @Provides
    fun provideTransaksiApiService(retrofit: Retrofit): TransaksiApiService {
        return retrofit.create(TransaksiApiService::class.java)
    }

    @Provides
    fun provideTransaksiRepo(
        transaksiApiService: TransaksiApiService,
        tokenManager: TokenRepository
    ): TransaksiRepository {
        return TransaksiRepoImpl(transaksiApiService, tokenManager)
    }

    @Provides
    fun provideDepositApiService(retrofit: Retrofit): DepositApiService {
        return retrofit.create(DepositApiService::class.java)
    }

    @Provides
    fun provideDepositRepo(
        depositApiService: DepositApiService,
        tokenManager: TokenRepository
    ): DepositRepository {
        return DepositRepoImpl(depositApiService, tokenManager)
    }

    @Provides
    fun provideWarungApiService(retrofit: Retrofit): WarungApiService {
        return retrofit.create(WarungApiService::class.java)
    }

    @Provides
    fun provideWarungRepo(
        warungApiService: WarungApiService,
        tokenManager: TokenRepository
    ): WarungRepository {
        return WarungRepoImpl(warungApiService, tokenManager)
    }

    @Provides
    fun providePegawaiApiService(retrofit: Retrofit): PegawaiApiService {
        return retrofit.create(PegawaiApiService::class.java)
    }

    @Provides
    fun providePegawaiRepo(
        pegawaiApiService: PegawaiApiService,
        tokenManager: TokenRepository
    ): PegawaiRepository {
        return PegawaiRepoImpl(pegawaiApiService, tokenManager)
    }

}

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return context.dataStore
    }
}