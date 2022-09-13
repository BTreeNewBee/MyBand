package com.iguigui.band

import android.bluetooth.BluetoothGattService
import android.os.Parcel
import cn.com.heaton.blelibrary.ble.model.BleDevice

class MyBandDevice : BleDevice {

    constructor(address: String, name: String) : super(address, name)

    constructor(parcel: Parcel) : super(parcel)

    var service: BluetoothGattService? = null
    var notify: BluetoothGattService? = null
    var heartRate: BluetoothGattService? = null
    var battery: BluetoothGattService? = null

    fun registerService(services: List<BluetoothGattService>) {
        val serviceMap = services.associateBy { it.uuid.toString().lowercase() }
        service = serviceMap["fee0"]
        notify = serviceMap["fee1"]
        heartRate = serviceMap["180d"]
        battery = serviceMap["180f"]
    }


}