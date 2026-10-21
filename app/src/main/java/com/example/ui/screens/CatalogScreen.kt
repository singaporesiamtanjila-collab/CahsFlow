package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CncDesignEntity
import com.example.ui.CncViewModel
import com.example.ui.components.CadGridBackground
import com.example.ui.theme.CncBorder
import com.example.ui.theme.CncPrimary
import com.example.ui.theme.CncSurface
import com.example.ui.theme.CncSurfaceVariant
import com.example.ui.theme.CncTextPrimary
import com.example.ui.theme.CncTextSecondary

@Composable
fun CatalogScreen(
    viewModel: CncViewModel,
    onEditDesign: (CncDesignEntity) -> Unit
) {
    val catalogDesigns by viewModel.catalogDesigns.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val categories = listOf("All", "Door", "Jali / Grill", "3D Relief", "Wall Panel", "Engraving", "Furniture")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("catalog_screen")
    ) {
        CadGridBackground()

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "CNC DESIGN CATALOG ARCHIVE",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        ),
                        color = CncTextPrimary
                    )
                    Text(
                        text = "Browse, search and manage all completed projects",
                        style = MaterialTheme.typography.bodySmall,
                        color = CncTextSecondary
                    )
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search by design title, client, material...") },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = CncTextSecondary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("catalog_search_field"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = CncSurface,
                        unfocusedContainerColor = CncSurface,
                        focusedBorderColor = CncPrimary,
                        unfocusedBorderColor = CncBorder
                    ),
                    singleLine = true
                )
            }

            // Category filter chips
            item {
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

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RESULTS (${catalogDesigns.size})",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = CncTextSecondary
                    )
                }
            }

            if (catalogDesigns.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CncSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CncBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No Designs Found",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = CncTextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try modifying your search query or switching categories.",
                                style = MaterialTheme.typography.bodySmall,
                                color = CncTextSecondary
                            )
                        }
                    }
                }
            } else {
                items(catalogDesigns, key = { it.id }) { design ->
                    CncDesignCard(
                        design = design,
                        currencySymbol = currencySymbol,
                        onClick = { onEditDesign(design) },
                        onEdit = { onEditDesign(design) },
                        onDuplicate = { viewModel.duplicateDesign(design) },
                        onDelete = { viewModel.deleteDesign(design) }
                    )
                }
            }
        }
    }
}
