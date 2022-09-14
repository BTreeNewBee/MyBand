package com.iguigui.band

import android.bluetooth.BluetoothGattService
import android.os.Parcel
import cn.com.heaton.blelibrary.ble.model.BleDevice

class MyBandDevice : BleDevice {

    constructor(address: String, name: String?) : super(address, name)

    constructor(parcel: Parcel) : super(parcel)

    /**
     * 180d HeartRate
     * 180f Battery
     * 1800 GenericAccess
     * 1801 GenericAttribute
     * 180a DeviceInformation
     * 1811 AlertNotification
     * 1802 ImmediateAlert
     * fee0 HuamiInformation1
     * fee1 HuamiInformation2
     * 1812 HumanInterfaceDevice
     * 3802 Unknown
     */

    var service: BluetoothGattService? = null
    var notify: BluetoothGattService? = null
    var heartRate: BluetoothGattService? = null
    var battery: BluetoothGattService? = null
    var genericAccess: BluetoothGattService? = null
    var genericAttribute: BluetoothGattService? = null
    var deviceInformation: BluetoothGattService? = null
    var alertNotification: BluetoothGattService? = null
    var immediateAlert: BluetoothGattService? = null
    var humanInterfaceDevice: BluetoothGattService? = null
    var unknown: BluetoothGattService? = null
//    var huamiInformation1: BluetoothGattService? = null
//    var huamiInformation2: BluetoothGattService? = null



    fun registerService(services: List<BluetoothGattService>) {
        val serviceMap = services.associateBy { it.uuid.toString().lowercase() }
        service = serviceMap["fee0"]
        notify = serviceMap["fee1"]
        heartRate = serviceMap["180d"]
        battery = serviceMap["180f"]
        genericAccess = serviceMap["1800"]
        genericAttribute = serviceMap["1801"]
        deviceInformation = serviceMap["180a"]
        alertNotification = serviceMap["1811"]
        immediateAlert = serviceMap["1802"]
        humanInterfaceDevice = serviceMap["1812"]
        unknown = serviceMap["3802"]
    }


}