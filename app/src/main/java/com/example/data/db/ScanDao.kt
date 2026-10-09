package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ScanItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ScanDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanItem>>

    @Query("SELECT * FROM scan_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<ScanItem>>

    @Query("SELECT * FROM scan_history WHERE barcode = :barcode LIMIT 1")
    suspend fun getByBarcode(barcode: String): ScanItem?

    @Query("SELECT * FROM scan_history WHERE barcode LIKE '%' || :query || '%' OR productName LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchScans(query: String): Flow<List<ScanItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ScanItem): Long

    @Update
    suspend fun update(item: ScanItem)

    @Delete
    suspend fun delete(item: ScanItem)

    @Query("DELETE FROM scan_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM scan_history")
    suspend fun clearAll()
}
