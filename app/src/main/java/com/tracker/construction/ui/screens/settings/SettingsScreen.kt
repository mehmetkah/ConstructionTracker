package com.tracker.construction.ui.screens.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tracker.construction.util.BackupUtil
import com.tracker.construction.util.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val autoBackup by viewModel.autoBackup.collectAsState()
    val exportFolder by viewModel.exportFolder.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var folderText by remember(exportFolder) { mutableStateOf(exportFolder) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            ThemeOption("Light Mode", themeMode == ThemeMode.LIGHT) { viewModel.setThemeMode(ThemeMode.LIGHT) }
            ThemeOption("Dark Mode", themeMode == ThemeMode.DARK) { viewModel.setThemeMode(ThemeMode.DARK) }
            ThemeOption("Follow System", themeMode == ThemeMode.SYSTEM) { viewModel.setThemeMode(ThemeMode.SYSTEM) }

            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(20.dp))

            Text("Backup", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Auto Backup", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = autoBackup, onCheckedChange = { viewModel.setAutoBackup(it) })
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = {
                    scope.launch {
                        val file = BackupUtil.createBackup(context)
                        Toast.makeText(context, "Backup saved: ${file.name}", Toast.LENGTH_LONG).show()
                    }
                }) { Text("Create Backup") }

                Button(onClick = {
                    val backups = BackupUtil.listBackups(context)
                    if (backups.isEmpty()) {
                        Toast.makeText(context, "No backups found", Toast.LENGTH_SHORT).show()
                    } else {
                        BackupUtil.restoreBackup(context, backups.first())
                        Toast.makeText(context, "Restored. Restart the app to finish.", Toast.LENGTH_LONG).show()
                    }
                }) { Text("Restore Backup") }
            }

            Spacer(Modifier.height(20.dp))
            Divider()
            Spacer(Modifier.height(20.dp))

            Text("Export Folder Name", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = folderText,
                onValueChange = {
                    folderText = it
                    viewModel.setExportFolder(it)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Exports (PDF, Excel, CSV) are saved inside the app's private storage and can be shared from each project's export menu.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ThemeOption(label: String, selected: Boolean, onSelect: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        RadioButton(selected = selected, onClick = onSelect)
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
