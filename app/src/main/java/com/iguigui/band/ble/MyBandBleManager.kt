package com.iguigui.band.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.BleManager
import java.util.*


class MyBandBleManager(context: Context) : BleManager(context) {

    private val defaultScope = CoroutineScope(Dispatchers.Default)

    private val fluxCapacitorControlPoint: BluetoothGattCharacteristic? = null

    private lateinit var bluetoothObserver: BroadcastReceiver

    private var myCharacteristicChangedChannel: SendChannel<String>? = null


    override fun getGattCallback(): BleManagerGattCallback {
        return MyGattCallbackImpl()
    }

    private inner class MyGattCallbackImpl : BleManagerGattCallback() {

        private var myCharacteristic: BluetoothGattCharacteristic? = null


        override fun initialize() {
            setNotificationCallback(myCharacteristic).with { _, data ->
                if (data.value != null) {
                    val value = String(data.value!!, Charsets.UTF_8)
                    defaultScope.launch {
                        myCharacteristicChangedChannel?.send(value)
                    }
                }
            }

            beginAtomicRequestQueue()
                .add(enableNotifications(myCharacteristic)
                    .fail { _: BluetoothDevice?, status: Int ->
                        log(Log.ERROR, "Could not subscribe: $status")
                        disconnect().enqueue()
                    }
                )
                .done {
                    log(Log.INFO, "Target initialized")
                }
                .enqueue()
        }

        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(MyServiceProfile.MY_SERVICE_UUID)
            myCharacteristic =
                service?.getCharacteristic(MyServiceProfile.MY_CHARACTERISTIC_UUID)
            val myCharacteristicProperties = myCharacteristic?.properties ?: 0
            return (myCharacteristicProperties and BluetoothGattCharacteristic.PROPERTY_READ != 0) &&
                    (myCharacteristicProperties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0)
        }

        override fun onServicesInvalidated() {
            TODO("Not yet implemented")
        }

    }

    object MyServiceProfile {
        val MY_SERVICE_UUID: UUID = UUID.fromString("80323644-3537-4F0B-A53B-CF494ECEAAB3")
        val MY_CHARACTERISTIC_UUID: UUID = UUID.fromString("80323644-3537-4F0B-A53B-CF494ECEAAB3")
    }

}