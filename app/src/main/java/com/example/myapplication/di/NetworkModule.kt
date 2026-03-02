package com.example.myapplication.di

import android.util.Log
import com.example.myapplication.api.*
import com.example.myapplication.auth.TokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val IS_PRODUCTION = false 
    private const val PROD_URL = "https://kiwi-ai-backend.vercel.app/"
    private const val MY_MAC_IP = "192.168.0.5" 

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenManager: TokenManager): OkHttpClient {
        val logging = HttpLoggingInterceptor { message ->
            Log.d("NETWORK_TRACE", message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY 
        }
        
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                tokenManager.getToken()?.let {
                    request.addHeader("Authorization", "Bearer $it")
                }
                chain.proceed(request.build())
            }
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        val isEmulator = android.os.Build.PRODUCT.contains("sdk") || 
                        android.os.Build.MODEL.contains("Emulator")
        
        val finalUrl = when {
            IS_PRODUCTION -> PROD_URL
            isEmulator -> "http://10.0.2.2:5002/"
            else -> "http://$MY_MAC_IP:5002/" 
        }

        return Retrofit.Builder()
            .baseUrl(finalUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides @Singleton fun provideAuthApi(r: Retrofit): AuthApi = r.create(AuthApi::class.java)
    @Provides @Singleton fun provideUsageApi(r: Retrofit): UsageApi = r.create(UsageApi::class.java)
    @Provides @Singleton fun provideFinanceApi(r: Retrofit): FinanceApi = r.create(FinanceApi::class.java)
    @Provides @Singleton fun provideProductivityApi(r: Retrofit): ProductivityApi = r.create(ProductivityApi::class.java)
    @Provides @Singleton fun provideFitnessApi(r: Retrofit): FitnessApi = r.create(FitnessApi::class.java)
}
