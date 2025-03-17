package com.example.demoapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.demoapp.data.AppDatabase
import com.example.demoapp.data.DataEntry
import com.example.demoapp.data.DataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DataViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: DataRepository
    val allDataEntries: StateFlow<List<DataEntry>>

    private val _minValue = MutableStateFlow<Float?>(null)
    val minValue = _minValue.asStateFlow()

    private val _maxValue = MutableStateFlow<Float?>(null)
    val maxValue = _maxValue.asStateFlow()

    private val _dataFlow = MutableStateFlow<List<DataEntry>>(emptyList())

    // Add these to DataViewModel.kt
    private val _startDate = MutableStateFlow<Long?>(null)
    val startDate = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Long?>(null)
    val endDate = _endDate.asStateFlow()

    fun setDateRange(start: Long?, end: Long?) {
        _startDate.value = start
        _endDate.value = end
    }

    init {
        val dataEntryDao = AppDatabase.getDatabase(application).dataEntryDao()
        repository = DataRepository(dataEntryDao)
        allDataEntries = _dataFlow

        viewModelScope.launch {
            repository.allDataEntries.collect {
                _dataFlow.value = it
            }
        }
    }

    fun insertDataEntry(date: Long, value: Float) {
        viewModelScope.launch {
            repository.insertDataEntry(date, value)
        }
    }

    fun setMinValue(min: Float?) {
        _minValue.value = min
    }

    fun setMaxValue(max: Float?) {
        _maxValue.value = max
    }

    // In DataViewModel.kt
    fun clearAllData() {
        viewModelScope.launch {
            // Delete all data entries from the database
            repository.deleteAllDataEntries()
        }
    }

    fun checkAndInsertDataEntry(
        date: Long,
        value: Float,
        onExistingEntry: (DataEntry) -> Unit,
        onNewEntryInserted: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val existingEntry = repository.getDataEntryForDay(date)
            if (existingEntry != null) {
                onExistingEntry(existingEntry)
            } else {
                insertDataEntry(date, value)
                onNewEntryInserted()
            }
        }
    }

    fun deleteDataEntry(entry: DataEntry) {
        viewModelScope.launch {
            repository.deleteDataEntry(entry)
        }
    }
}