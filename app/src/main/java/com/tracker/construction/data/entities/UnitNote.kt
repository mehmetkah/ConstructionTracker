package com.tracker.construction.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "unit_notes",
    foreignKeys = [
        ForeignKey(
            entity = UnitRecord::class,
            parentColumns = ["id"],
            childColumns = ["unitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("unitId")]
)
data class UnitNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val unitId: Long,
    val text: String,
    val createdAt: Long = System.currentTimeMillis()
)
