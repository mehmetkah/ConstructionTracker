package com.tracker.construction

import android.app.Application
import com.tracker.construction.data.AppDatabase
import com.tracker.construction.data.Repository
import com.tracker.construction.util.SettingsStore

class ConstructionApp : Application() {

    lateinit var repository: Repository
    lateinit var settingsStore: SettingsStore

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = Repository(db)
        settingsStore = SettingsStore(this)
    }
}
