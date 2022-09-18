package com.iguigui.band

import android.Manifest
import android.R.attr.data
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGattCharacteristic
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import cn.com.heaton.blelibrary.ble.Ble
import cn.com.heaton.blelibrary.ble.BleLog
import cn.com.heaton.blelibrary.ble.callback.*
import cn.com.heaton.blelibrary.ble.model.BleFactory
import cn.com.heaton.blelibrary.ble.utils.ByteUtils
import java.util.*


class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE: Int = 0x01

    private lateinit var mBle: Ble<MyBandDevice>
    private var devices = mutableMapOf<String, MyBandDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        requestBLEPermission()
        initBLE()
//        initBleStatus()
        findViewById<Button>(R.id.ConnectBand).setOnClickListener {

//            mBle.startScan(bleScanCallback())
            val myBandDevice = devices["F0:71:B7:35:CD:3A"]
            if (myBandDevice == null) {
                if (mBle.isScanning) {
                    mBle.stopScan()
                }
                mBle.startScan(bleScanCallback())
                return@setOnClickListener
            }
            if (myBandDevice.isConnected) {
                myBandDevice.auth()
                return@setOnClickListener
            }

            myBandDevice.mBle = mBle
            myBandDevice.connect()

        }
    }

    private fun initBleStatus() {
        mBle.setBleStatusCallback { isOn ->
            BleLog.i(TAG, "onBluetoothStatusOn: 蓝牙是否打开>>>>:$isOn")
            if (mBle.isScanning) {
                mBle.stopScan()
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }


    private fun requestBLEPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), REQUEST_CODE
        )
    }


    //初始化蓝牙
    private fun initBLE() {

        mBle = Ble.options().apply {
            logBleEnable = true
            throwBleException = true
            autoConnect = true
            logTAG = "BleLog"
            connectFailedRetryCount = 3
            connectTimeout = 10000L
            scanPeriod = 12000L
            uuidService = UUID.fromString("00001530-0000-3512-2118-0009af100700")
            uuidWriteCha = UUID.fromString("00001531-0000-3512-2118-0009af100700")
            uuidReadCha = UUID.fromString("00001532-0000-3512-2118-0009af100700")
            bleWrapperCallback = MyBleWrapperCallback()
            factory = object : BleFactory<MyBandDevice>() {
                //实现自定义MyBandDevice时必须设置
                override fun create(address: String, name: String?): MyBandDevice{
                    Log.d("BleLog", "BleLogcreate: $address")
                    return MyBandDevice(address, name) //自定义MyBandDevice的子类
                }
            }
        }.create(applicationContext, object : Ble.InitCallback {
            override fun failed(failedCode: Int) {
                BleLog.i(TAG, "init failed: $failedCode")
            }

            override fun success() {
                BleLog.i(TAG, "init success")
            }

        })
        //3、检查蓝牙是否支持及打开
        checkBluetoothStatus()
    }


    //检查蓝牙是否支持及打开
    private fun checkBluetoothStatus() {
        // 检查设备是否支持BLE4.0
        if (!mBle.isSupportBle(this)) {
            finish()
        }
        if (!mBle.isBleEnable) {
            //4、若未打开，则请求打开蓝牙
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        } else {
            //5、若已打开，则进行扫描
            mBle.startScan(bleScanCallback())
        }
    }

    private fun bleScanCallback(): BleScanCallback<MyBandDevice> {
        return object : BleScanCallback<MyBandDevice>() {
            override fun onStart() {
                Log.println(Log.INFO, TAG, "onStart $this")
            }

            override fun onStop() {
                Log.println(Log.INFO, TAG, "onStop $this")

            }

            override fun onScanFailed(errorCode: Int) {
                Log.println(Log.INFO, TAG, "onScanFailed $this")
            }

            override fun onLeScan(device: MyBandDevice?, rssi: Int, scanRecord: ByteArray?) {
                Log.println(Log.INFO, "onLeScan", "device: $device scanRecord: $scanRecord")
                device?.let {
                    devices.putIfAbsent(device.bleAddress, device)
                    Log.println(Log.INFO, "onLeScan", "device: $device")
                }
            }
        }
    }

}