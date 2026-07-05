package com.tracker.construction.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tracker.construction.data.entities.Floor
import com.tracker.construction.data.entities.Project

data class ProjectWithFloors(val project: Project, val floors: List<Floor>)

@Composable
fun AppDrawer(
    projectsWithFloors: List<ProjectWithFloors>,
    onHomeClick: () -> Unit,
    onFloorClick: (Floor) -> Unit,
    onSearchClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.fillMaxSize().padding(vertical = 12.dp)) {
            Text(
                "Site Tracker",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            Divider()

            NavigationDrawerItem(
                label = { Text("Projects") },
                icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                selected = false,
                onClick = onHomeClick,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            NavigationDrawerItem(
                label = { Text("Search Unit") },
                icon = { Icon(Icons.Filled.Search, contentDescription = null) },
                selected = false,
                onClick = onSearchClick,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(projectsWithFloors, key = { it.project.id }) { entry ->
                    ProjectDrawerEntry(entry, onFloorClick)
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))
            NavigationDrawerItem(
                label = { Text("Settings") },
                icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                selected = false,
                onClick = onSettingsClick,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

@Composable
private fun ProjectDrawerEntry(entry: ProjectWithFloors, onFloorClick: (Floor) -> Unit) {
    var expanded by remember { mutableStateOf(true) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Icon(
            if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = null
        )
        Text(
            entry.project.name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }

    if (expanded) {
        entry.floors.forEach { floor ->
            NavigationDrawerItem(
                label = { Text(floor.name) },
                selected = false,
                onClick = { onFloorClick(floor) },
                modifier = Modifier.padding(start = 28.dp, end = 12.dp)
            )
        }
    }
}
