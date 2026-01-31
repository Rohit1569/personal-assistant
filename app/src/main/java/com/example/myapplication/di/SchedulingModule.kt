package com.example.myapplication.di

import android.content.Context
import com.example.myapplication.communication.*
import com.example.myapplication.data.SchedulingRepository
import com.example.myapplication.data.SchedulingRepositoryImpl
import com.example.myapplication.voice.VoiceIntentProcessor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SchedulingModule {

    @Provides
    @Singleton
    fun provideSchedulingRepository(): SchedulingRepository {
        return SchedulingRepositoryImpl()
    }

    @Provides
    @Singleton
    fun provideVoiceIntentProcessor(): VoiceIntentProcessor {
        return VoiceIntentProcessor()
    }

    @Provides
    @Singleton
    fun provideCommunicationManager(@ApplicationContext context: Context): CommunicationManager {
        return CommunicationManager(context)
    }

    @Provides
    @Singleton
    fun provideBackgroundManager(@ApplicationContext context: Context): BackgroundManager {
        return BackgroundManager(context)
    }

    @Provides
    @Singleton
    fun provideContactHelper(@ApplicationContext context: Context): ContactHelper {
        return ContactHelper(context)
    }

    @Provides
    @Singleton
    fun provideTtsManager(@ApplicationContext context: Context): TtsManager {
        return TtsManager(context)
    }

    @Provides
    @Singleton
    fun provideCabBookingManager(@ApplicationContext context: Context): CabBookingManager {
        return CabBookingManager(context)
    }
}
