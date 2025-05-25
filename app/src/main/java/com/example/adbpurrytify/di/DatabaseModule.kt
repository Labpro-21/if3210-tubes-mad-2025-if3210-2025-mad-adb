package com.example.adbpurrytify.di

import android.content.Context
import androidx.room.Room
import com.example.adbpurrytify.data.local.AnalyticsDao
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
    fun provideAppDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "app_database"
        )
            .fallbackToDestructiveMigration() // Remove in production
            .build()
    }

    @Provides
    fun provideSongDao(appDatabase: AppDatabase): SongDao {
        return appDatabase.songDao()
    }

    @Provides
    fun provideAnalyticsDao(appDatabase: AppDatabase): AnalyticsDao {
        return appDatabase.analyticsDao()
    }
}