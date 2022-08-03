package com.iguigui.band

import android.annotation.SuppressLint
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase.openOrCreateDatabase
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.inuker.bluetooth.library.BluetoothClient
import com.inuker.bluetooth.library.Code.REQUEST_SUCCESS
import com.inuker.bluetooth.library.Constants.STATUS_CONNECTED
import com.inuker.bluetooth.library.Constants.STATUS_DISCONNECTED
import com.inuker.bluetooth.library.connect.listener.BleConnectStatusListener
import com.inuker.bluetooth.library.connect.listener.BluetoothStateListener


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<Button>(R.id.ConnectBand).setOnClickListener {
//            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
            val devideAddress = view.findViewById<TextView>(R.id.deviceAddress).text
            val authKey = view.findViewById<TextView>(R.id.authKey).text

//            view.findViewById<TextView>(R.id.heartBeatText).text = devideAddress
//            view.findViewById<TextView>(R.id.batteryText).text = authKey


        }

//        database.query("my_band",)

    }

    private fun connectMyBand(deviceAddress: String, authKey: String) {
        if (!mClient.isBluetoothOpened()) {
            mClient.openBluetooth()
            val mBluetoothStateListener: BluetoothStateListener = object : BluetoothStateListener() {
                override fun onBluetoothStateChanged(openOrClosed: Boolean) {
                    if (!openOrClosed) {
                        return
                    }
                    connect(deviceAddress, authKey)
                }
            }
            mClient.registerBluetoothStateListener(mBluetoothStateListener)
        }
        connect(deviceAddress, authKey)
    }

    private val TAG = "MainActivity"

    private fun connect(deviceAddress: String, authKey: String) {

        //发起链接
        mClient.connect(deviceAddress) { code, profile ->
            run {
                if (code == REQUEST_SUCCESS) {
                    profile.services.forEach {
                        Log.d(TAG,"${it.uuid}:${it.characters}");
                    }
                }
            }
        }

        val mBleConnectStatusListener: BleConnectStatusListener = object : BleConnectStatusListener() {
            override fun onConnectStatusChanged(mac: String, status: Int) {
                if (status == STATUS_CONNECTED) {
                    Log.d(TAG,"$mac connected");
                } else if (status == STATUS_DISCONNECTED) {
                    Log.d(TAG,"$mac disconnected");
                }
            }
        }

        mClient.registerConnectStatusListener(deviceAddress, mBleConnectStatusListener)
    }


    var mClient: BluetoothClient = BluetoothClient(context)

    @SuppressLint("SdCardPath")
    val database = openOrCreateDatabase("/data/data/com.iguigui.db/databases/my_band.db", null)
    {
        it.execSQL("create table if not exists my_band( _id integer primary key autoincrement," +
                "device_address VARCHAR(64)," +
                "auth_key  VARCHAR(64))")
    }


    //数据存储
    fun saveData(deviceAddress: String, authKey: String) {
        ContentValues().apply {
            put("device_address", deviceAddress)
            put("auth_key", authKey)
            database.insert("my_band", null, this)
        }
    }


}