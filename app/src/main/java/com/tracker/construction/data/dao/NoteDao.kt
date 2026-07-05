package com.tracker.construction.data.dao

import androidx.room.*
import com.tracker.construction.data.entities.UnitNote
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM unit_notes WHERE unitId = :unitId ORDER BY createdAt DESC")
    fun observeForUnit(unitId: Long): Flow<List<UnitNote>>

    @Insert
    suspend fun insert(note: UnitNote): Long

    @Delete
    suspend fun delete(note: UnitNote)
}
