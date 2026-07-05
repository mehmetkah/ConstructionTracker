package com.tracker.construction.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.tracker.construction.data.Repository
import com.tracker.construction.data.entities.Project
import com.tracker.construction.ui.components.ExportDialog
import com.tracker.construction.ui.components.ProjectCard
import com.tracker.construction.ui.screens.project.CreateProjectDialog
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    repository: Repository,
    onOpenDrawer: () -> Unit,
    onOpenProject: (Long) -> Unit
) {
    val summaries by viewModel.summaries.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }
    var projectMenuTarget by remember { mutableStateOf<Project?>(null) }
    var renameTarget by remember { mutableStateOf<Project?>(null) }
    var deleteTarget by remember { mutableStateOf<Project?>(null) }
    var exportTarget by remember { mutableStateOf<Project?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Projects") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = null)
                Text("  New Project")
            }
        }
    ) { padding ->
        if (summaries.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("No projects yet.", style = MaterialTheme.typography.titleMedium)
                Text("Tap + New Project to get started.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(summaries, key = { it.project.id }) { summary ->
                    ProjectCard(
                        summary = summary,
                        onClick = { onOpenProject(summary.project.id) },
                        onLongPress = { projectMenuTarget = summary.project }
                    )
                }
            }
        }
    }

    projectMenuTarget?.let { project ->
        AlertDialog(
            onDismissRequest = { projectMenuTarget = null },
            title = { Text(project.name) },
            text = {
                Column {
                    TextButton(onClick = {
                        exportTarget = project
                        projectMenuTarget = null
                    }) { Text("Export (PDF / Excel / CSV)") }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    renameTarget = project
                    projectMenuTarget = null
                }) { Text("Rename") }
            },
            dismissButton = {
                TextButton(onClick = {
                    deleteTarget = project
                    projectMenuTarget = null
                }) { Text("Delete") }
            }
        )
    }

    exportTarget?.let { project ->
        ExportDialog(
            onDismiss = { exportTarget = null },
            onExport = { format ->
                scope.launch {
                    val units = repository.getUnitsForProjectOnce(project.id)
                    com.tracker.construction.util.exportAndShare(context, project, units, format)
                }
                exportTarget = null
            }
        )
    }

    renameTarget?.let { project ->
        var text by remember(project.id) { mutableStateOf(project.name) }
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("Rename Project") },
            text = {
                OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.renameProject(project, text)
                    renameTarget = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { renameTarget = null }) { Text("Cancel") }
            }
        )
    }

    deleteTarget?.let { project ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Project?") },
            text = { Text("This will permanently delete \"${project.name}\" and all its floors and units.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteProject(project)
                    deleteTarget = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text("Cancel") }
            }
        )
    }

    if (showCreateDialog) {
        CreateProjectDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name ->
                viewModel.createProject(name) { newId ->
                    showCreateDialog = false
                    onOpenProject(newId)
                }
            }
        )
    }
}
