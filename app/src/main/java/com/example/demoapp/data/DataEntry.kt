package com.example.demoapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "data_entries")
data class DataEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long, // Timestamp
    val value: Float
)