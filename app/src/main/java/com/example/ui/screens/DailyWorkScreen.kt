package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DoorFront
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CncDesignEntity
import com.example.ui.CncViewModel
import com.example.ui.components.AnimatedCncCutter
import com.example.ui.components.CadGridBackground
import com.example.ui.components.CncImageThumbnail
import com.example.ui.components.FuturisticMetricCard
import com.example.ui.theme.CncBorder
import com.example.ui.theme.CncBorderBright
import com.example.ui.theme.CncCost
import com.example.ui.theme.CncLaserCyan
import com.example.ui.theme.CncPrimary
import com.example.ui.theme.CncProfit
import com.example.ui.theme.CncProfitLight
import com.example.ui.theme.CncSurface
import com.example.ui.theme.CncSurfaceVariant
import com.example.ui.theme.CncTextPrimary
import com.example.ui.theme.CncTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyWorkScreen(
    viewModel: CncViewModel,
    onAddNewDesign: () -> Unit,
    onEditDesign: (CncDesignEntity) -> Unit
) {
    val selectedDate by viewModel.selectedDate.collectAsState()
    val dailyDesigns by viewModel.dailyDesigns.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    var designToDelete by remember { mutableStateOf<CncDesignEntity?>(null) }
    var selectedDesignDetails by remember { mutableStateOf<CncDesignEntity?>(null) }

    val categories = listOf("All", "Door", "Jali / Grill", "3D Relief", "Wall Panel", "Engraving", "Furniture")

    val totalRevenue = dailyDesigns.sumOf { it.sellingPrice }
    val totalCost = dailyDesigns.sumOf { it.productionCost }
    val netProfit = totalRevenue - totalCost
    val margin = if (totalRevenue > 0) (netProfit / totalRevenue) * 100.0 else 0.0
    val totalMinutes = dailyDesigns.sumOf { it.machineTimeMinutes }

    val todayStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()) }
    val isToday = selectedDate == todayStr

    val displayDateFormatted = remember(selectedDate) {
        try {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatter = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
            val parsed = parser.parse(selectedDate)
            if (parsed != null) formatter.format(parsed) else selectedDate
        } catch (_: Exception) {
            selectedDate
        }
    }

    // Generate 7 carousel days centered around selectedDate
    val carouselDays = remember(selectedDate) {
        val list = mutableListOf<Pair<String, Date>>()
        val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val baseDate = dayFmt.parse(selectedDate) ?: Date()
            val cal = Calendar.getInstance()
            for (offset in -3..3) {
                cal.time = baseDate
                cal.add(Calendar.DAY_OF_YEAR, offset)
                list.add(Pair(dayFmt.format(cal.time), cal.time))
            }
        } catch (_: Exception) {
            val cal = Calendar.getInstance()
            for (offset in -3..3) {
                cal.time = Date()
                cal.add(Calendar.DAY_OF_YEAR, offset)
                list.add(Pair(dayFmt.format(cal.time), cal.time))
            }
        }
        list
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("daily_work_screen")
    ) {
        CadGridBackground()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // High-Tech Header & Date Carousel
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CncSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CncBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Title bar
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                AnimatedCncCutter(size = 22.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DAILY WORK TELEMETRY",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.2.sp,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = CncPrimary
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!isToday) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = CncPrimary.copy(alpha = 0.1f),
                                        modifier = Modifier.clickable { viewModel.setToday() }
                                    ) {
                                        Text(
                                            text = "Today",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = CncPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Surface(
                                    shape = CircleShape,
                                    color = CncSurfaceVariant,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clickable { showDatePicker = true }
                                        .testTag("open_date_picker_btn")
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Pick Date",
                                            tint = CncPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = displayDateFormatted,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.2).sp
                            ),
                            color = CncTextPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Interactive 7-Day Date Carousel
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val dayLetterFmt = SimpleDateFormat("EEE", Locale.getDefault())
                            val dayNumFmt = SimpleDateFormat("d", Locale.getDefault())

                            carouselDays.forEach { (dateStr, dateObj) ->
                                val isSelected = dateStr == selectedDate
                                val isCurrentToday = dateStr == todayStr

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) CncPrimary else if (isCurrentToday) CncPrimary.copy(alpha = 0.1f) else CncSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) CncLaserCyan else CncBorder
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 2.dp)
                                        .clickable { viewModel.setSelectedDate(dateStr) }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = dayLetterFmt.format(dateObj).uppercase(Locale.getDefault()),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = if (isSelected) Color.White.copy(alpha = 0.9f) else CncTextSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = dayNumFmt.format(dateObj),
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                fontFamily = FontFamily.Monospace
                                            ),
                                            color = if (isSelected) Color.White else CncTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Key Daily Summary Metrics Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FuturisticMetricCard(
                        title = "Today Net Profit",
                        value = "$currencySymbol${String.format(Locale.US, "%.0f", netProfit)}",
                        subtitle = if (totalRevenue > 0) "+${String.format(Locale.US, "%.1f", margin)}% margin" else "0% margin",
                        icon = Icons.Default.TrendingUp,
                        accentColor = CncProfit,
                        modifier = Modifier.weight(1f),
                        testTag = "daily_net_profit_card"
                    )

                    FuturisticMetricCard(
                        title = "Designs Completed",
                        value = "${dailyDesigns.size}",
                        subtitle = if (totalMinutes > 0) "${totalMinutes / 60}h ${totalMinutes % 60}m cut" else "No run time",
                        icon = Icons.Default.Engineering,
                        accentColor = CncPrimary,
                        modifier = Modifier.weight(1f),
                        testTag = "daily_designs_count_card"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FuturisticMetricCard(
                        title = "Gross Revenue",
                        value = "$currencySymbol${String.format(Locale.US, "%.0f", totalRevenue)}",
                        subtitle = "Client billing total",
                        icon = Icons.Default.MonetizationOn,
                        accentColor = CncLaserCyan,
                        modifier = Modifier.weight(1f),
                        testTag = "daily_gross_revenue_card"
                    )

                    FuturisticMetricCard(
                        title = "Production Cost",
                        value = "$currencySymbol${String.format(Locale.US, "%.0f", totalCost)}",
                        subtitle = "Materials & tooling wear",
                        icon = Icons.Default.Schedule,
                        accentColor = CncCost,
                        modifier = Modifier.weight(1f),
                        testTag = "daily_production_cost_card"
                    )
                }
            }

            // Category Filter Chips
            item {
                Column {
                    Text(
                        text = "FILTER DESIGNS BY CATEGORY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CncTextSecondary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { cat ->
                            val isSelected = if (cat == "All") selectedCategory == null else selectedCategory.equals(cat, ignoreCase = true)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.selectedCategoryFilter.value = if (cat == "All") null else cat
                                },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = CncPrimary,
                                    selectedLabelColor = Color.White,
                                    containerColor = CncSurface,
                                    labelColor = CncTextPrimary
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COMPLETED WORK ENTRIES (${dailyDesigns.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.Black
                        ),
                        color = CncTextPrimary
                    )

                    if (dailyDesigns.isNotEmpty()) {
                        Text(
                            text = "Tap item to view specs",
                            style = MaterialTheme.typography.labelSmall,
                            color = CncTextSecondary
                        )
                    }
                }
            }

            // Empty State
            if (dailyDesigns.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CncSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CncBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = CncPrimary.copy(alpha = 0.08f),
                                modifier = Modifier.size(72.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.PrecisionManufacturing,
                                        contentDescription = null,
                                        tint = CncPrimary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "No Designs Logged for $selectedDate",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CncTextPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Finished creating a CNC door, jali screen, or 3D carving? Tap below to log design specifications, cost, and income.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CncTextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(18.dp))

                            Button(
                                onClick = onAddNewDesign,
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CncPrimary),
                                modifier = Modifier.testTag("empty_state_add_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Log Completed Design")
                            }
                        }
                    }
                }
            } else {
                items(dailyDesigns, key = { it.id }) { design ->
                    CncDesignCard(
                        design = design,
                        currencySymbol = currencySymbol,
                        onClick = { selectedDesignDetails = design },
                        onEdit = { onEditDesign(design) },
                        onDuplicate = { viewModel.duplicateDesign(design) },
                        onDelete = { designToDelete = design }
                    )
                }
            }
        }

        // Floating Action Button to add design
        FloatingActionButton(
            onClick = onAddNewDesign,
            containerColor = CncPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_design_fab")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Design")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log Design",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val picked = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                            viewModel.setSelectedDate(picked)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Select Date")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Delete confirmation dialog
    if (designToDelete != null) {
        val target = designToDelete!!
        AlertDialog(
            onDismissRequest = { designToDelete = null },
            title = { Text("Delete CNC Design?") },
            text = { Text("Are you sure you want to remove '${target.title}' from your daily records? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteDesign(target)
                        designToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { designToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Detailed Design Inspection Dialog
    if (selectedDesignDetails != null) {
        val detail = selectedDesignDetails!!
        AlertDialog(
            onDismissRequest = { selectedDesignDetails = null },
            title = {
                Column {
                    Text(
                        text = detail.category.uppercase(Locale.getDefault()),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = CncPrimary,
                            fontWeight = FontWeight.Black
                        )
                    )
                    Text(
                        text = detail.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CncImageThumbnail(
                        imageUri = detail.imageUri,
                        category = detail.category,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Selling Price", style = MaterialTheme.typography.labelSmall, color = CncTextSecondary)
                            Text(
                                "$currencySymbol${String.format(Locale.US, "%.2f", detail.sellingPrice)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            )
                        }
                        Column {
                            Text("Production Cost", style = MaterialTheme.typography.labelSmall, color = CncTextSecondary)
                            Text(
                                "$currencySymbol${String.format(Locale.US, "%.2f", detail.productionCost)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = CncCost
                                )
                            )
                        }
                        Column {
                            Text("Net Profit", style = MaterialTheme.typography.labelSmall, color = CncTextSecondary)
                            Text(
                                "$currencySymbol${String.format(Locale.US, "%.2f", detail.profit)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = CncProfit
                                )
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CncSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            if (detail.material.isNotBlank()) Text("Material: ${detail.material}", style = MaterialTheme.typography.bodySmall)
                            if (detail.dimensions.isNotBlank()) Text("Dimensions: ${detail.dimensions}", style = MaterialTheme.typography.bodySmall)
                            if (detail.bitType.isNotBlank()) Text("Tooling Bit: ${detail.bitType}", style = MaterialTheme.typography.bodySmall)
                            if (detail.machineTimeMinutes > 0) Text("Cut Time: ${detail.machineTimeMinutes} minutes", style = MaterialTheme.typography.bodySmall)
                            if (detail.clientName.isNotBlank()) Text("Client: ${detail.clientName}", style = MaterialTheme.typography.bodySmall)
                            if (detail.notes.isNotBlank()) Text("Notes: ${detail.notes}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedDesignDetails = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun CncDesignCard(
    design: CncDesignEntity,
    currencySymbol: String,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CncSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CncBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("cnc_design_card_${design.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Design Preview Image / Schematic
                CncImageThumbnail(
                    imageUri = design.imageUri,
                    category = design.category,
                    modifier = Modifier.size(72.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CncPrimary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = design.category.uppercase(Locale.getDefault()),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = CncPrimary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        if (design.machineTimeMinutes > 0) {
                            Text(
                                text = "${design.machineTimeMinutes}m cut",
                                style = MaterialTheme.typography.labelSmall,
                                color = CncTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = design.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = CncTextPrimary,
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    if (design.dimensions.isNotBlank() || design.material.isNotBlank()) {
                        Text(
                            text = listOf(design.dimensions, design.material).filter { it.isNotBlank() }.joinToString(" • "),
                            style = MaterialTheme.typography.bodySmall,
                            color = CncTextSecondary,
                            maxLines = 1,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Income, Cost, and Profit bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                    .border(1.dp, CncBorderBright, RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text("PRICE", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = CncTextSecondary)
                        Text(
                            "$currencySymbol${String.format(Locale.US, "%.0f", design.sellingPrice)}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                            color = CncTextPrimary
                        )
                    }

                    Column {
                        Text("COST", style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = CncTextSecondary)
                        Text(
                            "$currencySymbol${String.format(Locale.US, "%.0f", design.productionCost)}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold),
                            color = CncCost
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = CncProfitLight
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PROFIT: +$currencySymbol${String.format(Locale.US, "%.0f", design.profit)}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = CncProfit
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${String.format(Locale.US, "%.0f", design.profitMarginPercent)}%)",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            color = CncProfit
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDuplicate,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Duplicate Design",
                        tint = CncTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Design",
                        tint = CncPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
