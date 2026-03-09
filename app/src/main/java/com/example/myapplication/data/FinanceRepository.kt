package com.example.myapplication.data

import android.util.Log
import com.example.myapplication.api.FinanceApi
import com.example.myapplication.models.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FinanceRepository @Inject constructor(
    private val financeApi: FinanceApi
) {
    // REFRESH PULSE: Forces UI to stay in sync with Cloud DB
    private val _refreshSignal = MutableSharedFlow<Unit>(replay = 1)
    val refreshSignal = _refreshSignal.asSharedFlow()

    init { triggerRefresh() }

    fun triggerRefresh() { _refreshSignal.tryEmit(Unit) }

    fun getAllExpenses(userId: String): Flow<List<Expense>> = refreshSignal
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                try {
                    val response = financeApi.getExpenses()
                    if (response.isSuccessful) emit(response.body() ?: emptyList())
                } catch (e: Exception) { emit(emptyList()) }
            }
        }

    suspend fun addExpense(expense: Expense): Boolean {
        return try {
            val response = financeApi.addExpense(expense)
            if (response.isSuccessful) {
                triggerRefresh()
                true
            } else false
        } catch (e: Exception) {
            Log.e("FinanceRepo", "Failed to add expense")
            false
        }
    }

    fun getIncomes(userId: String): Flow<List<Income>> = refreshSignal
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                try {
                    val response = financeApi.getIncomes()
                    if (response.isSuccessful) emit(response.body() ?: emptyList())
                } catch (e: Exception) { emit(emptyList()) }
            }
        }

    suspend fun addIncome(income: Income): Boolean {
        return try {
            val response = financeApi.addIncome(income)
            if (response.isSuccessful) {
                triggerRefresh()
                true
            } else false
        } catch (e: Exception) {
            Log.e("FinanceRepo", "Failed to add income")
            false
        }
    }

    fun getBudgetsForMonth(userId: String, month: String): Flow<List<Budget>> = flowOf(emptyList())
    fun getSavingsGoals(userId: String): Flow<List<SavingsGoal>> = flowOf(emptyList())
    suspend fun clearAllData() { }
}
