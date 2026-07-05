package com.tracker.construction.ui.screens.floor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun CreateFloorDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, start: Int, end: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }

    val startInt = start.toIntOrNull()
    val endInt = end.toIntOrNull()
    val isValid = name.isNotBlank() && startInt != null && endInt != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Floor") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Floor Name or Number") },
                    placeholder = { Text("e.g. 4th Floor") },
                    singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = start,
                    onValueChange = { start = it.filter(Char::isDigit) },
                    label = { Text("Starting Unit") },
                    placeholder = { Text("e.g. 401") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = end,
                    onValueChange = { end = it.filter(Char::isDigit) },
                    label = { Text("Ending Unit") },
                    placeholder = { Text("e.g. 426") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (isValid) {
                    Spacer(Modifier.height(8.dp))
                    val count = kotlin.math.abs(endInt!! - startInt!!) + 1
                    Text("Will generate $count units automatically.")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = { onCreate(name.trim(), startInt!!, endInt!!) }
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
