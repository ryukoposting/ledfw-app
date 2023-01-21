package com.evanperrygrove.fwcom.util

import android.content.Context
import android.util.Log
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException

object DeviceStorage {
    private const val FILE_NAME = "stored_devices"

    private var data: LinkedHashSet<StoredDevice> = linkedSetOf()
    private var loaded = false

    private fun readToData(context: Context) {
        try {
            Log.d("writeFromData", "Reading stored_devices")
            data = context.openFileInput(FILE_NAME).use {
                val raw = it.readBytes().toString(Charsets.UTF_8)
                Json.decodeFromString(raw)
            }
        } catch (e: FileNotFoundException) {
            Log.d("writeFromData", "stored_devices does not exist, creating it now")
            writeFromData(context)
        }
    }

    private fun writeFromData(context: Context) {
        context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use {
            val str = Json.encodeToString(data)
            Log.d("writeFromData", "Saving stored_devices")
            it.write(str.toByteArray(Charsets.UTF_8))
        }
    }

    @Synchronized
    fun<T> read(context: Context, fn: (Set<StoredDevice>) -> T): T {
        if (!loaded) {
            readToData(context)
            loaded = true
        }
        return fn(data)
    }

    @Synchronized
    fun<T> write(context: Context, fn: (MutableSet<StoredDevice>) -> T): T {
        if (!loaded) {
            readToData(context)
            loaded = true
        }
        val result = fn(data)
        writeFromData(context)
        return result
    }
}
