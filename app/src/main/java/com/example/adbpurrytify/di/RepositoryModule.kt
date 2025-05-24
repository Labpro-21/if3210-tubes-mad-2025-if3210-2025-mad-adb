package com.example.adbpurrytify.di

import com.example.adbpurrytify.api.ApiService
import com.example.adbpurrytify.data.AuthRepository
import com.example.adbpurrytify.data.SongRepository
import com.example.adbpurrytify.data.TokenManager
import com.example.adbpurrytify.data.local.AnalyticsDao
import com.example.adbpurrytify.data.local.SongDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: ApiService,
        tokenManager: TokenManager,
        analyticsDao: AnalyticsDao,
        songRepository: SongRepository
    ): AuthRepository {
        return AuthRepository(apiService, tokenManager, analyticsDao, songRepository)
    }

    @Provides
    @Singleton
    fun provideSongRepository(
        songDao: SongDao,
        apiService: ApiService,
        analyticsDao: AnalyticsDao

    ): SongRepository {
        return SongRepository(songDao, apiService, analyticsDao)
    }
}