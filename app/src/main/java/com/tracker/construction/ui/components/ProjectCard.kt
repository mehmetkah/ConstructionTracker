package com.tracker.construction.ui.components

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tracker.construction.ui.screens.home.ProjectSummary

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ProjectCard(
    summary: ProjectSummary,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressColor = when {
        summary.percentComplete >= 100 -> Color(0xFF2E7D32)
        summary.percentComplete >= 50 -> Color(0xFF1E88E5)
        summary.percentComplete > 0 -> Color(0xFFFB8C00)
        else -> Color(0xFFE53935)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongPress),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Business, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.size(8.dp))
                Text(summary.project.name, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Layers, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(4.dp))
                    Text("${summary.floorCount} floors", style = MaterialTheme.typography.bodyMedium)
                }
                Text(
                    "${summary.completedUnits}/${summary.totalUnits} units",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "${summary.percentComplete}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = progressColor
                )
            }

            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { summary.percentComplete / 100f },
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = progressColor
            )
        }
    }
}
