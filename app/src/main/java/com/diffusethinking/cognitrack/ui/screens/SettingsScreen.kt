package com.diffusethinking.cognitrack.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.diffusethinking.cognitrack.data.ExportData
import com.diffusethinking.cognitrack.data.TestRecord
import com.diffusethinking.cognitrack.ui.MainViewModel
import com.diffusethinking.cognitrack.ui.theme.AppRed
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showSuccessDialog by remember { mutableStateOf("") }
    var showPVTDetail by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val json = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(it)?.bufferedReader()?.readText()
                    }
                    if (json == null) {
                        errorMessage = "Failed to read the file."
                        showErrorDialog = true
                        return@launch
                    }
                    val exportData = Gson().fromJson(json, ExportData::class.java)
                    val records = exportData.records.map { r ->
                        TestRecord(
                            startDate = r.startDate,
                            isBaseline = r.isBaseline,
                            isGuestMode = r.isGuestMode,
                            guestName = r.guestName,
                            trials = r.trials,
                            averageRT = r.averageRT,
                            lapseCount = r.lapseCount,
                            baselineRT = r.baselineRT
                        )
                    }
                    viewModel.importData(
                        records,
                        exportData.baselineRT,
                        exportData.baselineDate,
                        exportData.guestMode,
                        exportData.guestName
                    )
                    showSuccessDialog = "Your data has been imported."
                } catch (e: Exception) {
                    errorMessage = "The file format is invalid or corrupted."
                    showErrorDialog = true
                }
            }
        }
    }

    if (showPVTDetail) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Reaction Time Test", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { showPVTDetail = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
                )
            },
            containerColor = Color.Black
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                PVTDetailScreen(
                    baselineRT = viewModel.baselineRT,
                    baselineDate = viewModel.baselineDate
                )
            }
        }
        return
    }

    if (showAbout) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("About", fontWeight = FontWeight.SemiBold) },
                    navigationIcon = {
                        IconButton(onClick = { showAbout = false }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
                )
            },
            containerColor = Color.Black
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                AboutScreen()
            }
        }
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                SectionHeader("Cognitive Games")
                SettingsRow(
                    icon = "👆",
                    title = "Reaction Time Test",
                    onClick = { showPVTDetail = true }
                )
                SettingsRow(
                    icon = "✨",
                    title = "More mini-games coming soon",
                    enabled = false
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            }
            item {
                SectionHeader("Data")
                SettingsRow(
                    icon = "📤",
                    title = "Export",
                    onClick = {
                        scope.launch {
                            try {
                                val records = withContext(Dispatchers.IO) {
                                    viewModel.getAllRecordsList()
                                }
                                val exportRecords = records.map { r ->
                                    ExportData.ExportRecord(
                                        startDate = r.startDate,
                                        isBaseline = r.isBaseline,
                                        isGuestMode = r.isGuestMode,
                                        guestName = r.guestName,
                                        trials = r.trials,
                                        averageRT = r.averageRT,
                                        lapseCount = r.lapseCount,
                                        baselineRT = r.baselineRT
                                    )
                                }
                                val data = ExportData(
                                    version = 1,
                                    exportedAt = System.currentTimeMillis(),
                                    baselineRT = viewModel.baselineRT,
                                    baselineDate = viewModel.baselineDate,
                                    guestMode = viewModel.guestMode,
                                    guestName = viewModel.guestName,
                                    records = exportRecords
                                )
                                val jsonStr = Gson().toJson(data)
                                val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                                val fileName = "CogniTrack_${sdf.format(Date())}.cognitrack"
                                val file = File(context.cacheDir, fileName)
                                withContext(Dispatchers.IO) { file.writeText(jsonStr) }
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    file
                                )
                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/json"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Export CogniTrack Data"))
                            } catch (e: Exception) {
                                errorMessage = "Export failed: ${e.localizedMessage}"
                                showErrorDialog = true
                            }
                        }
                    }
                )
                SettingsRow(
                    icon = "📥",
                    title = "Import",
                    onClick = { showImportDialog = true }
                )
                SettingsRow(
                    icon = "🗑️",
                    title = "Delete All Data",
                    isDestructive = true,
                    onClick = { showDeleteDialog = true }
                )
                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            }
            item {
                SettingsRow(
                    icon = "ℹ️",
                    title = "About",
                    onClick = { showAbout = true }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete All Data") },
            text = {
                Text("This will permanently delete all test history and reset your baseline. This cannot be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAllData()
                    showDeleteDialog = false
                    showSuccessDialog = "All test history and your baseline have been removed."
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false },
            title = { Text("Import Data") },
            text = {
                Text("All existing data will be removed and replaced with the imported data. Continue?")
            },
            confirmButton = {
                TextButton(onClick = {
                    showImportDialog = false
                    importLauncher.launch(arrayOf("application/json", "*/*"))
                }) {
                    Text("Import", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) { Text("OK") }
            }
        )
    }

    if (showSuccessDialog.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = "" },
            title = { Text("Success") },
            text = { Text(showSuccessDialog) },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = "" }) { Text("OK") }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.labelMedium,
        color = Color.White.copy(alpha = 0.5f),
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: String,
    title: String,
    isDestructive: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null && enabled) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(icon)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            title,
            style = MaterialTheme.typography.bodyLarge,
            color = when {
                isDestructive -> AppRed
                !enabled -> Color.White.copy(alpha = 0.4f)
                else -> Color.White
            },
            modifier = Modifier.weight(1f)
        )
        if (onClick != null && enabled) {
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
