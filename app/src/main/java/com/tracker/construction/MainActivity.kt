package com.tracker.construction

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tracker.construction.ui.navigation.AppNavGraph
import com.tracker.construction.ui.theme.ConstructionTrackerTheme
import com.tracker.construction.util.ThemeMode

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ConstructionApp

        setContent {
            val themeMode by app.settingsStore.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            ConstructionTrackerTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavGraph(repository = app.repository, app = app)
                }
            }
        }
    }
}
