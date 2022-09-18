package com.iguigui.band

import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import android.content.ContentValues
import android.os.Parcel
import android.util.Log
import cn.com.heaton.blelibrary.ble.Ble
import cn.com.heaton.blelibrary.ble.BleLog
import cn.com.heaton.blelibrary.ble.callback.BleConnectCallback
import cn.com.heaton.blelibrary.ble.callback.BleNotifyCallback
import cn.com.heaton.blelibrary.ble.callback.BleReadCallback
import cn.com.heaton.blelibrary.ble.callback.BleWriteCallback
import cn.com.heaton.blelibrary.ble.model.BleDevice
import cn.com.heaton.blelibrary.ble.utils.ByteUtils

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

    private lateinit var service: BluetoothGattService
    private lateinit var notify: BluetoothGattService
    private lateinit var heartRate: BluetoothGattService
    private lateinit var battery: BluetoothGattService
    private lateinit var genericAccess: BluetoothGattService
    private lateinit var genericAttribute: BluetoothGattService
    private lateinit var deviceInformation: BluetoothGattService
    private lateinit var alertNotification: BluetoothGattService
    private lateinit var immediateAlert: BluetoothGattService
    private lateinit var humanInterfaceDevice: BluetoothGattService
    private lateinit var unknown: BluetoothGattService

    lateinit var mBle: Ble<MyBandDevice>

    //"00001800-0000-1000-8000-00805f9b34fb" -> {BluetoothGattService@18043}
    //"00001801-0000-1000-8000-00805f9b34fb" -> {BluetoothGattService@18045}
    //"0000180a-0000-1000-8000-00805f9b34fb" -> {BluetoothGattService@18047}
    //"00001530-0000-3512-2118-0009af100700" -> {BluetoothGattService@18049}
    //"00001811-0000-1000-8000-00805f9b34fb" -> {BluetoothGattService@18051}
    //"00001802-0000-1000-8000-00805f9b34fb" -> {BluetoothGattService@18053}
    //"0000180d-0000-1000-8000-00805f9b34fb" -> {BluetoothGattService@18055}
    //"0000fee0-0000-1000-8000-00805f9b34fb" -> {BluetoothGattService@18057}
    //"0000fee1-0000-1000-8000-00805f9b34fb" -> {BluetoothGattService@18059}
    //"0000180f-0000-1000-8000-00805f9b34fb" -> {BluetoothGattService@18061}
    //"00001812-0000-1000-8000-00805f9b34fb" -> {BluetoothGattService@18063}
    //"00003802-0000-1000-8000-00805f9b34fb" -> {BluetoothGattService@18065}
    fun registerService(services: List<BluetoothGattService>) {
        val serviceMap = services.associateBy { it.uuid.toString().lowercase() }
//        service = serviceMap["fee0"]!!
//        notify = serviceMap["fee1"]!!
//        heartRate = serviceMap["180d"]!!
//        battery = serviceMap["180f"]!!
//        genericAccess = serviceMap["1800"]!!
//        genericAttribute = serviceMap["1801"]!!
//        deviceInformation = serviceMap["180a"]!!
//        alertNotification = serviceMap["1811"]!!
//        immediateAlert = serviceMap["1802"]!!
//        humanInterfaceDevice = serviceMap["1812"]!!
//        unknown = serviceMap["3802"]!!

        service = serviceMap["0000fee0-0000-1000-8000-00805f9b34fb"]!!
        notify = serviceMap["0000fee1-0000-1000-8000-00805f9b34fb"]!!
        heartRate = serviceMap["0000180d-0000-1000-8000-00805f9b34fb"]!!
        battery = serviceMap["0000180f-0000-1000-8000-00805f9b34fb"]!!
        genericAccess = serviceMap["00001800-0000-1000-8000-00805f9b34fb"]!!
        genericAttribute = serviceMap["00001801-0000-1000-8000-00805f9b34fb"]!!
        deviceInformation = serviceMap["0000180a-0000-1000-8000-00805f9b34fb"]!!
        alertNotification = serviceMap["00001811-0000-1000-8000-00805f9b34fb"]!!
        immediateAlert = serviceMap["00001802-0000-1000-8000-00805f9b34fb"]!!
        humanInterfaceDevice = serviceMap["00001812-0000-1000-8000-00805f9b34fb"]!!
        unknown = serviceMap["00003802-0000-1000-8000-00805f9b34fb"]!!
        Log.d("MyBandDevice", "registerService successes: $service")

    }

    fun auth() {

        //Find auth characteristic
        val characteristic = notify.characteristics.find {
            it.uuid.toString().lowercase() == "00000009-0000-3512-2118-0009af100700"
        }

        if (characteristic != null) {
//            mBle.enableNotifyByUuid(
//                this,
//                true,
//                notify.uuid,
//                characteristic.uuid,
//                object : BleNotifyCallback<MyBandDevice>() {
//                    override fun onChanged(
//                        device: MyBandDevice?,
//                        characteristic: BluetoothGattCharacteristic
//                    ) {
//                        val uuid = characteristic.uuid
//                        BleLog.e(ContentValues.TAG, "onChanged==uuid:$uuid")
//                        BleLog.e(
//                            ContentValues.TAG,
//                            "onChanged==data:" + ByteUtils.toHexString(characteristic.value)
//                        )
//                    }
//
//                    override fun onNotifySuccess(device: MyBandDevice?) {
//                        super.onNotifySuccess(device)
//                        BleLog.e(ContentValues.TAG, "onNotifySuccess: " + device?.bleName)
//                    }
//                })

            mBle.readByUuid(
                this,
                notify.uuid,
                characteristic.uuid,
                object : BleReadCallback<MyBandDevice>() {
                    fun onReadSuccess(
                        device: MyBandDevice?,
                        data: ByteArray?,
                        uuid: String?
                    ) {
                        BleLog.e(ContentValues.TAG, "onReadSuccess: " + device?.bleName)
                        data?.let {
                            BleLog.e(
                                ContentValues.TAG,
                                "onReadSuccess==data:" + ByteUtils.bytes2HexStr(it)
                            )
                        }
                    }

                    override fun onReadFailed(device: MyBandDevice?, failedCode: Int) {
                        BleLog.e(ContentValues.TAG, "onReadFailed: ${device?.bleName} $failedCode ")
                    }
                })
            mBle.writeByUuid(this, byteArrayOf(0x02, 0x00), notify.uuid, characteristic.uuid, object :
                BleWriteCallback<MyBandDevice>() {
                override fun onWriteSuccess(
                    device: MyBandDevice?,
                    characteristic: BluetoothGattCharacteristic?
                ) {
                    BleLog.e(ContentValues.TAG, "onWriteSuccess: " + device?.bleName)
                }

                override fun onWriteFailed(device: MyBandDevice?, failedCode: Int) {
                    BleLog.e(ContentValues.TAG, "onWriteFailed: " + device?.bleName + " failedCode:" + failedCode)
                }
            })
        }
    }


    fun connect() {
        mBle.connect(this, object : BleConnectCallback<MyBandDevice>() {
            override fun onConnectionChanged(device: MyBandDevice?) {
                BleLog.e(ContentValues.TAG, "onConnectionChanged: " + device?.bleName)
            }
        })
    }


    fun read() {
        mBle.read(this, object : BleReadCallback<MyBandDevice>() {
            @Override
            override fun onReadSuccess(
                dedvice: MyBandDevice,
                characteristic: BluetoothGattCharacteristic
            ) {
                super.onReadSuccess(dedvice, characteristic);
                BleLog.e(
                    ContentValues.TAG,
                    "onReadSuccess: " + dedvice.bleName + "  " + ByteUtils.toHexString(
                        characteristic.value
                    )
                );
            }
        })
    }

    fun write() {
        //写入一条数据
        mBle.write(this, ByteArray(0), object : BleWriteCallback<MyBandDevice?>() {
            override fun onWriteSuccess(
                device: MyBandDevice?,
                characteristic: BluetoothGattCharacteristic
            ) {

            }
        })
    }


}