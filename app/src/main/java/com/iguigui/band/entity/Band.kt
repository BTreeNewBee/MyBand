package com.iguigui.band.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Band (
    @PrimaryKey val _id: Int,
    @ColumnInfo(name = "device_address") val deviceAddress: String,
    @ColumnInfo(name = "auth_key") val authKey: String
)
