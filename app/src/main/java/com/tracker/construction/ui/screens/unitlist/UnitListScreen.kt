package com.tracker.construction.ui.screens.unitlist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tracker.construction.data.UnitStage
import com.tracker.construction.data.entities.UnitRecord
import com.tracker.construction.ui.components.ProgressSummary
import com.tracker.construction.ui.components.UnitCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitListScreen(
    viewModel: UnitListViewModel,
    onBack: () -> Unit,
    onOpenUnit: (Long) -> Unit
) {
    val floor by viewModel.floor.collectAsState()
    val units by viewModel.filteredUnits.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val completed by viewModel.completedCount.collectAsState()
    val total by viewModel.totalCount.collectAsState()

    var longPressUnit by remember { mutableStateOf<UnitRecord?>(null) }
    var renameUnit by remember { mutableStateOf<UnitRecord?>(null) }
    var deleteUnit by remember { mutableStateOf<UnitRecord?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(floor?.name ?: "Floor") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ProgressSummary(
                completed = completed,
                total = total,
                modifier = Modifier.padding(12.dp)
            )

            FilterRow(current = filter, onSelect = viewModel::setFilter)

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(onClick = viewModel::addUnit) {
                    Icon(Icons.Filled.Add, contentDescription = null)
                    Text(" Add Unit")
                }
                Button(onClick = viewModel::removeLastUnit) {
                    Icon(Icons.Filled.Remove, contentDescription = null)
                    Text(" Remove Last")
                }
            }

            if (units.isEmpty()) {
                Text(
                    "No units match this filter.",
                    modifier = Modifier.padding(24.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(units, key = { it.id }) { unit ->
                        UnitCard(
                            unit = unit,
                            onToggleTile = { viewModel.toggleStage(unit, UnitStage.TILE) },
                            onToggleGrout = { viewModel.toggleStage(unit, UnitStage.GROUT) },
                            onToggleLvp = { viewModel.toggleStage(unit, UnitStage.LVP) },
                            onToggleSilicone = { viewModel.toggleStage(unit, UnitStage.SILICONE) },
                            onToggleQc = { viewModel.toggleStage(unit, UnitStage.QC) },
                            onClick = { onOpenUnit(unit.id) },
                            onLongPress = { longPressUnit = unit }
                        )
                    }
                }
            }
        }
    }

    longPressUnit?.let { unit ->
        AlertDialog(
            onDismissRequest = { longPressUnit = null },
            title = { Text("Unit ${unit.unitNumber}") },
            text = { Text("Choose an action") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.duplicateUnit(unit)
                    longPressUnit = null
                }) { Text("Duplicate") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        renameUnit = unit
                        longPressUnit = null
                    }) { Text("Rename") }
                    TextButton(onClick = {
                        deleteUnit = unit
                        longPressUnit = null
                    }) { Text("Delete") }
                }
            }
        )
    }

    renameUnit?.let { unit ->
        var text by remember(unit.id) { mutableStateOf(unit.unitNumber) }
        AlertDialog(
            onDismissRequest = { renameUnit = null },
            title = { Text("Rename Unit") },
            text = {
                OutlinedTextField(value = text, onValueChange = { text = it }, singleLine = true)
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.renameUnit(unit, text)
                    renameUnit = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { renameUnit = null }) { Text("Cancel") }
            }
        )
    }

    deleteUnit?.let { unit ->
        AlertDialog(
            onDismissRequest = { deleteUnit = null },
            title = { Text("Delete Unit ${unit.unitNumber}?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteUnit(unit)
                    deleteUnit = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteUnit = null }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(current: UnitFilter, onSelect: (UnitFilter) -> Unit) {
    val options = listOf(
        UnitFilter.ALL to "All",
        UnitFilter.PENDING_TILE to "Pending Tile",
        UnitFilter.PENDING_GROUT to "Pending Grout",
        UnitFilter.PENDING_LVP to "Pending LVP",
        UnitFilter.PENDING_SILICONE to "Pending Silicone",
        UnitFilter.PENDING_QC to "Pending QC",
        UnitFilter.COMPLETED to "Completed"
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(options) { (value, label) ->
            FilterChip(
                selected = current == value,
                onClick = { onSelect(value) },
                label = { Text(label) }
            )
        }
    }
}
