package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scan_history")
data class ScanItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barcode: String,
    val format: String,
    val productName: String? = null,
    val brand: String? = null,
    val imageUrl: String? = null,
    val category: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val note: String? = null
)
