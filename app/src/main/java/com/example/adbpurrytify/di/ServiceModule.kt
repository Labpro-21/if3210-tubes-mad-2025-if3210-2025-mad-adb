package com.example.adbpurrytify.di

import android.content.Context
import com.example.adbpurrytify.service.MediaPlayerService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped

@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {

    @ServiceScoped
    @Provides
    fun provideMediaPlayerService(
        @ApplicationContext context: Context
    ): MediaPlayerService {
        return MediaPlayerService(context)
    }
}