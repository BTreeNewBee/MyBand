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
import androidx.fragment.app.viewModels
import com.iguigui.band.database.MyBandDatabase
import com.iguigui.band.viewmodels.BandViewModel

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private val viewModel: BandViewModel by viewModels()

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


    }

    private fun connectMyBand(deviceAddress: String, authKey: String) {

    }

    private val TAG = "MainActivity"

    private fun connect(deviceAddress: String, authKey: String) {


    }


}