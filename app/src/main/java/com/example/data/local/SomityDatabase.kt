package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CommitteeMember
import com.example.data.model.Member
import com.example.data.model.Payment

@Database(
    entities = [Member::class, Payment::class, CommitteeMember::class],
    version = 1,
    exportSchema = false
)
abstract class SomityDatabase : RoomDatabase() {
    abstract fun somityDao(): SomityDao

    companion object {
        @Volatile
        private var INSTANCE: SomityDatabase? = null

        fun getDatabase(context: Context): SomityDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SomityDatabase::class.java,
                    "scsm_somity_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
