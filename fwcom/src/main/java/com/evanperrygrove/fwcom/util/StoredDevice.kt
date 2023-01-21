package com.evanperrygrove.fwcom.util

import android.annotation.SuppressLint
import android.bluetooth.le.ScanResult
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class StoredDevice(val name: String, val address: String, val nickname: String?): Parcelable {
    override fun describeContents(): Int = 0

    override fun writeToParcel(out: Parcel?, flags: Int) {
        out?.writeString(name)
        out?.writeString(address)
        if (nickname != null) {
            out?.writeInt(1)
            out?.writeString(nickname)
        } else {
            out?.writeInt(0)
        }
    }

    override fun hashCode(): Int {
        return address.hashCode()
    }

    companion object CREATOR: Parcelable.Creator<StoredDevice?> {
        override fun createFromParcel(input: Parcel?): StoredDevice? {
            val name = input?.readString() ?: return null
            val address = input.readString() ?: return null
            val hasNickname = input.readInt()
            val nickname = if (hasNickname != 0) {
                input.readString() ?: return null
            } else {
                null
            }

            return StoredDevice(name, address, nickname)
        }

        override fun newArray(size: Int): Array<StoredDevice?> {
            return arrayOfNulls(size)
        }
    }
}

@SuppressLint("MissingPermission")
fun ScanResult.toStoredDevice(): StoredDevice? {
    val name = device?.name ?: return null
    val address = device?.address ?: return null
    val nickname = scanRecord?.let { record ->
        record.getManufacturerSpecificData(0xffff)?.let {
            if (it.size >= 2 && it[0] == 0x41.toByte() && it[1] == 0x98.toByte()) {
                it.drop(2).toByteArray()
            } else {
                null
            }
        }
    }

    return StoredDevice(name, address, nickname?.toString(Charsets.UTF_8))
}
