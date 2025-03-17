package com.example.demoapp.data

import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class DataRepository(private val dataEntryDao: DataEntryDao) {
    val allDataEntries: Flow<List<DataEntry>> = dataEntryDao.getAllDataEntries()

    suspend fun insertDataEntry(date: Long, value: Float) {
        dataEntryDao.insertDataEntry(DataEntry(date = date, value = value))
    }

    suspend fun deleteAllDataEntries() {
        dataEntryDao.deleteAll()
    }

    suspend fun getDataEntryForDay(date: Long): DataEntry? {
        val calendar = Calendar.getInstance().apply { timeInMillis = date }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis

        return dataEntryDao.getDataEntryForDay(startOfDay, endOfDay)
    }

    suspend fun deleteDataEntry(entry: DataEntry) {
        dataEntryDao.delete(entry)
    }
}