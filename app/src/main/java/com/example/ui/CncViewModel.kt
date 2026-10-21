package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.CncDatabase
import com.example.data.CncDesignEntity
import com.example.data.CncRepository
import com.example.security.SecurityManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CncViewModel(application: Application) : AndroidViewModel(application) {

    private val database = CncDatabase.getDatabase(application)
    val repository = CncRepository(database.cncDao())
    val securityManager = SecurityManager(application)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    val allDesigns: StateFlow<List<CncDesignEntity>> = repository.allDesigns
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedDate = MutableStateFlow(dateFormat.format(Date()))
    val selectedCategoryFilter = MutableStateFlow<String?>(null)
    val searchQuery = MutableStateFlow("")

    val currentTab = MutableStateFlow(0) // 0: Daily, 1: Analytics, 2: Catalog, 3: Settings

    // Lock state: If user enabled security, lock initially
    val isAppLocked = MutableStateFlow(securityManager.isBiometricEnabled)

    val currencySymbol = MutableStateFlow(securityManager.currencySymbol)

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun unlockApp() {
        isAppLocked.value = false
    }

    fun lockApp() {
        isAppLocked.value = true
    }

    fun updateCurrency(symbol: String) {
        securityManager.currencySymbol = symbol
        currencySymbol.value = symbol
    }

    fun setSelectedDate(dateStr: String) {
        selectedDate.value = dateStr
    }

    fun setPreviousDay() {
        try {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(selectedDate.value) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, -1)
            selectedDate.value = dateFormat.format(cal.time)
        } catch (_: Exception) {}
    }

    fun setNextDay() {
        try {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(selectedDate.value) ?: Date()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            selectedDate.value = dateFormat.format(cal.time)
        } catch (_: Exception) {}
    }

    fun setToday() {
        selectedDate.value = dateFormat.format(Date())
    }

    // Daily designs for selected date, filtered by category
    val dailyDesigns: StateFlow<List<CncDesignEntity>> = combine(
        allDesigns,
        selectedDate,
        selectedCategoryFilter
    ) { designs, date, category ->
        designs.filter { item ->
            val matchesDate = item.dateString == date
            val matchesCategory = category == null || item.category.equals(category, ignoreCase = true)
            matchesDate && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered catalog designs for search & category
    val catalogDesigns: StateFlow<List<CncDesignEntity>> = combine(
        allDesigns,
        searchQuery,
        selectedCategoryFilter
    ) { list, query, category ->
        list.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.title.contains(query, ignoreCase = true) ||
                    item.clientName.contains(query, ignoreCase = true) ||
                    item.material.contains(query, ignoreCase = true) ||
                    item.bitType.contains(query, ignoreCase = true)
            val matchesCategory = category == null || item.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveDesign(design: CncDesignEntity) {
        viewModelScope.launch {
            if (design.id == 0L) {
                repository.insertDesign(design)
            } else {
                repository.updateDesign(design)
            }
        }
    }

    fun deleteDesign(design: CncDesignEntity) {
        viewModelScope.launch {
            repository.deleteDesign(design)
        }
    }

    fun duplicateDesign(design: CncDesignEntity) {
        viewModelScope.launch {
            val copy = design.copy(
                id = 0L,
                title = "${design.title} (Copy)",
                timestampMillis = System.currentTimeMillis(),
                dateString = dateFormat.format(Date())
            )
            repository.insertDesign(copy)
        }
    }

    fun restoreBackup(designs: List<CncDesignEntity>, replaceExisting: Boolean) {
        viewModelScope.launch {
            if (replaceExisting) {
                repository.clearAll()
            }
            repository.insertAll(designs)
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.clearAll()
            repository.seedInitialDataIfEmpty()
        }
    }
}
