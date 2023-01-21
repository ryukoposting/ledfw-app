package com.evanperrygrove.fwcom.device.bleprotocol

import com.evanperrygrove.fwcom.userapp.AppInfo
import com.evanperrygrove.fwcom.userapp.AppState
import com.evanperrygrove.fwcom.userapp.StorageState
import com.evanperrygrove.fwcom.util.parseUint16At

object AppInfoChar {
    fun parse(bytes: ByteArray): AppInfo? {
        if (bytes.size < 8 || (bytes[0] != 1.toByte())) return null

        val appState = when (bytes[1]) {
            AppState.NOT_INITIALIZED.bleValue -> AppState.NOT_INITIALIZED
            AppState.LOADING_APP.bleValue -> AppState.LOADING_APP
            AppState.APP_LOADED.bleValue -> AppState.APP_LOADED
            else -> return null
        }

        val storageState = when (bytes[2]) {
            StorageState.NOT_INITIALIZED.bleValue -> StorageState.NOT_INITIALIZED
            StorageState.STORING_APP.bleValue -> StorageState.STORING_APP
            StorageState.APP_STORED.bleValue -> StorageState.APP_STORED
            StorageState.EMPTY.bleValue -> StorageState.EMPTY
            else -> return null
        }

        val abiCode = bytes[3].toUByte()

        val archFlagsMask = bytes.parseUint16At(4)!!.toUInt()
        val archFlags = bytes.parseUint16At(6)!!.toUInt()

        return AppInfo(
            appState,
            storageState,
            abiCode,
            archFlagsMask,
            archFlags
        )
    }
}