package com.example.demoapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demoapp.data.DataEntry
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.input.KeyboardType
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlin.math.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import java.util.Calendar
import java.util.Date
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.text.style.TextOverflow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(
    viewModel: DataViewModel = viewModel(),
    onNavigateToEntry: () -> Unit
) {
    val entries by viewModel.allDataEntries.collectAsState()
    var minValue by remember { mutableStateOf("") }
    var maxValue by remember { mutableStateOf("") }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val context = LocalContext.current

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val appliedMinValue by viewModel.minValue.collectAsState()
    val appliedMaxValue by viewModel.maxValue.collectAsState()
    val startDateState by viewModel.startDate.collectAsState()
    val endDateState by viewModel.endDate.collectAsState()

    var localStartDate by remember { mutableStateOf<Long?>(null) }
    var localEndDate by remember { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Graph") },
                actions = {
                    FilledTonalIconButton(
                        onClick = onNavigateToEntry,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add New Data"
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Graph Area
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(
                    modifier = Modifier.padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (entries.isEmpty()) {
                        Text("No data available", textAlign = TextAlign.Center)
                    } else {
                        val filteredEntries = entries.filter { entry ->
                            (startDateState == null || entry.date >= startDateState!!) &&
                                    (endDateState == null || entry.date <= endDateState!!)
                        }
                        DataChart(
                            entries = filteredEntries,
                            minValue = appliedMinValue,
                            maxValue = appliedMaxValue,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Filter Controls Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Filter Options",
                        style = MaterialTheme.typography.titleMedium
                    )

                    // Y-axis value filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = minValue,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                                    minValue = input
                                }
                            },
                            label = { Text("Min Value") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = maxValue,
                            onValueChange = { input ->
                                if (input.isEmpty() || input.all { it.isDigit() || it == '.' }) {
                                    maxValue = input
                                }
                            },
                            label = { Text("Max Value") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    // Date range filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showStartDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = localStartDate?.let { "From: ${dateFormat.format(Date(it))}" }
                                    ?: "Select Start Date",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = { showEndDatePicker = true },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = localEndDate?.let { "To: ${dateFormat.format(Date(it))}" }
                                    ?: "Select End Date",
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = {
                                viewModel.setMinValue(minValue.toFloatOrNull())
                                viewModel.setMaxValue(maxValue.toFloatOrNull())
                                viewModel.setDateRange(localStartDate, localEndDate)
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Apply Filters")
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        OutlinedButton(
                            onClick = {
                                viewModel.setMinValue(null)
                                viewModel.setMaxValue(null)
                                viewModel.setDateRange(null, null)
                                minValue = ""
                                maxValue = ""
                                localStartDate = null
                                localEndDate = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Clear Filters")
                        }
                    }
                }
            }

            // Mobile version Add button
            Button(
                onClick = onNavigateToEntry,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Add New Data Entry", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Date picker dialogs
    if (showStartDatePicker) {
        ShowDatePicker(
            initialDate = localStartDate ?: System.currentTimeMillis(),
            onDismiss = { showStartDatePicker = false },
            onDateSelected = { millis ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = millis
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                localStartDate = calendar.timeInMillis
            }
        )
    }

    if (showEndDatePicker) {
        ShowDatePicker(
            initialDate = localEndDate ?: System.currentTimeMillis(),
            onDismiss = { showEndDatePicker = false },
            onDateSelected = { millis ->
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = millis
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                localEndDate = calendar.timeInMillis
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowDatePicker(
    initialDate: Long,
    onDismiss: () -> Unit,
    onDateSelected: (Long) -> Unit
) {
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDate)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    onDateSelected(millis)
                }
                onDismiss()
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@Composable
fun DataChart(
    entries: List<DataEntry>,
    minValue: Float?,
    maxValue: Float?,
    modifier: Modifier = Modifier
) {
    if (entries.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No data available")
        }
        return
    }

    val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())
    val minY = minValue ?: entries.minOfOrNull { it.value }?.let { it - it * 0.1f } ?: 0f
    val maxY = maxValue ?: entries.maxOfOrNull { it.value }?.let { it + it * 0.1f } ?: 1f
    val minX = entries.minOfOrNull { it.date } ?: 0L
    val maxX = entries.maxOfOrNull { it.date } ?: System.currentTimeMillis()

    // For showing tooltip
    var selectedPoint by remember { mutableStateOf<Pair<DataEntry, Offset>?>(null) }
    // Track if tooltip was shown by tap (to make it persistent)
    var isTooltipFromTap by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                // Handle taps
                .pointerInput(entries) {
                    detectTapGestures { offset ->
                        findClosestDataPoint(entries, offset, size, minX, maxX, minY, maxY)?.let {
                            selectedPoint = it
                            isTooltipFromTap = true
                        }
                    }
                }
                // Simulate hover with drag detection
                .pointerInput(entries) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            if (!isTooltipFromTap) {
                                findClosestDataPoint(entries, offset, size, minX, maxX, minY, maxY)?.let {
                                    selectedPoint = it
                                }
                            }
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            if (!isTooltipFromTap) {
                                findClosestDataPoint(entries, change.position, size, minX, maxX, minY, maxY)?.let {
                                    selectedPoint = it
                                }
                            }
                        },
                        onDragEnd = {
                            isTooltipFromTap = false
                        },
                        onDragCancel = {
                            isTooltipFromTap = false
                        }
                    )
                }
        ) {
            val width = size.width
            val height = size.height
            val padding = width * 0.1f

            drawAxesWithLabels(
                dateFormat = dateFormat,
                minY = minY, maxY = maxY,
                minX = minX, maxX = maxX,
                padding = padding
            )

            if (entries.size > 1) {
                drawDataPoints(
                    entries = entries,
                    minY = minY, maxY = maxY,
                    minX = minX, maxX = maxX,
                    padding = padding
                )
            }
        }

        // Tooltip for selected point
        selectedPoint?.let { (entry, position) ->
            Card(
                modifier = Modifier
                    .offset(
                        x = with(LocalDensity.current) { position.x.toDp() - 75.dp },
                        y = with(LocalDensity.current) { position.y.toDp() - 80.dp }
                    )
                    .width(150.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "Date: ${dateFormat.format(Date(entry.date))}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Value: ${entry.value}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Helper function to find closest data point
private fun findClosestDataPoint(
    entries: List<DataEntry>,
    position: Offset,
    size: androidx.compose.ui.unit.IntSize,
    minX: Long, maxX: Long,
    minY: Float, maxY: Float
): Pair<DataEntry, Offset>? {
    val width = size.width.toFloat()
    val height = size.height.toFloat()
    val padding = width * 0.1f
    val chartWidth = width - 2 * padding
    val chartHeight = height - 2 * padding

    return entries
        .map { entry ->
            val x = padding + ((entry.date - minX).toFloat() / (maxX - minX)) * chartWidth
            val normalizedValue = when {
                entry.value < minY -> minY
                entry.value > maxY -> maxY
                else -> entry.value
            }
            val y = height - padding - ((normalizedValue - minY) / (maxY - minY)) * chartHeight
            val distance = sqrt((position.x - x).pow(2) + (position.y - y).pow(2))
            Triple(entry, Offset(x, y), distance)
        }
        .filter { it.third < 50f } // Within 50px
        .minByOrNull { it.third }
        ?.let { Pair(it.first, it.second) }
}

private fun DrawScope.drawAxesWithLabels(
    dateFormat: SimpleDateFormat,
    minY: Float, maxY: Float,
    minX: Long, maxX: Long,
    padding: Float
) {
    val width = size.width
    val height = size.height

    // X and Y axes
    drawLine(
        color = Color.Gray,
        start = Offset(padding, height - padding),
        end = Offset(width - padding, height - padding),
        strokeWidth = 2f
    )

    drawLine(
        color = Color.Gray,
        start = Offset(padding, padding),
        end = Offset(padding, height - padding),
        strokeWidth = 2f
    )

    // Y-axis labels (5 values)
    val yRange = maxY - minY
    for (i in 0..4) {
        val value = minY + (yRange * i / 4f)
        val y = height - padding - (height - 2 * padding) * i / 4f

        drawContext.canvas.nativeCanvas.apply {
            drawText(
                String.format("%.1f", value),
                padding - 12f,  // Increased right margin for Y labels
                y + 5f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.GRAY
                    textSize = 12.sp.toPx()
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        // Horizontal grid line
        drawLine(
            color = Color.LightGray,
            start = Offset(padding, y),
            end = Offset(width - padding, y),
            strokeWidth = 0.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        )
    }

    // X-axis labels (dates)
    val timeRange = maxX - minX
    for (i in 0..4) {
        val time = minX + (timeRange * i / 4)
        val x = padding + (width - 2 * padding) * i / 4f

        // Skip label at origin to avoid overlap
        if (i > 0 || x > padding + 30f) {
            drawContext.canvas.nativeCanvas.apply {
                val date = dateFormat.format(Date(time))
                drawText(
                    date,
                    x,
                    height - padding + 20f,  // Increased bottom margin for X labels
                    android.graphics.Paint().apply {
                        color = android.graphics.Color.GRAY
                        textSize = 12.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }

        // Vertical grid line
        drawLine(
            color = Color.LightGray,
            start = Offset(x, padding),
            end = Offset(x, height - padding),
            strokeWidth = 0.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
        )
    }
}

private fun DrawScope.drawDataPoints(
    entries: List<DataEntry>,
    minY: Float, maxY: Float,
    minX: Long, maxX: Long,
    padding: Float
) {
    val width = size.width
    val height = size.height
    val chartWidth = width - 2 * padding
    val chartHeight = height - 2 * padding

    // Calculate point positions with clamping for out-of-range values
    val points = entries.map { entry ->
        val x = padding + ((entry.date - minX).toFloat() / (maxX - minX)) * chartWidth

        // Clamp y value to prevent going outside the chart area
        val normalizedValue = when {
            entry.value < minY -> minY
            entry.value > maxY -> maxY
            else -> entry.value
        }

        val y = height - padding - ((normalizedValue - minY) / (maxY - minY)) * chartHeight
        Triple(entry, Offset(x, y), entry.value < minY || entry.value > maxY)
    }

    // Draw lines between points
    for (i in 0 until points.size - 1) {
        drawLine(
            color = Color.Blue,
            start = points[i].second,
            end = points[i + 1].second,
            strokeWidth = 2f
        )
    }

    // Draw points as circles
    points.forEach { (entry, point, isOutOfRange) ->
        // Draw different indicator for out-of-range points
        if (isOutOfRange) {
            // Draw a diamond shape for out-of-range points
            val radius = 8f
            drawCircle(
                color = Color.Red,
                radius = 6f,
                center = point
            )

            // Optional: Draw small triangular indicator to show if value is above or below range
            if (entry.value > maxY) {
                drawLine(
                    color = Color.Red,
                    start = Offset(point.x, point.y - 8f),
                    end = Offset(point.x - 5f, point.y),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color.Red,
                    start = Offset(point.x, point.y - 8f),
                    end = Offset(point.x + 5f, point.y),
                    strokeWidth = 2f
                )
            } else if (entry.value < minY) {
                drawLine(
                    color = Color.Red,
                    start = Offset(point.x, point.y + 8f),
                    end = Offset(point.x - 5f, point.y),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color.Red,
                    start = Offset(point.x, point.y + 8f),
                    end = Offset(point.x + 5f, point.y),
                    strokeWidth = 2f
                )
            }
        } else {
            // Normal in-range point
            drawCircle(
                color = Color.Blue,
                radius = 6f,
                center = point
            )
        }
    }
}