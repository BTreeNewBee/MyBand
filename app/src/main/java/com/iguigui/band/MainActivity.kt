package com.iguigui.band

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import cn.com.heaton.blelibrary.ble.utils.ByteUtils
import com.inuker.bluetooth.library.BluetoothClient
import com.inuker.bluetooth.library.Code.REQUEST_SUCCESS
import com.inuker.bluetooth.library.Constants.STATUS_CONNECTED
import com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener
import com.inuker.bluetooth.library.connect.options.BleConnectOptions
import com.inuker.bluetooth.library.connect.response.BleNotifyResponse
import java.util.*


class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE: Int = 0x01


    private val MAC = "F0:71:B7:35:CD:3A"

    lateinit var mClient: BluetoothClient

    val notifyService = UUID.fromString("0000fee1-0000-1000-8000-00805f9b34fb")

    val auth = UUID.fromString("00000009-0000-3512-2118-0009af100700")

    /**
     *
    heartRate = serviceMap["0000180d-0000-1000-8000-00805f9b34fb"]!!
    battery = serviceMap["0000180f-0000-1000-8000-00805f9b34fb"]!!
     */
    val heartRate = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")

    val heartRateMeasureCharacteristic = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

    val heartRateControlCharacteristic = UUID.fromString("00002a39-0000-1000-8000-00805f9b34fb")


    val battery = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")

    val batteryRead = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        requestBLEPermission()
//        initBleStatus()
        findViewById<Button>(R.id.ConnectBand).setOnClickListener {
            mClient = BluetoothClient(this)
//            val request = SearchRequest.Builder()
//                .searchBluetoothLeDevice(3000, 3) // 先扫BLE设备3次，每次3s
//                .build()
//            mClient.search(request, object : SearchResponse {
//                override fun onSearchStarted() {
//                    Log.d("onSearchStarted", "onSearchStarted")
//                }
//                override fun onDeviceFounded(device: SearchResult) {
//                    if (device.address.toString() == MAC) {
//                        Log.d("onDeviceFounded", "onDeviceFounded ${device.address}" )
//                        val beacon = Beacon(device.scanRecord)
//                        BluetoothLog.v(
//                            String.format(
//                                "beacon for %s\n%s",
//                                device.getAddress(),
//                                beacon.toString()
//                            )
//                        )
//                    }
//                }
//                override fun onSearchStopped() {
//                    Log.d("MainActivity", "onSearchStopped")
//                }
//                override fun onSearchCanceled() {
//                    Log.d("MainActivity", "onSearchCanceled")
//                }
//            })
            val options = BleConnectOptions.Builder()
                .setConnectRetry(3) // 连接如果失败重试3次
                .setConnectTimeout(30000) // 连接超时30s
                .setServiceDiscoverRetry(3) // 发现服务如果失败重试3次
                .setServiceDiscoverTimeout(20000) // 发现服务超时20s
                .build()

            mClient.connect(
                MAC, options
            ) { code, data ->
                if (code == REQUEST_SUCCESS) {
                    Log.d("MainActivity", "connect success data: ${data.services}")
                    data.services
                } else {
                    Log.d("MainActivity", "connect failed code: $code")
                }
            }



            mClient.read(
                MAC, battery, batteryRead
            ) { code, data ->
                if (code == REQUEST_SUCCESS) {
                    Log.d("MainActivity", "read success data: ${data[0]}")
                } else {
                    Log.d("MainActivity", "read failed code: $code")
                }
            }


            mClient.notify(MAC, notifyService, auth, object : BleNotifyResponse {
                override fun onNotify(service: UUID?, character: UUID?, value: ByteArray?) {

                    Log.d("MainActivity", "onNotify: ${ByteUtils.bytes2HexStr(value)}")

                    value?.let {
                        val bytes2HexStr = ByteUtils.bytes2HexStr(it.sliceArray(0..2))
                        when (bytes2HexStr) {
                            "100101" -> {
                                Log.d("MainActivity", "Start to request random number...")
                                requestRandomNumber()
                            }
                            "100104" -> {
                                Log.d("MainActivity", "Failed to send key.")
                            }
                            "100201" -> {
                                Log.d("MainActivity", "Start to send encrypt random number...")
                                sendEncryptRandomNumber(it.sliceArray(3 until it.size))
                            }
                            "100204" -> {
                                Log.d("MainActivity", "Failed to request random number.")
                            }
                            "100301" -> {
                                Log.d("MainActivity", "Mi Band Connect Success!")
                                mClient.notify(MAC,heartRate, heartRateMeasureCharacteristic, object : BleNotifyResponse {
                                    override fun onNotify(service: UUID?, character: UUID?, value: ByteArray?) {
                                        Log.d("Main Activity", "heart rate notify : ${value?.get(1)}")
                                    }

                                    override fun onResponse(code: Int) {
                                        if (code == REQUEST_SUCCESS) {
                                            Log.d("MainActivity", "notify success")
                                        } else {
                                            Log.d("MainActivity", "notify failed code: $code")
                                        }
                                    }
                                })
                            }
                            "100304" -> {
                                Log.d(
                                    "MainActivity",
                                    "Encryption key auth fail, sending new key..."
                                )
//                                sendKey()
                            }
                            else -> {
                                Log.d("MainActivity", "Unknown response.")
                            }
                        }
                    }

                }

                override fun onResponse(code: Int) {
                    if (code == REQUEST_SUCCESS) {
                        Log.d("MainActivity", "notify success")
                    } else {
                        Log.d("MainActivity", "notify failed code: $code")
                    }
                }
            })

            mClient.registerConnectStatusListener(MAC, mBleConnectStatusListener)





        }
    }



    private fun hexStrToByteArray(str: String): ByteArray {
        if (str.isEmpty()) {
            return ByteArray(0)
        }
        val byteArray = ByteArray(str.length / 2)
        for (i in byteArray.indices) {
            val subStr = str.substring(2 * i, 2 * i + 2)
            byteArray[i] = subStr.toInt(16).toByte()
        }
        return byteArray
    }

    private fun sendEncryptRandomNumber(sliceArray: ByteArray) {
        val key = "f969db5c1efb6b2021f0a3c6e03efd9d"
        val bytes = byteArrayOf(0x03, 0x00) + AESCrypt.encrypt(
            sliceArray,
            hexStrToByteArray(key)
        )
        mClient.write(
            MAC, notifyService, auth, bytes
        ) { code ->
            if (code == REQUEST_SUCCESS) {
                Log.d("MainActivity", "sendEncryptRandomNumber write success")
            } else {
                Log.d("MainActivity", "sendEncryptRandomNumber write failed code: $code")
            }
        }

    }

    private fun requestRandomNumber() {
        mClient.write(
            MAC, notifyService, auth, byteArrayOf(0x02, 0x00)
        ) { code ->
            if (code == REQUEST_SUCCESS) {
                Log.d("MainActivity", "requestRandomNumber write success")
            } else {
                Log.d("MainActivity", "requestRandomNumber write failed code: $code")
            }
        }
    }


    private val mBleConnectStatusListener: BleConnectStatusListener =
        object : BleConnectStatusListener() {
            override fun onConnectStatusChanged(mac: String, status: Int) {
                if (status == STATUS_CONNECTED) {
                    Log.d("MainActivity", "onConnectStatusChanged: connected")
                    requestRandomNumber()
                } else if (status == STATUS_DISCONNECTED) {
                    Log.d("MainActivity", "onConnectStatusChanged: disconnected")
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


}