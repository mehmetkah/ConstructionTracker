package com.tracker.construction.data.dao

import androidx.room.*
import com.tracker.construction.data.entities.Floor
import kotlinx.coroutines.flow.Flow

@Dao
interface FloorDao {
    @Query("SELECT * FROM floors WHERE projectId = :projectId ORDER BY sortOrder ASC")
    fun observeByProject(projectId: Long): Flow<List<Floor>>

    @Query("SELECT * FROM floors WHERE projectId = :projectId ORDER BY sortOrder ASC")
    suspend fun getByProjectOnce(projectId: Long): List<Floor>

    @Query("SELECT * FROM floors WHERE id = :id")
    suspend fun getById(id: Long): Floor?

    @Query("SELECT * FROM floors WHERE id = :id")
    fun observeById(id: Long): Flow<Floor?>

    @Query("SELECT COUNT(*) FROM floors WHERE projectId = :projectId")
    suspend fun countForProject(projectId: Long): Int

    @Insert
    suspend fun insert(floor: Floor): Long

    @Update
    suspend fun update(floor: Floor)

    @Delete
    suspend fun delete(floor: Floor)
}
