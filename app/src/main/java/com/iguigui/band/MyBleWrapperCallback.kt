package com.iguigui.band

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.util.Log
import cn.com.heaton.blelibrary.ble.callback.wrapper.BleWrapperCallback
import cn.com.heaton.blelibrary.ble.utils.ByteUtils

/**
 * author: jerry
 * date: 20-4-13
 * email: superliu0911@gmail.com
 * des: 例： OTA升级可以再这里实现,与项目其他功能逻辑完全解耦
 */
class MyBleWrapperCallback : BleWrapperCallback<MyBandDevice>() {

    override fun onChanged(device: MyBandDevice, characteristic: BluetoothGattCharacteristic) {
        super.onChanged(device, characteristic)
        Log.d(TAG, "onChanged: " + ByteUtils.toHexString(characteristic.value))
    }

    override fun onServicesDiscovered(device: MyBandDevice, gatt: BluetoothGatt) {
        super.onServicesDiscovered(device, gatt)
        device.registerService(gatt.services)
    }

    override fun onWriteSuccess(device: MyBandDevice, characteristic: BluetoothGattCharacteristic) {
        super.onWriteSuccess(device, characteristic)
        Log.d(TAG, "onWriteSuccess: ")
    }

    override fun onConnectionChanged(device: MyBandDevice) {
        super.onConnectionChanged(device)
        Log.d(TAG, "onConnectionChanged: $device")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: ")
    }

    override fun onLeScan(device: MyBandDevice, rssi: Int, scanRecord: ByteArray) {
        super.onLeScan(device, rssi, scanRecord)
        Log.d(TAG, "onLeScan: $device")
    }

    override fun onNotifySuccess(device: MyBandDevice) {
        super.onNotifySuccess(device)
        Log.d(TAG, "onNotifySuccess: ")
    }

    override fun onNotifyCanceled(device: MyBandDevice) {
        super.onNotifyCanceled(device)
        Log.d(TAG, "onNotifyCanceled: ")
    }

    override fun onReady(device: MyBandDevice) {
        super.onReady(device)
        Log.d(TAG, "onReady: ")
    }

    override fun onDescWriteSuccess(device: MyBandDevice, descriptor: BluetoothGattDescriptor) {
        super.onDescWriteSuccess(device, descriptor)
    }

    override fun onDescWriteFailed(device: MyBandDevice, failedCode: Int) {
        super.onDescWriteFailed(device, failedCode)
    }

    override fun onDescReadFailed(device: MyBandDevice, failedCode: Int) {
        super.onDescReadFailed(device, failedCode)
    }

    override fun onDescReadSuccess(device: MyBandDevice, descriptor: BluetoothGattDescriptor) {
        super.onDescReadSuccess(device, descriptor)
    }

    override fun onMtuChanged(device: MyBandDevice, mtu: Int, status: Int) {
        super.onMtuChanged(device, mtu, status)
    }

    override fun onReadSuccess(device: MyBandDevice, characteristic: BluetoothGattCharacteristic) {
        super.onReadSuccess(device, characteristic)
    }

    companion object {
        private const val TAG = "MyBleWrapperCallback"
    }

    override fun onNotifyFailed(device: MyBandDevice?, failedCode: Int) {
        TODO("Not yet implemented")
    }
}