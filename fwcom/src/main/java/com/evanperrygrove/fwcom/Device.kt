package com.evanperrygrove.fwcom

import android.app.Activity
import androidx.annotation.Keep
import com.evanperrygrove.fwcom.dmx.DmxConfig
import com.evanperrygrove.fwcom.dmx.PersonalityInfo
import com.evanperrygrove.fwcom.dmx.SlotInfo
import com.evanperrygrove.fwcom.userapp.DmxInfo

interface Device {
    val isConnected: Boolean
    val name: String
    val address: String
    val nickname: String?

    fun connect()

    fun disconnect()

    fun readSerialNumber(callback: OnSerialNumberReceived)
    fun readSerialNumberUi(activity: Activity, callback: OnSerialNumberReceived) =
        readSerialNumber {
            activity.runOnUiThread {
                callback.onSerialNumberReceived(it)
            }
        }

    fun readManufacturerName(callback: OnManufacturerNameReceived)
    fun readManufacturerNameUi(activity: Activity, callback: OnManufacturerNameReceived) =
        readManufacturerName {
            activity.runOnUiThread {
                callback.onManufacturerNameReceived(it)
            }
        }

    fun sendSlotValue(slotNumber: Int, value: UByte)

    fun sendDmxConfig(config: DmxConfig)

    /**
     * Request detailed information about a particular DMX personality
     * Causes the `onDmxPersonalityInfo` callback to fire.
     */
    fun requestDmxPersonalityInfo(personalityNumber: Int)

    /**
     * Request detailed information for multiple DMX personalities
     * Causes the `onDmxPersonalityInfo` callback to fire once for each personality.
     */
    fun requestDmxPersonalityInfo(range: IntRange)

    /**
     * Request detailed information for all DMX slots in a particular personality
     */
    fun requestDmxSlotInfo(personalityNumber: Int)

    /**
     * Retrieve basic information about the current application's DMX config
     * Causes the `onDmxInfoChanged` callback to fire.
     */
    fun requestDmxInfo()

    var onConnect: OnConnect
    var onDisconnect: OnDisconnectFinished
    var onDmxPersonalityInfo: OnDmxPersonalityInfoReceived
    var onDmxSlotInfo: OnDmxSlotInfoReceived
    var onStatusMessage: OnStatusMessageReceived
    var onSlotValueChanged: OnSlotValueChanged
    var onDmxInfoChanged: OnDmxInfoChanged

    fun interface OnConnect {
        fun onConnectFinished(didSucceed: Boolean, statusCode: Int)
    }

    fun interface OnDisconnectFinished {
        fun onDisconnectFinished(didSucceed: Boolean, statusCode: Int)
    }

    fun interface OnSerialNumberReceived {
        fun onSerialNumberReceived(serialNumber: String?)
    }

    fun interface OnManufacturerNameReceived {
        fun onManufacturerNameReceived(manufacturerName: String?)
    }

    fun interface OnDmxPersonalityInfoReceived {
        fun onDmxPersonalityInfoReceived(info: PersonalityInfo)
    }

    fun interface OnDmxSlotInfoReceived {
        fun onDmxSlotInfoReceived(info: SlotInfo)
    }

    fun interface OnStatusMessageReceived {
        fun onStatusMessageReceived(message: String)
    }

    fun interface OnDmxInfoChanged {
        fun onDmxInfoChanged(dmxInfo: DmxInfo)
    }

    fun interface OnSlotValueChanged {
        fun onSlotValueChanged(slotNumber: Int, value: UByte)
    }

    fun interface OnAllDmxPersonalityInfoReceived {
        fun onAllDmxPersonalityInfoReceived()
    }
}