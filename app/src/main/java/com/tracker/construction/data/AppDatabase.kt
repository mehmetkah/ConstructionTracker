package com.tracker.construction.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tracker.construction.data.dao.FloorDao
import com.tracker.construction.data.dao.NoteDao
import com.tracker.construction.data.dao.PhotoDao
import com.tracker.construction.data.dao.ProjectDao
import com.tracker.construction.data.dao.UnitDao
import com.tracker.construction.data.entities.Floor
import com.tracker.construction.data.entities.Project
import com.tracker.construction.data.entities.UnitRecord
import com.tracker.construction.data.entities.UnitNote
import com.tracker.construction.data.entities.UnitPhoto

@Database(
    entities = [Project::class, Floor::class, UnitRecord::class, UnitNote::class, UnitPhoto::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao
    abstract fun floorDao(): FloorDao
    abstract fun unitDao(): UnitDao
    abstract fun noteDao(): NoteDao
    abstract fun photoDao(): PhotoDao

    companion object {
        const val DB_NAME = "site_tracker.db"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
