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
    // HEARTBEAT SIGNAL: Forces UI to refresh in real-time
    private val _refreshSignal = MutableSharedFlow<Unit>(replay = 1)
    val refreshSignal = _refreshSignal.asSharedFlow()

    fun triggerRefresh() {
        _refreshSignal.tryEmit(Unit)
    }

    fun getAllExpenses(userId: String): Flow<List<Expense>> = refreshSignal
        .onStart { emit(Unit) } // Fetch immediately on start
        .flatMapLatest {
            flow {
                try {
                    val response = financeApi.getExpenses()
                    if (response.isSuccessful) emit(response.body() ?: emptyList())
                } catch (e: Exception) { emit(emptyList()) }
            }
        }

    suspend fun addExpense(expense: Expense) {
        try {
            val response = financeApi.addExpense(expense)
            if (response.isSuccessful) triggerRefresh() // PULSE HEARTBEAT
        } catch (e: Exception) {
            Log.e("FinanceRepo", "Network error adding expense")
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

    suspend fun addIncome(income: Income) {
        try {
            val response = financeApi.addIncome(income)
            if (response.isSuccessful) triggerRefresh() // PULSE HEARTBEAT
        } catch (e: Exception) {
            Log.e("FinanceRepo", "Network error adding income")
        }
    }

    fun getBudgetsForMonth(userId: String, month: String): Flow<List<Budget>> = refreshSignal
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                try {
                    val response = financeApi.getBudgets(month)
                    if (response.isSuccessful) emit(response.body() ?: emptyList())
                } catch (e: Exception) { emit(emptyList()) }
            }
        }

    suspend fun addBudget(budget: Budget) {
        try {
            val response = financeApi.addBudget(budget)
            if (response.isSuccessful) triggerRefresh()
        } catch (e: Exception) { }
    }

    fun getSavingsGoals(userId: String): Flow<List<SavingsGoal>> = refreshSignal
        .onStart { emit(Unit) }
        .flatMapLatest {
            flow {
                try {
                    val response = financeApi.getSavingsGoals()
                    if (response.isSuccessful) emit(response.body() ?: emptyList())
                } catch (e: Exception) { emit(emptyList()) }
            }
        }

    suspend fun addSavingsGoal(goal: SavingsGoal) {
        try {
            val response = financeApi.addSavingsGoal(goal)
            if (response.isSuccessful) triggerRefresh()
        } catch (e: Exception) { }
    }

    suspend fun clearAllData() { }
}
