package com.tracker.construction.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "units",
    foreignKeys = [
        ForeignKey(
            entity = Floor::class,
            parentColumns = ["id"],
            childColumns = ["floorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("floorId"), Index("projectId"), Index("unitNumber")]
)
data class UnitRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val floorId: Long,
    val projectId: Long,
    val unitNumber: String,
    val tileDone: Boolean = false,
    val groutDone: Boolean = false,
    val lvpDone: Boolean = false,
    val siliconeDone: Boolean = false,
    val qcDone: Boolean = false,
    val tileAt: Long? = null,
    val groutAt: Long? = null,
    val lvpAt: Long? = null,
    val siliconeAt: Long? = null,
    val qcAt: Long? = null,
    val sortOrder: Long = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    /** Number of the 5 stages completed on this unit. */
    val stagesCompleted: Int
        get() = listOf(tileDone, groutDone, lvpDone, siliconeDone, qcDone).count { it }

    val isFullyComplete: Boolean
        get() = qcDone
}
