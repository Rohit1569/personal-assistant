package com.example.myapplication.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.FinanceRepository
import com.example.myapplication.models.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class MonthlyReport(
    val month: String,
    val totalSpent: Double,
    val totalIncome: Double,
    val remaining: Double,
    val percentageUsed: Float,
    val dailyAverage: Double,
    val topCategory: String,
    val prevMonthComparison: Double,
    val healthScore: Int,
    val insights: List<String>,
    val savingTips: List<String>,
    val warnings: List<String>
)

@HiltViewModel
class FinanceViewModel @Inject constructor(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _userId = MutableStateFlow<String?>(null)
    private val _selectedMonth = MutableStateFlow(SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date()))
    val selectedMonth: StateFlow<String> = _selectedMonth.asStateFlow()

    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())
    val expenses: StateFlow<List<Expense>> = _expenses.asStateFlow()

    private val _summaries = MutableStateFlow<List<ExpenseSummary>>(emptyList())
    val summaries: StateFlow<List<ExpenseSummary>> = _summaries.asStateFlow()

    private val _incomes = MutableStateFlow<List<Income>>(emptyList())
    val incomes: StateFlow<List<Income>> = _incomes.asStateFlow()

    private val _monthlyReport = MutableStateFlow<MonthlyReport?>(null)
    val monthlyReport: StateFlow<MonthlyReport?> = _monthlyReport.asStateFlow()

    fun initForUser(userId: String) {
        if (_userId.value == userId) return
        _userId.value = userId
        loadAllData(userId)
    }

    fun setSelectedMonth(month: String) {
        _selectedMonth.value = month
        _userId.value?.let { loadAllData(it) }
    }

    private fun loadAllData(userId: String) {
        viewModelScope.launch {
            repository.getAllExpenses(userId).collect { list ->
                _expenses.value = list
                updateCalculations()
            }
        }
        viewModelScope.launch {
            repository.getIncomes(userId).collect { list ->
                _incomes.value = list
                updateCalculations()
            }
        }
    }

    private fun updateCalculations() {
        val currentMonthStr = _selectedMonth.value
        val allExpenses = _expenses.value
        val allIncomes = _incomes.value

        val currentExpenses = allExpenses.filter { it.date.startsWith(currentMonthStr) }
        val totalSpent = currentExpenses.sumOf { it.amount }
        val totalIncome = allIncomes.filter { it.date.startsWith(currentMonthStr) }.sumOf { it.amount }

        val summaryList = currentExpenses.groupBy { it.category }.map { (name, items) ->
            val catTotal = items.sumOf { it.amount }
            ExpenseSummary(
                categoryName = name,
                totalAmount = catTotal,
                currency = "USD",
                percentage = if (totalSpent > 0) (catTotal / totalSpent).toFloat() else 0f
            )
        }
        _summaries.value = summaryList.sortedByDescending { it.totalAmount }

        generateNeuralReport(allExpenses, allIncomes, currentMonthStr, totalSpent, totalIncome)
    }

    private fun generateNeuralReport(allExpenses: List<Expense>, allIncomes: List<Income>, monthStr: String, totalSpent: Double, totalIncome: Double) {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        try {
            val calendar = Calendar.getInstance()
            calendar.time = sdf.parse(monthStr)!!
            val prevCal = calendar.clone() as Calendar
            prevCal.add(Calendar.MONTH, -1)
            val prevMonthStr = sdf.format(prevCal.time)
            val prevSpent = allExpenses.filter { it.date.startsWith(prevMonthStr) }.sumOf { it.amount }
            val comparison = if (prevSpent > 0) ((totalSpent - prevSpent) / prevSpent) * 100 else 0.0
            val currentDay = if (monthStr == sdf.format(Date())) Calendar.getInstance().get(Calendar.DAY_OF_MONTH) else 30
            val dailyAvg = if (currentDay > 0) totalSpent / currentDay else 0.0
            val insights = mutableListOf<String>()
            val tips = mutableListOf<String>()
            val warnings = mutableListOf<String>()

            tips.add("Rule of thumb: Aim to save at least 20% of your income.")

            if (totalIncome > 0) {
                val gasRatio = (allExpenses.filter { it.category.lowercase().contains("gas") }.sumOf { it.amount } / totalIncome) * 100
                if (gasRatio >= 15) {
                    warnings.add("Alert: You spent ${gasRatio.toInt()}% of your salary on fuel.")
                    tips.add("Consider carpooling or planning trips to save fuel.")
                }
                if (totalSpent > totalIncome) {
                    warnings.add("Critical: Your expenses exceed your income this month!")
                }
            } else {
                insights.add("Add your salary to enable Neural Overspending detection.")
            }

            var score = 100
            if (totalIncome > 0) {
                val savingsRatio = (totalIncome - totalSpent) / totalIncome
                if (savingsRatio < 0.1) score -= 50
            } else if (totalSpent > 0) score = 40

            _monthlyReport.value = MonthlyReport(
                month = monthStr,
                totalSpent = totalSpent,
                totalIncome = totalIncome,
                remaining = totalIncome - totalSpent,
                percentageUsed = if (totalIncome > 0) (totalSpent / totalIncome).toFloat() else 0f,
                dailyAverage = dailyAvg,
                topCategory = _summaries.value.firstOrNull()?.categoryName ?: "None",
                prevMonthComparison = comparison,
                healthScore = score.coerceIn(0, 100),
                insights = insights,
                savingTips = tips,
                warnings = warnings
            )
        } catch (e: Exception) { /* Date parsing can fail on month switch */ }
    }

    private fun getCurrentIsoDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }

    fun addManualExpense(name: String, catName: String, price: Double) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            repository.addExpense(Expense(
                id = UUID.randomUUID().toString(),
                userId = uid,
                category = catName,
                amount = price,
                date = getCurrentIsoDate(),
                note = name
            ))
            loadAllData(uid)
        }
    }

    fun addIncome(source: String, amount: Double) {
        val uid = _userId.value ?: return
        viewModelScope.launch {
            repository.addIncome(Income(
                id = UUID.randomUUID().toString(),
                userId = uid,
                source = source,
                amount = amount,
                date = getCurrentIsoDate()
            ))
            loadAllData(uid)
        }
    }

    fun clearDataOnLogout() {
        viewModelScope.launch {
            // repository.clearAllData() // Disabled for local testing persistence
            _expenses.value = emptyList()
            _incomes.value = emptyList()
            _summaries.value = emptyList()
            _monthlyReport.value = null
            _userId.value = null
        }
    }

    fun processExtractedText(text: String) {
        val uid = _userId.value ?: return
        val lines = text.split("\n")
        var detectedTotal = 0.0
        var totalFoundByKeyword = false
        val priceRegex = Regex("[\\$₹]?\\s?(\\d+[\\.,]\\d{2})")

        for (line in lines) {
            val lowerLine = line.lowercase().trim()
            if (lowerLine.isEmpty()) continue

            val match = priceRegex.find(line)
            if (match != null) {
                val priceStr = match.groups[1]?.value?.replace(",", ".")
                val price = priceStr?.toDoubleOrNull() ?: 0.0
                
                if (lowerLine.contains("total")) {
                    detectedTotal = price
                    totalFoundByKeyword = true
                } else if (!totalFoundByKeyword && !lowerLine.contains("cash") && !lowerLine.contains("change")) {
                    if (price > detectedTotal) detectedTotal = price
                }
            }
        }

        if (detectedTotal > 0.0) {
            viewModelScope.launch {
                repository.addExpense(Expense(
                    id = UUID.randomUUID().toString(),
                    userId = uid,
                    category = "Shopping", 
                    amount = detectedTotal,
                    date = getCurrentIsoDate(),
                    note = "Scanned Bill"
                ))
                loadAllData(uid)
            }
        }
    }
}
