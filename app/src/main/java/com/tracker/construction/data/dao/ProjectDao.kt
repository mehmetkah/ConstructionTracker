package com.tracker.construction.data.dao

import androidx.room.*
import com.tracker.construction.data.entities.Project
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM projects ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<Project>>

    @Query("SELECT * FROM projects ORDER BY name ASC")
    fun observeAllByName(): Flow<List<Project>>

    @Query("SELECT * FROM projects WHERE id = :id")
    suspend fun getById(id: Long): Project?

    @Query("SELECT * FROM projects WHERE id = :id")
    fun observeById(id: Long): Flow<Project?>

    @Insert
    suspend fun insert(project: Project): Long

    @Update
    suspend fun update(project: Project)

    @Delete
    suspend fun delete(project: Project)

    @Query("SELECT * FROM projects ORDER BY name ASC")
    suspend fun getAllOnce(): List<Project>
}
