package com.tracker.construction.data.dao

import androidx.room.*
import com.tracker.construction.data.entities.UnitPhoto
import kotlinx.coroutines.flow.Flow

@Dao
interface PhotoDao {
    @Query("SELECT * FROM unit_photos WHERE unitId = :unitId ORDER BY createdAt DESC")
    fun observeForUnit(unitId: Long): Flow<List<UnitPhoto>>

    @Insert
    suspend fun insert(photo: UnitPhoto): Long

    @Delete
    suspend fun delete(photo: UnitPhoto)
}
