package com.diffusethinking.cognitrack.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.diffusethinking.cognitrack.data.TestRecord
import com.diffusethinking.cognitrack.ui.MainViewModel
import com.diffusethinking.cognitrack.ui.components.RecordRow
import com.diffusethinking.cognitrack.ui.theme.AppCyan
import com.diffusethinking.cognitrack.ui.theme.AppGreen
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    records: List<TestRecord>,
    viewModel: MainViewModel
) {
    var showGuestTests by remember { mutableStateOf(true) }
    var selectedRecord by remember { mutableStateOf<TestRecord?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var recordToDelete by remember { mutableStateOf<TestRecord?>(null) }

    if (selectedRecord != null) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        val rec = selectedRecord!!
                        Text(
                            when {
                                rec.isBaseline -> "Baseline"
                                rec.isGuestMode -> "Guest"
                                else -> "Quick Check"
                            },
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectedRecord = null }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            recordToDelete = selectedRecord
                            showDeleteDialog = true
                        }) {
                            Icon(Icons.Filled.Delete, "Delete", tint = Color.Red)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Black
                    )
                )
            },
            containerColor = Color.Black
        ) { padding ->
            Box(modifier = Modifier.padding(padding)) {
                ResultsScreenForRecord(
                    record = selectedRecord!!,
                    viewModel = viewModel,
                    onDone = { selectedRecord = null }
                )
            }
        }
    } else {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("History", fontWeight = FontWeight.SemiBold) },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Black
                    )
                )
            },
            containerColor = Color.Black
        ) { padding ->
            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(48.dp)
                    ) {
                        Text("🕐", fontSize = 64.sp)
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            "No tests yet",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Complete a baseline or quick check to see results here.",
                            color = Color.White.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                val hasGuestTests = records.any { it.isGuestMode }
                val filteredRecords = if (showGuestTests) records else records.filter { !it.isGuestMode }
                val groupedByDay = groupRecordsByDay(filteredRecords)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    if (hasGuestTests) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Show guest tests",
                                    modifier = Modifier.weight(1f),
                                    color = Color.White
                                )
                                Switch(
                                    checked = showGuestTests,
                                    onCheckedChange = { showGuestTests = it },
                                    colors = SwitchDefaults.colors(
                                        checkedTrackColor = AppGreen,
                                        checkedThumbColor = Color.White
                                    )
                                )
                            }
                            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                        }
                    }

                    groupedByDay.forEach { (day, dayRecords) ->
                        item {
                            val sdf = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                            Text(
                                sdf.format(Date(day)),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                            )
                        }
                        items(dayRecords, key = { it.id }) { record ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedRecord = record }
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RecordRow(
                                    record = record,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    Icons.Filled.ChevronRight,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 48.dp),
                                color = Color.White.copy(alpha = 0.08f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                recordToDelete = null
            },
            title = { Text("Delete Record") },
            text = { Text("This record will be permanently deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    recordToDelete?.let {
                        viewModel.deleteRecord(it)
                        if (selectedRecord?.id == it.id) selectedRecord = null
                    }
                    showDeleteDialog = false
                    recordToDelete = null
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    recordToDelete = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun groupRecordsByDay(records: List<TestRecord>): List<Pair<Long, List<TestRecord>>> {
    val calendar = Calendar.getInstance()
    val grouped = records.groupBy { record ->
        calendar.timeInMillis = record.startDate
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.timeInMillis
    }
    return grouped.entries
        .sortedByDescending { it.key }
        .map { it.key to it.value }
}
