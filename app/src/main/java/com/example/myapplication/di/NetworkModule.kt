package com.example.myapplication.di

import com.example.myapplication.api.AuthApi
import com.example.myapplication.api.UsageApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Your verified live Vercel URL
    private const val BASE_URL = "https://kiwi-ai-backend.vercel.app/" 

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUsageApi(retrofit: Retrofit): UsageApi {
        return retrofit.create(UsageApi::class.java)
    }
}
