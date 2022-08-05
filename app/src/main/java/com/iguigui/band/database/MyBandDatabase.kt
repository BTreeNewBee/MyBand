package com.iguigui.band.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.iguigui.band.dao.BandDao
import com.iguigui.band.entity.Band

const val DATABASE_NAME = "myBand-db"

@Database(entities = [Band::class], version = 1)
abstract class MyBandDatabase : RoomDatabase() {
    abstract fun bandDao(): BandDao


    companion object {
        // For Singleton instantiation
        @Volatile private var instance: MyBandDatabase? = null

        fun getInstance(context: Context): MyBandDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        // Create and pre-populate the database. See this article for more details:
        // https://medium.com/google-developers/7-pro-tips-for-room-fbadea4bfbd1#4785
        private fun buildDatabase(context: Context): MyBandDatabase {
            return Room.databaseBuilder(context, MyBandDatabase::class.java, DATABASE_NAME)
                .build()
        }
    }

}
