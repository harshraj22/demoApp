package com.example.demoapp

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import com.example.demoapp.data.DataEntry
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.text.font.FontWeight
import android.os.Handler
import android.os.Looper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogDataScreen(
    viewModel: DataViewModel = viewModel(),
    onNavigateToGraph: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
    var showClearAllDialog by remember { mutableStateOf(false) }

    // State for the screen
    var value by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showReplacementDialog by remember { mutableStateOf(false) }
    var existingEntry by remember { mutableStateOf<DataEntry?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    // Format validation
    val isValueValid = value.isEmpty() || value.toFloatOrNull() != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log New Data") },
                actions = {
                    IconButton(onClick = { showClearAllDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Clear All Data"
                        )
                    }
                    // Add this action button to navigate to graph
                    IconButton(onClick = onNavigateToGraph) {
                        Icon(
                            Icons.Default.Info, // or Icons.Default.ShowChart
                            contentDescription = "View Graph"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Card for date selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Select Date",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 16.dp)
                        )

                        Button(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = dateFormat.format(selectedDate.time),
                                modifier = Modifier.padding(vertical = 8.dp),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }

            // Card for value entry
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        "Enter Value",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )

                    OutlinedTextField(
                        value = value,
                        onValueChange = {
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                value = it
                            }
                        },
                        label = { Text("Value") },
                        placeholder = { Text("Enter numeric value") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isValueValid,
                        supportingText = {
                            if (!isValueValid) {
                                Text("Please enter a valid number")
                            }
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Create, // or Icons.Default.Input or Icons.Default.Tag
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true
                    )
                }
            }

            // Success message
            AnimatedVisibility(
                visible = showSuccessMessage,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            "Data saved successfully!",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save button
            Button(
                onClick = {
                    value.toFloatOrNull()?.let { numericValue ->
                        viewModel.checkAndInsertDataEntry(
                            date = selectedDate.timeInMillis,
                            value = numericValue,
                            onExistingEntry = { entry ->
                                existingEntry = entry
                                showReplacementDialog = true
                            },
                            onNewEntryInserted = {
                                showSuccessMessage = true
                                // Hide success message after delay
                                Handler(Looper.getMainLooper()).postDelayed({
                                    showSuccessMessage = false
                                }, 3000)
                                // Clear input
                                value = ""
                            }
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = value.isNotEmpty() && isValueValid
            ) {
                Icon(
                    Icons.Default.Done,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    "SAVE DATA",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        ShowDatePicker(
            initialDate = selectedDate.timeInMillis,
            onDismiss = { showDatePicker = false },
            onDateSelected = { millis ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = millis
                calendar.set(Calendar.HOUR_OF_DAY, 12)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                selectedDate = calendar
            }
        )
    }

    // Replacement dialog
    if (showReplacementDialog) {
        AlertDialog(
            onDismissRequest = { showReplacementDialog = false },
            title = { Text("Data Already Exists") },
            text = {
                Column {
                    Text("Data already exists for ${dateFormat.format(selectedDate.time)}.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Existing value: ${existingEntry?.value}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("New value: $value")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Would you like to replace it?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        value.toFloatOrNull()?.let { newValue ->
                            existingEntry?.let { entry ->
                                viewModel.deleteDataEntry(entry)
                                viewModel.insertDataEntry(selectedDate.timeInMillis, newValue)
                                showSuccessMessage = true
                                Handler(Looper.getMainLooper()).postDelayed({
                                    showSuccessMessage = false
                                }, 3000)
                                value = ""
                            }
                            showReplacementDialog = false
                        }
                    }
                ) {
                    Text("Replace")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showReplacementDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add confirmation dialog for clearing all data
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            title = { Text("Clear All Data") },
            text = { Text("Are you sure you want to delete all data? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllData()
                        showClearAllDialog = false
                        // Show success message
                        showSuccessMessage = true
                        Handler(Looper.getMainLooper()).postDelayed({
                            showSuccessMessage = false
                        }, 3000)
                    }
                ) {
                    Text("Delete All")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}