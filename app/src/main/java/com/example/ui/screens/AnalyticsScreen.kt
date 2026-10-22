package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.theme.CncCostLight
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

@Composable
fun AnalyticsScreen(
    viewModel: CncViewModel
) {
    val allDesigns by viewModel.allDesigns.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    var selectedTimeframeIndex by remember { mutableIntStateOf(0) } // 0: 7 Days, 1: 30 Days, 2: All Time

    val timeframeFilteredDesigns = remember(allDesigns, selectedTimeframeIndex) {
        val now = System.currentTimeMillis()
        val limitMillis = when (selectedTimeframeIndex) {
            0 -> 7L * 24 * 3600 * 1000
            1 -> 30L * 24 * 3600 * 1000
            else -> Long.MAX_VALUE
        }
        allDesigns.filter { (now - it.timestampMillis) <= limitMillis }
    }

    val totalRevenue = timeframeFilteredDesigns.sumOf { it.sellingPrice }
    val totalCost = timeframeFilteredDesigns.sumOf { it.productionCost }
    val netProfit = totalRevenue - totalCost
    val avgMargin = if (totalRevenue > 0) (netProfit / totalRevenue) * 100.0 else 0.0
    val totalMinutes = timeframeFilteredDesigns.sumOf { it.machineTimeMinutes }
    val hourlyProfit = if (totalMinutes > 0) (netProfit / (totalMinutes / 60.0)) else 0.0

    // Top categories
    val categoryTotals = remember(timeframeFilteredDesigns) {
        timeframeFilteredDesigns.groupBy { it.category }
            .mapValues { entry ->
                val revenue = entry.value.sumOf { it.sellingPrice }
                val profit = entry.value.sumOf { it.profit }
                Pair(revenue, profit)
            }.toList().sortedByDescending { it.second.first }
    }

    // Top designs
    val topDesigns = remember(timeframeFilteredDesigns) {
        timeframeFilteredDesigns.sortedByDescending { it.profit }.take(5)
    }

    // Daily bars data for last 7 days
    val last7DaysData = remember(allDesigns) {
        val list = mutableListOf<Triple<String, Double, Double>>() // DayLabel, Revenue, Profit
        val cal = Calendar.getInstance()
        val dayFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val labelFmt = SimpleDateFormat("EEE", Locale.getDefault())

        for (i in 6 downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            val dStr = dayFmt.format(c.time)
            val label = labelFmt.format(c.time)
            val dayItems = allDesigns.filter { it.dateString == dStr }
            val dayRev = dayItems.sumOf { it.sellingPrice }
            val dayProf = dayItems.sumOf { it.profit }
            list.add(Triple(label, dayRev, dayProf))
        }
        list
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("analytics_screen")
    ) {
        CadGridBackground()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "FINANCIAL & RUNTIME ANALYTICS",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = CncTextPrimary
                        )
                        Text(
                            text = "Track earnings, profitability and CNC utilization",
                            style = MaterialTheme.typography.bodySmall,
                            color = CncTextSecondary
                        )
                    }
                    AnimatedCncCutter(size = 28.dp)
                }
            }

            // Timeframe Selector Tabs
            item {
                val tabs = listOf("Last 7 Days", "Last 30 Days", "All History")
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CncSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CncBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val isSelected = selectedTimeframeIndex == index
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) CncPrimary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTimeframeIndex = index }
                            ) {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else CncTextSecondary,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Key KPI Metric Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FuturisticMetricCard(
                        title = "Total Net Profit",
                        value = "$currencySymbol${String.format(Locale.US, "%.0f", netProfit)}",
                        subtitle = "+${String.format(Locale.US, "%.1f", avgMargin)}% Avg Margin",
                        icon = Icons.Default.TrendingUp,
                        accentColor = CncProfit,
                        modifier = Modifier.weight(1f)
                    )

                    FuturisticMetricCard(
                        title = "Total Revenue",
                        value = "$currencySymbol${String.format(Locale.US, "%.0f", totalRevenue)}",
                        subtitle = "${timeframeFilteredDesigns.size} designs sold",
                        icon = Icons.Default.MonetizationOn,
                        accentColor = CncPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FuturisticMetricCard(
                        title = "Profit / Cut Hour",
                        value = "$currencySymbol${String.format(Locale.US, "%.0f", hourlyProfit)}/h",
                        subtitle = "${totalMinutes / 60} hrs machine runtime",
                        icon = Icons.Default.Timer,
                        accentColor = CncLaserCyan,
                        modifier = Modifier.weight(1f)
                    )

                    FuturisticMetricCard(
                        title = "Total Shop Costs",
                        value = "$currencySymbol${String.format(Locale.US, "%.0f", totalCost)}",
                        subtitle = "Materials & tools",
                        icon = Icons.Default.Engineering,
                        accentColor = CncCost,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Interactive Weekly Performance Chart
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "7-DAY EARNINGS SPECTRUM",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        letterSpacing = 1.sp,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = CncTextPrimary
                                )
                                Text(
                                    text = "Daily Revenue vs Net Profit Bars",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CncTextSecondary
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(CncPrimary, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Revenue", style = MaterialTheme.typography.labelSmall, color = CncTextSecondary)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(CncProfit, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Profit", style = MaterialTheme.typography.labelSmall, color = CncTextSecondary)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Custom Bar Chart Canvas
                        var selectedBarIndex by remember { mutableStateOf<Int?>(null) }
                        val animProgress = remember { Animatable(0f) }

                        LaunchedEffect(last7DaysData) {
                            animProgress.snapTo(0f)
                            animProgress.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Canvas(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(Unit) {
                                        detectTapGestures { offset ->
                                            val barWidth = size.width / 7
                                            val index = (offset.x / barWidth).toInt().coerceIn(0, 6)
                                            selectedBarIndex = if (selectedBarIndex == index) null else index
                                        }
                                    }
                            ) {
                                val canvasWidth = size.width
                                val canvasHeight = size.height - 30.dp.toPx()
                                val maxVal = (last7DaysData.maxOfOrNull { it.second } ?: 1.0).coerceAtLeast(100.0)

                                val groupWidth = canvasWidth / 7
                                val barWidth = groupWidth * 0.32f

                                // Baseline
                                drawLine(
                                    color = CncBorder,
                                    start = Offset(0f, canvasHeight),
                                    end = Offset(canvasWidth, canvasHeight),
                                    strokeWidth = 2f
                                )

                                for (i in 0 until 7) {
                                    val (dayLabel, rev, prof) = last7DaysData[i]
                                    val centerX = i * groupWidth + groupWidth / 2

                                    val revHeight = ((rev / maxVal) * canvasHeight * animProgress.value).toFloat()
                                    val profHeight = ((prof / maxVal) * canvasHeight * animProgress.value).toFloat()

                                    // Draw Revenue Bar
                                    val revLeft = centerX - barWidth - 2.dp.toPx()
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            listOf(CncLaserCyan, CncPrimary)
                                        ),
                                        topLeft = Offset(revLeft, canvasHeight - revHeight),
                                        size = Size(barWidth, revHeight),
                                        cornerRadius = CornerRadius(6f, 6f)
                                    )

                                    // Draw Profit Bar
                                    val profLeft = centerX + 2.dp.toPx()
                                    drawRoundRect(
                                        brush = Brush.verticalGradient(
                                            listOf(Color(0xFF34D399), CncProfit)
                                        ),
                                        topLeft = Offset(profLeft, canvasHeight - profHeight),
                                        size = Size(barWidth, profHeight),
                                        cornerRadius = CornerRadius(6f, 6f)
                                    )
                                }
                            }

                            // Day labels below
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                for ((index, item) in last7DaysData.withIndex()) {
                                    val isSelected = selectedBarIndex == index
                                    Text(
                                        text = item.first,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                                        ),
                                        color = if (isSelected) CncPrimary else CncTextSecondary,
                                        modifier = Modifier.weight(1f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        if (selectedBarIndex != null) {
                            val target = last7DaysData[selectedBarIndex!!]
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = CncSurfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    Text("${target.first}:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                                    Text("Revenue: $currencySymbol${String.format(Locale.US, "%.0f", target.second)}", style = MaterialTheme.typography.bodySmall)
                                    Text("Profit: +$currencySymbol${String.format(Locale.US, "%.0f", target.third)}", style = MaterialTheme.typography.bodySmall.copy(color = CncProfit, fontWeight = FontWeight.Bold))
                                }
                            }
                        }
                    }
                }
            }

            // Category Distribution Card
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
                        Text(
                            text = "EARNINGS BY CATEGORY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Black
                            ),
                            color = CncTextPrimary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (categoryTotals.isEmpty()) {
                            Text("No category data logged yet.", style = MaterialTheme.typography.bodySmall, color = CncTextSecondary)
                        } else {
                            val maxCatRevenue = categoryTotals.maxOfOrNull { it.second.first } ?: 1.0

                            categoryTotals.forEach { (cat, revenueProfit) ->
                                val (rev, prof) = revenueProfit
                                val ratio = if (maxCatRevenue > 0) (rev / maxCatRevenue).toFloat() else 0f

                                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = cat,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                            color = CncTextPrimary
                                        )
                                        Text(
                                            text = "$currencySymbol${String.format(Locale.US, "%.0f", rev)} (Profit: +$currencySymbol${String.format(Locale.US, "%.0f", prof)})",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Medium
                                            ),
                                            color = CncTextSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { ratio },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(CircleShape),
                                        color = CncPrimary,
                                        trackColor = CncSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Top Profitable Designs Leaderboard
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = CncCost, modifier = Modifier.size(18.dp))
                            Text(
                                text = "TOP PROFITABLE CNC DESIGNS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Black
                                ),
                                color = CncTextPrimary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        topDesigns.forEachIndexed { rank, design ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (rank == 0) CncCostLight else CncSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "#${rank + 1}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp
                                            ),
                                            color = if (rank == 0) CncCost else CncTextSecondary
                                        )
                                    }
                                }

                                CncImageThumbnail(
                                    imageUri = design.imageUri,
                                    category = design.category,
                                    modifier = Modifier.size(36.dp)
                                )

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = design.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        maxLines = 1,
                                        color = CncTextPrimary
                                    )
                                    Text(
                                        text = "${design.category} • ${design.dateString}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CncTextSecondary,
                                        fontSize = 10.sp
                                    )
                                }

                                Text(
                                    text = "+$currencySymbol${String.format(Locale.US, "%.0f", design.profit)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Black
                                    ),
                                    color = CncProfit
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
