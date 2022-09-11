package com.iguigui.band

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import cn.com.heaton.blelibrary.ble.Ble
import cn.com.heaton.blelibrary.ble.BleLog
import cn.com.heaton.blelibrary.ble.callback.BleScanCallback
import cn.com.heaton.blelibrary.ble.callback.BleStatusCallback
import cn.com.heaton.blelibrary.ble.model.BleDevice
import cn.com.heaton.blelibrary.ble.utils.Utils
import cn.com.heaton.blelibrary.ble.utils.UuidUtils
import java.util.*

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE: Int = 0x01

    private lateinit var mBle: Ble<BleDevice>
    private var listDatas = mutableListOf<BleDevice>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        requestBLEPermission()
        initBLE()
        initBleStatus()
        findViewById<Button>(R.id.ConnectBand).setOnClickListener {
            mBle.startScan(bleScanCallback())
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
            uuidService = UUID.fromString(UuidUtils.uuid16To128("fd00"))
            uuidWriteCha = UUID.fromString(UuidUtils.uuid16To128("fd01"))
            bleWrapperCallback = MyBleWrapperCallback()
            /*factory = object : BleFactory<MyDevice>() {
                //实现自定义BleDevice时必须设置
                override fun create(address: String, name: String): MyDevice{
                    return MyDevice(address, name) //自定义BleDevice的子类
                }
            }*/
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
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
        } else {
            //5、若已打开，则进行扫描
            mBle.startScan(bleScanCallback())
        }
    }

    private fun bleScanCallback(): BleScanCallback<BleDevice> {
        return object : BleScanCallback<BleDevice>() {
            override fun onStart() {
                Log.println(Log.INFO, TAG, "onStart $this")
            }

            override fun onStop() {
                Log.println(Log.INFO, TAG, "onStop $this")
            }

            override fun onScanFailed(errorCode: Int) {
                Log.println(Log.INFO, TAG, "onScanFailed $this")
            }

            override fun onLeScan(device: BleDevice?, rssi: Int, scanRecord: ByteArray?) {
                Log.println(Log.INFO, "onLeScan", "device: $device scanRecord: $scanRecord")
                if (TextUtils.isEmpty(device?.bleName)) {
                    return
                }
                for (d in listDatas) {
                    if (d.bleAddress == device?.bleAddress) {
                        return
                    }
                }
                device?.let {
                    listDatas.add(it)
                }
            }
        }
    }

}