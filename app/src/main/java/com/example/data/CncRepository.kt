package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CncRepository(private val dao: CncDao) {

    val allDesigns: Flow<List<CncDesignEntity>> = dao.getAllDesignsFlow()

    fun getDesignsForDate(date: String): Flow<List<CncDesignEntity>> = dao.getDesignsForDateFlow(date)

    suspend fun getAllDesignsList(): List<CncDesignEntity> = dao.getAllDesignsList()

    suspend fun insertDesign(design: CncDesignEntity): Long = dao.insert(design)

    suspend fun updateDesign(design: CncDesignEntity) = dao.update(design)

    suspend fun deleteDesign(design: CncDesignEntity) = dao.delete(design)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun insertAll(designs: List<CncDesignEntity>) = dao.insertAll(designs)

    suspend fun clearAll() = dao.clearAll()

    suspend fun seedInitialDataIfEmpty() {
        val existing = dao.getAllDesignsList()
        if (existing.isEmpty()) {
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val now = System.currentTimeMillis()
            val sampleDesigns = listOf(
                CncDesignEntity(
                    title = "Royal Baroque Solid Teak Double Door",
                    category = "Door",
                    sellingPrice = 450.0,
                    productionCost = 140.0,
                    profit = 310.0,
                    profitMarginPercent = 68.88,
                    imageUri = "preset:door",
                    timestampMillis = now - 7200000,
                    dateString = todayStr,
                    machineTimeMinutes = 240,
                    bitType = "Ball Nose 6mm + V-Carve 60°",
                    material = "Solid Burma Teak",
                    clientName = "Horizon Luxury Villas",
                    dimensions = "7.5ft x 4.0ft x 45mm",
                    notes = "3D roughing with 12mm flat bit, finishing with 6mm tapered ball nose at 18000 RPM."
                ),
                CncDesignEntity(
                    title = "Modern Geometric Moroccan Jali Screen",
                    category = "Jali / Grill",
                    sellingPrice = 280.0,
                    productionCost = 75.0,
                    profit = 205.0,
                    profitMarginPercent = 73.21,
                    imageUri = "preset:panel",
                    timestampMillis = now - 3600000,
                    dateString = todayStr,
                    machineTimeMinutes = 110,
                    bitType = "Downcut End Mill 4mm",
                    material = "White HPL / WPC Board",
                    clientName = "Studio Architectura",
                    dimensions = "6ft x 3ft x 18mm",
                    notes = "High precision through-cut with dual tab holding. Clean zero-burr finish."
                ),
                CncDesignEntity(
                    title = "Acoustic Parametric 3D Wave Panel",
                    category = "Wall Panel",
                    sellingPrice = 360.0,
                    productionCost = 95.0,
                    profit = 265.0,
                    profitMarginPercent = 73.61,
                    imageUri = "preset:banner",
                    timestampMillis = now - 1800000,
                    dateString = todayStr,
                    machineTimeMinutes = 150,
                    bitType = "Core Box Bit 1/2 inch",
                    material = "High Density MDF + Walnut Veneer",
                    clientName = "SoundSphere Audio Lounge",
                    dimensions = "8ft x 4ft x 25mm",
                    notes = "Fluted sinusoidal cutting pattern along X-axis. Feed rate 4200 mm/min."
                )
            )
            dao.insertAll(sampleDesigns)
        }
    }
}
