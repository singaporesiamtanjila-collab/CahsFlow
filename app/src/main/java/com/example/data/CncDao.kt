package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CncDao {
    @Query("SELECT * FROM cnc_designs ORDER BY timestampMillis DESC")
    fun getAllDesignsFlow(): Flow<List<CncDesignEntity>>

    @Query("SELECT * FROM cnc_designs WHERE dateString = :date ORDER BY timestampMillis DESC")
    fun getDesignsForDateFlow(date: String): Flow<List<CncDesignEntity>>

    @Query("SELECT * FROM cnc_designs WHERE id = :id LIMIT 1")
    suspend fun getDesignById(id: Long): CncDesignEntity?

    @Query("SELECT * FROM cnc_designs ORDER BY timestampMillis DESC")
    suspend fun getAllDesignsList(): List<CncDesignEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(design: CncDesignEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(designs: List<CncDesignEntity>)

    @Update
    suspend fun update(design: CncDesignEntity)

    @Delete
    suspend fun delete(design: CncDesignEntity)

    @Query("DELETE FROM cnc_designs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM cnc_designs")
    suspend fun clearAll()
}
