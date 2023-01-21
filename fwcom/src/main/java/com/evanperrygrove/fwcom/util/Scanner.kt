package com.evanperrygrove.fwcom.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat

class Scanner(private val activity: Activity, private val scanCallback: ScanCallback) {
    private lateinit var btManager: BluetoothManager // = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private lateinit var btAdapter: BluetoothAdapter // = btManager.adapter
    private var btScanner: BluetoothLeScanner? = null //= = btAdapter?.bluetoothLeScanner

    private val scanFilters = mutableListOf(
        ScanFilter.Builder().apply {
            setManufacturerData(0xffff, byteArrayOf(0x41, 0x98.toByte()), byteArrayOf(0xff.toByte(), 0xff.toByte()))
        }.build()
    )

    companion object {
        const val REQUEST_PERMISSIONS = 1
    }

    @SuppressLint("MissingPermission")
    fun onCreate() {
        btManager = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        btAdapter = btManager.adapter
        btScanner = btAdapter.bluetoothLeScanner

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            checkPermissions(Manifest.permission.BLUETOOTH_SCAN)
        } else {
            checkPermissions(Manifest.permission.BLUETOOTH)
        }

        checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION)

        if (!btAdapter.isEnabled) {
            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            activity.startActivityForResult(intent, 1)
        }
    }

    @SuppressLint("MissingPermission")
    fun startScanning() {
        Log.d("Scanner", "Starting scan")
        if (btScanner == null)
            btScanner = btAdapter.bluetoothLeScanner
        val builder = ScanSettings.Builder()
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//        builder.setNumOfMatches(ScanSettings.MATCH_NUM_MAX_ADVERTISEMENT)
        btScanner?.startScan(scanFilters, builder.build(), scanCallback)
    }

    @SuppressLint("MissingPermission")
    fun stopScanning() {
        Log.d("Scanner", "Stopping scan")
        if (btScanner == null)
            btScanner = btAdapter.bluetoothLeScanner
        btScanner?.stopScan(scanCallback)
    }

    fun filterByMacAddress(address: String) {
        val filter = ScanFilter.Builder().apply {
            setDeviceAddress(address)
        }.build()

        scanFilters.add(filter)
    }

    private fun checkPermissions(permission: String) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("This app needs Bluetooth permissions")
            builder.setMessage("Please grant bluetooth permissions so that this app can scan for devices.")
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener { _ ->
                val perms = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    arrayOf(
                        Manifest.permission.BLUETOOTH_SCAN,
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                } else {
                    arrayOf(
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.BLUETOOTH_ADMIN,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                }

                ActivityCompat.requestPermissions(activity,
                    perms,
                    REQUEST_PERMISSIONS
                )
            }
            builder.create().show()
        }
    }
}