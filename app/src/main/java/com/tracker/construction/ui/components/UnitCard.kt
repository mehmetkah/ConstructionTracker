package com.tracker.construction.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tracker.construction.data.entities.UnitRecord
import com.tracker.construction.util.unitStatusColor

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun UnitCard(
    unit: UnitRecord,
    onToggleTile: () -> Unit,
    onToggleGrout: () -> Unit,
    onToggleLvp: () -> Unit,
    onToggleSilicone: () -> Unit,
    onToggleQc: () -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = unitStatusColor(unit)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongPress),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(10.dp)
                    .fillMaxHeight()
                    .background(statusColor, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp))
            )
            Column(modifier = Modifier.padding(14.dp).fillMaxWidth()) {
                Text("Unit ${unit.unitNumber}", style = MaterialTheme.typography.titleMedium)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CheckLabel("Tile", unit.tileDone, onToggleTile)
                    CheckLabel("Grout", unit.groutDone, onToggleGrout)
                    CheckLabel("LVP", unit.lvpDone, onToggleLvp)
                    CheckLabel("Silic.", unit.siliconeDone, onToggleSilicone)
                    CheckLabel("QC", unit.qcDone, onToggleQc)
                }
            }
        }
    }
}

@Composable
private fun CheckLabel(label: String, checked: Boolean, onToggle: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Larger checkbox target so it is easy to tap with work gloves.
        Checkbox(
            checked = checked,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(40.dp)
        )
        Text(label, style = MaterialTheme.typography.labelLarge)
    }
}
