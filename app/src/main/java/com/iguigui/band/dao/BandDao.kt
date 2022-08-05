package com.iguigui.band.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.iguigui.band.entity.Band

@Dao
interface BandDao {

    @Query("SELECT * FROM band")
    fun getAll(): List<Band>

    @Query(
        "SELECT * FROM band WHERE device_address = :deviceAddress LIMIT 1"
    )
    fun findByName(deviceAddress: String): Band

    @Insert
    fun insertAll(vararg band: Band)

    @Delete
    fun delete(band: Band)

}