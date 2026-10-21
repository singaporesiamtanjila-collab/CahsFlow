package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.CncDesignEntity
import com.example.ui.components.CncImageThumbnail
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
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDesignSheet(
    initialDesign: CncDesignEntity? = null,
    defaultDate: String,
    currencySymbol: String = "$",
    onDismiss: () -> Unit,
    onSave: (CncDesignEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf(initialDesign?.title ?: "") }
    var category by remember { mutableStateOf(initialDesign?.category ?: "Door") }
    var priceText by remember { mutableStateOf(initialDesign?.sellingPrice?.toString()?.replace(".0", "") ?: "") }
    var costText by remember { mutableStateOf(initialDesign?.productionCost?.toString()?.replace(".0", "") ?: "") }
    var imageUri by remember { mutableStateOf(initialDesign?.imageUri ?: "preset:door") }

    var machineTimeText by remember { mutableStateOf(initialDesign?.machineTimeMinutes?.takeIf { it > 0 }?.toString() ?: "") }
    var bitType by remember { mutableStateOf(initialDesign?.bitType ?: "Ball Nose 6mm") }
    var material by remember { mutableStateOf(initialDesign?.material ?: "Solid Wood") }
    var clientName by remember { mutableStateOf(initialDesign?.clientName ?: "") }
    var dimensions by remember { mutableStateOf(initialDesign?.dimensions ?: "7ft x 3.5ft") }
    var notes by remember { mutableStateOf(initialDesign?.notes ?: "") }

    var showAdvanced by remember { mutableStateOf(initialDesign != null) }

    val categories = listOf("Door", "Jali / Grill", "3D Relief", "Wall Panel", "Engraving", "Furniture", "Custom")

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri.toString()
        }
    }

    val sellingPrice = priceText.toDoubleOrNull() ?: 0.0
    val productionCost = costText.toDoubleOrNull() ?: 0.0
    val profit = sellingPrice - productionCost
    val margin = if (sellingPrice > 0) (profit / sellingPrice) * 100.0 else 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CncSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(44.dp)
                    .height(4.dp)
                    .clip(CircleShape)
                    .background(CncBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("add_edit_design_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (initialDesign == null) "NEW CNC DESIGN ENTRY" else "EDIT CNC DESIGN",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = CncTextPrimary
                    )
                    Text(
                        text = "Log design specifications, cost and earnings",
                        style = MaterialTheme.typography.bodySmall,
                        color = CncTextSecondary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_sheet_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = CncTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real-time Profit Preview Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, CncBorderBright),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CALCULATED NET PROFIT",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = CncTextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%.2f", profit)}",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (profit >= 0) CncProfit else MaterialTheme.colorScheme.error
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (profit >= 0) CncProfitLight else Color(0xFFFFE4E6),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Text(
                            text = "${if (profit >= 0) "+" else ""}${String.format(Locale.US, "%.1f", margin)}% MARGIN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = if (profit >= 0) CncProfit else Color(0xFFE11D48),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Design Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Design Title (e.g. Royal Double Door 3D)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_design_title"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CncPrimary,
                    unfocusedBorderColor = CncBorder
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category Selector
            Text(
                text = "CATEGORY / WORK TYPE",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = CncTextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = category.equals(cat, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { category = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = CncPrimary,
                            selectedLabelColor = Color.White,
                            containerColor = CncSurfaceVariant,
                            labelColor = CncTextPrimary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Price & Cost Inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Selling Price ($currencySymbol)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_selling_price"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CncPrimary,
                        unfocusedBorderColor = CncBorder
                    ),
                    singleLine = true
                )

                OutlinedTextField(
                    value = costText,
                    onValueChange = { costText = it },
                    label = { Text("Total Cost ($currencySymbol)") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_production_cost"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CncCost,
                        unfocusedBorderColor = CncBorder
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Image Selection
            Text(
                text = "DESIGN IMAGE & PREVIEW",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = CncTextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CncImageThumbnail(
                    imageUri = imageUri,
                    modifier = Modifier.size(76.dp)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CncSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pick_image_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = CncPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Upload Design Photo",
                            color = CncPrimary,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (imageUri == "preset:door") CncBorderBright else CncSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { imageUri = "preset:door" }
                        ) {
                            Text(
                                text = "Door",
                                style = MaterialTheme.typography.labelSmall,
                                color = CncTextPrimary,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (imageUri == "preset:panel") CncBorderBright else CncSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { imageUri = "preset:panel" }
                        ) {
                            Text(
                                text = "Jali",
                                style = MaterialTheme.typography.labelSmall,
                                color = CncTextPrimary,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (imageUri == "preset:banner") CncBorderBright else CncSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { imageUri = "preset:banner" }
                        ) {
                            Text(
                                text = "CAD",
                                style = MaterialTheme.typography.labelSmall,
                                color = CncTextPrimary,
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Expandable Technical Specs Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showAdvanced = !showAdvanced }
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showAdvanced) "HIDE CNC MACHINE SPECS ▲" else "ADD CNC MACHINE & MATERIAL SPECS ▼",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = CncPrimary
                )
            }

            AnimatedVisibility(visible = showAdvanced) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = dimensions,
                            onValueChange = { dimensions = it },
                            label = { Text("Dimensions (e.g. 7x3.5ft)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = material,
                            onValueChange = { material = it },
                            label = { Text("Material (Teak, MDF, HPL)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = machineTimeText,
                            onValueChange = { machineTimeText = it },
                            label = { Text("Run Time (Minutes)") },
                            placeholder = { Text("120") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = bitType,
                            onValueChange = { bitType = it },
                            label = { Text("Bit (Ball Nose, V-Bit)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client Name / Project Reference") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Technical Notes / G-Code Program Info") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        maxLines = 3
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Action Button
            Button(
                onClick = {
                    val finalTitle = title.trim().ifEmpty { "$category Design" }
                    val minutes = machineTimeText.toIntOrNull() ?: 0
                    val entity = (initialDesign ?: CncDesignEntity(
                        dateString = defaultDate,
                        title = finalTitle,
                        category = category
                    )).copy(
                        title = finalTitle,
                        category = category,
                        sellingPrice = sellingPrice,
                        productionCost = productionCost,
                        profit = profit,
                        profitMarginPercent = margin,
                        imageUri = imageUri,
                        machineTimeMinutes = minutes,
                        bitType = bitType,
                        material = material,
                        clientName = clientName,
                        dimensions = dimensions,
                        notes = notes
                    )
                    onSave(entity)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_design_button"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CncPrimary)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (initialDesign == null) "SAVE CNC DESIGN" else "UPDATE DESIGN",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
