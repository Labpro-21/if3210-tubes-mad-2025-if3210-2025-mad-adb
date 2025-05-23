package com.example.adbpurrytify.di

import android.content.Context
import androidx.room.Room
import com.example.adbpurrytify.data.local.AppDatabase
import com.example.adbpurrytify.data.local.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideSongDao(database: AppDatabase): SongDao {
        return database.songDao()
    }
}