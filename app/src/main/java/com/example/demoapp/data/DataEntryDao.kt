package com.example.demoapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DataEntryDao {
    @Insert
    suspend fun insertDataEntry(dataEntry: DataEntry): Long

    @Query("SELECT * FROM data_entries ORDER BY date ASC")
    fun getAllDataEntries(): Flow<List<DataEntry>>

    @Query("DELETE FROM data_entries")
    suspend fun deleteAll()

    @Query("SELECT * FROM data_entries WHERE date >= :startOfDay AND date < :endOfDay LIMIT 1")
    suspend fun getDataEntryForDay(startOfDay: Long, endOfDay: Long): DataEntry?

    @Delete
    suspend fun delete(dataEntry: DataEntry)
}