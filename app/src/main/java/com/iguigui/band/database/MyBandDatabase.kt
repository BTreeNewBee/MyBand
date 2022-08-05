package com.iguigui.band.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.iguigui.band.dao.BandDao
import com.iguigui.band.entity.Band

@Database(entities = [Band::class], version = 1)
abstract class MyBandDatabase : RoomDatabase() {
    abstract fun bandDao(): BandDao
}
