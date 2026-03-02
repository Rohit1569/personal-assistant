package com.example.myapplication.api

import com.example.myapplication.models.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface FinanceApi {
    @POST("api/finance/expenses")
    suspend fun addExpense(@Body expense: Expense): Response<Unit>

    @GET("api/finance/expenses")
    suspend fun getExpenses(): Response<List<Expense>>

    @POST("api/finance/incomes")
    suspend fun addIncome(@Body income: Income): Response<Unit>

    @GET("api/finance/incomes")
    suspend fun getIncomes(): Response<List<Income>>

    @POST("api/finance/budgets")
    suspend fun addBudget(@Body budget: Budget): Response<Unit>

    @GET("api/finance/budgets")
    suspend fun getBudgets(@Query("month") month: String?): Response<List<Budget>>

    @POST("api/finance/savings-goals")
    suspend fun addSavingsGoal(@Body goal: SavingsGoal): Response<Unit>

    @GET("api/finance/savings-goals")
    suspend fun getSavingsGoals(): Response<List<SavingsGoal>>

    @GET("api/finance/categories")
    suspend fun getCategories(): Response<List<Category>>
}
