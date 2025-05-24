package com.example.adbpurrytify.di

import com.example.adbpurrytify.data.AnalyticsRepository
import com.example.adbpurrytify.data.SongRepository
import com.example.adbpurrytify.data.analytics.AnalyticsService
import com.example.adbpurrytify.data.local.AnalyticsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AnalyticsModule {

    @Provides
    @Singleton
    fun provideAnalyticsService(analyticsDao: AnalyticsDao): AnalyticsService {
        return AnalyticsService(analyticsDao)
    }

    @Provides
    @Singleton
    fun provideAnalyticsRepository(
        analyticsDao: AnalyticsDao,
        analyticsService: AnalyticsService,
        songRepository: SongRepository
    ): AnalyticsRepository {
        return AnalyticsRepository(analyticsDao, analyticsService, songRepository)
    }
}