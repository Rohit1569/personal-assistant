package com.example.myapplication.data

import androidx.room.*
import com.example.myapplication.models.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    fun getAllExpenses(userId: String): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: Budget)

    @Query("SELECT * FROM budgets WHERE userId = :userId AND month = :month")
    fun getBudgetsForMonth(userId: String, month: String): Flow<List<Budget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: Income)

    @Query("SELECT * FROM incomes WHERE userId = :userId ORDER BY date DESC")
    fun getIncomes(userId: String): Flow<List<Income>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoal)

    @Query("SELECT * FROM savings_goals WHERE userId = :userId")
    fun getSavingsGoals(userId: String): Flow<List<SavingsGoal>>

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()

    @Query("DELETE FROM incomes")
    suspend fun clearIncomes()

    @Query("DELETE FROM budgets")
    suspend fun clearBudgets()

    @Query("DELETE FROM savings_goals")
    suspend fun clearSavings()
    
    @Transaction
    suspend fun clearAllUserData() {
        clearExpenses()
        clearIncomes()
        clearBudgets()
        clearSavings()
    }
}
