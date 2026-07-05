package com.tracker.construction.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ProgressSummary(completed: Int, total: Int, modifier: Modifier = Modifier) {
    val remaining = total - completed
    val percent = if (total == 0) 0 else (completed * 100) / total

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Completed: $completed", style = MaterialTheme.typography.bodyLarge)
                Text("Remaining: $remaining", style = MaterialTheme.typography.bodyLarge)
                Text("$percent%", style = MaterialTheme.typography.titleMedium)
            }
            Spacer()
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else completed / total.toFloat() },
                modifier = Modifier.fillMaxWidth().height(10.dp)
            )
        }
    }
}

@Composable
private fun Spacer() {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
}
