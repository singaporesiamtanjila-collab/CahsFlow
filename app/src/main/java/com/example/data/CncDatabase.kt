package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [CncDesignEntity::class], version = 1, exportSchema = false)
abstract class CncDatabase : RoomDatabase() {
    abstract fun cncDao(): CncDao

    companion object {
        @Volatile
        private var INSTANCE: CncDatabase? = null

        fun getDatabase(context: Context): CncDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CncDatabase::class.java,
                    "cnc_craft_database.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
