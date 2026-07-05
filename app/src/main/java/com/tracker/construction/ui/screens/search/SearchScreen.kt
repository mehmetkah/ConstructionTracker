package com.tracker.construction.ui.screens.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tracker.construction.data.Repository
import com.tracker.construction.data.entities.UnitRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    repository: Repository,
    onBack: () -> Unit,
    onOpenUnit: (Long) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<UnitRecord>>(emptyList()) }

    LaunchedEffect(query) {
        if (query.isBlank()) {
            results = emptyList()
        } else {
            val matches = repository.searchUnits(query.trim())
            results = matches
            // Exact match on a fully typed unit number opens it immediately.
            val exact = matches.firstOrNull { it.unitNumber == query.trim() }
            if (exact != null) {
                onOpenUnit(exact.id)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search Unit") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it.filter { c -> c.isLetterOrDigit() || c == '-' } },
                label = { Text("Unit Number") },
                placeholder = { Text("e.g. 412") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                "Type a unit number — an exact match opens instantly.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )

            LazyColumn(contentPadding = PaddingValues(top = 16.dp)) {
                items(results, key = { it.id }) { unit ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        onClick = { onOpenUnit(unit.id) }
                    ) {
                        Text(
                            "Unit ${unit.unitNumber}",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}
