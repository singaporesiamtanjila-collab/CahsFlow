package com.example.data

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object BackupManager {

    fun exportToJsonString(designs: List<CncDesignEntity>): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("appName", "CNC Craft")
        root.put("exportDate", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
        root.put("totalRecords", designs.size)

        val array = JSONArray()
        for (item in designs) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("title", item.title)
            obj.put("category", item.category)
            obj.put("sellingPrice", item.sellingPrice)
            obj.put("productionCost", item.productionCost)
            obj.put("profit", item.profit)
            obj.put("profitMarginPercent", item.profitMarginPercent)
            obj.put("imageUri", item.imageUri ?: "")
            obj.put("timestampMillis", item.timestampMillis)
            obj.put("dateString", item.dateString)
            obj.put("machineTimeMinutes", item.machineTimeMinutes)
            obj.put("bitType", item.bitType)
            obj.put("material", item.material)
            obj.put("clientName", item.clientName)
            obj.put("dimensions", item.dimensions)
            obj.put("notes", item.notes)
            array.put(obj)
        }
        root.put("designs", array)
        return root.toString(2)
    }

    fun parseJsonBackup(jsonString: String): List<CncDesignEntity> {
        val root = JSONObject(jsonString)
        val array = if (root.has("designs")) root.getJSONArray("designs") else JSONArray(jsonString)
        val list = mutableListOf<CncDesignEntity>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val title = obj.optString("title", "CNC Design")
            val category = obj.optString("category", "Custom")
            val sellingPrice = obj.optDouble("sellingPrice", 0.0)
            val cost = obj.optDouble("productionCost", 0.0)
            val profit = obj.optDouble("profit", sellingPrice - cost)
            val margin = obj.optDouble("profitMarginPercent", if (sellingPrice > 0) ((sellingPrice - cost) / sellingPrice) * 100 else 0.0)
            val imageUri = obj.optString("imageUri", "").takeIf { it.isNotBlank() }
            val ts = obj.optLong("timestampMillis", System.currentTimeMillis())
            val dateStr = obj.optString("dateString", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(ts)))
            val machineTime = obj.optInt("machineTimeMinutes", 0)
            val bitType = obj.optString("bitType", "")
            val material = obj.optString("material", "")
            val client = obj.optString("clientName", "")
            val dimensions = obj.optString("dimensions", "")
            val notes = obj.optString("notes", "")

            list.add(
                CncDesignEntity(
                    id = 0, // Auto-generate new IDs on restore to avoid conflict
                    title = title,
                    category = category,
                    sellingPrice = sellingPrice,
                    productionCost = cost,
                    profit = profit,
                    profitMarginPercent = margin,
                    imageUri = imageUri,
                    timestampMillis = ts,
                    dateString = dateStr,
                    machineTimeMinutes = machineTime,
                    bitType = bitType,
                    material = material,
                    clientName = client,
                    dimensions = dimensions,
                    notes = notes
                )
            )
        }
        return list
    }

    fun exportToCsvString(designs: List<CncDesignEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Date,Title,Category,Client,Dimensions,Material,Tooling Bit,Machine Minutes,Selling Price,Cost,Profit,Margin %\n")
        for (d in designs) {
            val escapedTitle = "\"${d.title.replace("\"", "\"\"")}\""
            val escapedCategory = "\"${d.category.replace("\"", "\"\"")}\""
            val escapedClient = "\"${d.clientName.replace("\"", "\"\"")}\""
            val escapedDims = "\"${d.dimensions.replace("\"", "\"\"")}\""
            val escapedMaterial = "\"${d.material.replace("\"", "\"\"")}\""
            val escapedBit = "\"${d.bitType.replace("\"", "\"\"")}\""

            sb.append("${d.id},${d.dateString},$escapedTitle,$escapedCategory,$escapedClient,$escapedDims,$escapedMaterial,$escapedBit,")
            sb.append("${d.machineTimeMinutes},${String.format(Locale.US, "%.2f", d.sellingPrice)},")
            sb.append("${String.format(Locale.US, "%.2f", d.productionCost)},${String.format(Locale.US, "%.2f", d.profit)},")
            sb.append("${String.format(Locale.US, "%.1f", d.profitMarginPercent)}%\n")
        }
        return sb.toString()
    }

    fun writeStringToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { output ->
                OutputStreamWriter(output).use { writer ->
                    writer.write(content)
                    writer.flush()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun readStringFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                BufferedReader(InputStreamReader(input)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
