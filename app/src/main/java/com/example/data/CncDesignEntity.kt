package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cnc_designs")
data class CncDesignEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String, // Door, Jali / Grill, 3D Relief, Wall Panel, Engraving, Furniture, Custom
    val sellingPrice: Double = 0.0,
    val productionCost: Double = 0.0,
    val profit: Double = sellingPrice - productionCost,
    val profitMarginPercent: Double = if (sellingPrice > 0) ((sellingPrice - productionCost) / sellingPrice) * 100.0 else 0.0,
    val imageUri: String? = null,
    val timestampMillis: Long = System.currentTimeMillis(),
    val dateString: String, // format: YYYY-MM-DD
    val machineTimeMinutes: Int = 0,
    val bitType: String = "",
    val material: String = "",
    val clientName: String = "",
    val dimensions: String = "",
    val notes: String = ""
)
