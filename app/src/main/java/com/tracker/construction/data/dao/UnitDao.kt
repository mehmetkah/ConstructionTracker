package com.tracker.construction.data.dao

import androidx.room.*
import com.tracker.construction.data.entities.UnitRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Query("SELECT * FROM units WHERE floorId = :floorId ORDER BY sortOrder ASC")
    fun observeByFloor(floorId: Long): Flow<List<UnitRecord>>

    @Query("SELECT * FROM units WHERE floorId = :floorId ORDER BY sortOrder ASC")
    suspend fun getByFloorOnce(floorId: Long): List<UnitRecord>

    @Query("SELECT * FROM units WHERE projectId = :projectId ORDER BY sortOrder ASC")
    fun observeByProject(projectId: Long): Flow<List<UnitRecord>>

    @Query("SELECT * FROM units WHERE projectId = :projectId ORDER BY sortOrder ASC")
    suspend fun getByProjectOnce(projectId: Long): List<UnitRecord>

    @Query("SELECT * FROM units WHERE id = :id")
    suspend fun getById(id: Long): UnitRecord?

    @Query("SELECT * FROM units WHERE id = :id")
    fun observeById(id: Long): Flow<UnitRecord?>

    @Query("SELECT * FROM units WHERE unitNumber = :unitNumber LIMIT 1")
    suspend fun findByNumber(unitNumber: String): UnitRecord?

    @Query("SELECT * FROM units WHERE unitNumber LIKE :query || '%' ORDER BY sortOrder ASC LIMIT 25")
    suspend fun searchByNumber(query: String): List<UnitRecord>

    @Query("SELECT COUNT(*) FROM units WHERE projectId = :projectId")
    suspend fun countForProject(projectId: Long): Int

    @Query("SELECT COUNT(*) FROM units WHERE projectId = :projectId AND qcDone = 1")
    suspend fun countCompletedForProject(projectId: Long): Int

    @Query("SELECT MAX(sortOrder) FROM units WHERE floorId = :floorId")
    suspend fun maxSortOrderForFloor(floorId: Long): Long?

    @Insert
    suspend fun insert(unit: UnitRecord): Long

    @Insert
    suspend fun insertAll(units: List<UnitRecord>)

    @Update
    suspend fun update(unit: UnitRecord)

    @Delete
    suspend fun delete(unit: UnitRecord)

    @Query("DELETE FROM units WHERE id = :id")
    suspend fun deleteById(id: Long)
}
