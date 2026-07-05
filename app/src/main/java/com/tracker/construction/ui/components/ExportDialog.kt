package com.tracker.construction.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.tracker.construction.util.ExportFormat

@Composable
fun ExportDialog(
    onDismiss: () -> Unit,
    onExport: (ExportFormat) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Project") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Choose a format to export and share:")
                TextButton(onClick = { onExport(ExportFormat.PDF) }) { Text("PDF") }
                TextButton(onClick = { onExport(ExportFormat.XLSX) }) { Text("Excel (.xlsx)") }
                TextButton(onClick = { onExport(ExportFormat.CSV) }) { Text("CSV") }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
