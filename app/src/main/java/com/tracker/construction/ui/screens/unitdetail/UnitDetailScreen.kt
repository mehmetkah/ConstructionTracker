package com.tracker.construction.ui.screens.unitdetail

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.tracker.construction.data.UnitStage
import com.tracker.construction.util.ImageUtil
import com.tracker.construction.util.unitStatusLabel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDetailScreen(
    viewModel: UnitDetailViewModel,
    onBack: () -> Unit
) {
    val unit by viewModel.unit.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val photos by viewModel.photos.collectAsState()
    val context = LocalContext.current

    var pendingCameraFile by remember { mutableStateOf<java.io.File?>(null) }
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            pendingCameraFile?.let { viewModel.addPhoto(it.absolutePath) }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            ImageUtil.copyFromUri(context, uri)?.let { viewModel.addPhoto(it) }
        }
    }

    var noteText by rememberSaveable { mutableStateOf("") }
    val dateFmt = remember { SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unit ${unit?.unitNumber ?: ""}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val u = unit
        if (u == null) {
            Text("Loading...", modifier = Modifier.padding(padding).padding(16.dp))
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            item {
                Text(unitStatusLabel(u), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
            }

            item {
                StageRow("Tile Installed", u.tileDone, u.tileAt, dateFmt) { viewModel.toggleStage(UnitStage.TILE) }
                StageRow("Grout Installed", u.groutDone, u.groutAt, dateFmt) { viewModel.toggleStage(UnitStage.GROUT) }
                StageRow("LVP Installed", u.lvpDone, u.lvpAt, dateFmt) { viewModel.toggleStage(UnitStage.LVP) }
                StageRow("Silicone Installed", u.siliconeDone, u.siliconeAt, dateFmt) { viewModel.toggleStage(UnitStage.SILICONE) }
                StageRow("QC Complete", u.qcDone, u.qcAt, dateFmt) { viewModel.toggleStage(UnitStage.QC) }
                Spacer(Modifier.height(20.dp))
                Divider()
                Spacer(Modifier.height(16.dp))
            }

            item {
                Text("Photos", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(onClick = {
                        val (file, uri) = ImageUtil.createCameraDestination(context)
                        pendingCameraFile = file
                        pendingCameraUri = uri
                        cameraLauncher.launch(uri)
                    }) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null)
                        Text(" Camera")
                    }
                    TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null)
                        Text(" Gallery")
                    }
                }
                Spacer(Modifier.height(8.dp))
                if (photos.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(photos, key = { it.id }) { photo ->
                            Card {
                                Column {
                                    AsyncImage(
                                        model = photo.filePath,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(110.dp)
                                    )
                                    IconButton(onClick = { viewModel.deletePhoto(photo) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Delete photo")
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
                Divider()
                Spacer(Modifier.height(16.dp))
            }

            item {
                Text("Notes", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        modifier = Modifier.fillMaxWidth(0.75f),
                        placeholder = { Text("Add a note...") }
                    )
                    Spacer(Modifier.size(8.dp))
                    TextButton(onClick = {
                        viewModel.addNote(noteText)
                        noteText = ""
                    }) { Text("Add") }
                }
                Spacer(Modifier.height(8.dp))
            }

            items(notes, key = { it.id }) { note ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.fillMaxWidth(0.85f)) {
                            Text(note.text, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                dateFmt.format(Date(note.createdAt)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { viewModel.deleteNote(note) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Delete note")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StageRow(
    label: String,
    checked: Boolean,
    timestamp: Long?,
    dateFmt: SimpleDateFormat,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Checkbox(checked = checked, onCheckedChange = { onToggle() })
            Column {
                Text(label, style = MaterialTheme.typography.bodyLarge)
                if (checked && timestamp != null) {
                    Text(
                        "Completed ${dateFmt.format(Date(timestamp))}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
