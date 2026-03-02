package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.FinanceDao
import com.example.myapplication.data.FinanceDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FinanceModule {

    @Provides
    @Singleton
    fun provideFinanceDatabase(@ApplicationContext context: Context): FinanceDatabase {
        return Room.databaseBuilder(
            context,
            FinanceDatabase::class.java,
            "finance_db"
        ).build()
    }

    @Provides
    fun provideFinanceDao(db: FinanceDatabase): FinanceDao {
        return db.financeDao()
    }
}
