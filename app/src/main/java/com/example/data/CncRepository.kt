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
}
