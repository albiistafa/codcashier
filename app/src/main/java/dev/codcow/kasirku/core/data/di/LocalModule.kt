package dev.codcow.kasirku.core.data.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.codcow.kasirku.core.data.local.KasirkuDatabase
import dev.codcow.kasirku.core.data.local.QrisDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LocalModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): KasirkuDatabase =
        Room.databaseBuilder(context, KasirkuDatabase::class.java, "kasirku.db").build()

    @Provides
    fun provideQrisDao(db: KasirkuDatabase): QrisDao = db.qrisDao()
}
