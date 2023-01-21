package com.evanperrygrove.fwcom.device.bleprotocol

import com.evanperrygrove.fwcom.userapp.AppInfo
import com.evanperrygrove.fwcom.userapp.DmxInfo

object DmxInfoChar {
    fun parse(bytes: ByteArray): DmxInfo? {
        if (bytes.size < 4 || (bytes[0] != 1.toByte())) return null

        val personalityCount = bytes[1].toUByte().toInt()
        val selectedPersonality = bytes[2].toUByte().toInt()
        val numberOfSlots = bytes[3].toUByte().toInt()

        if (selectedPersonality >= personalityCount) return null

        return DmxInfo(
            personalityCount,
            selectedPersonality,
            numberOfSlots
        )
    }
}