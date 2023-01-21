package com.evanperrygrove.fwcom.device.bleprotocol

import com.evanperrygrove.fwcom.dmx.PersonalityInfo
import com.evanperrygrove.fwcom.dmx.SlotInfo
import com.evanperrygrove.fwcom.util.parseUint16At
import java.nio.ByteBuffer
import java.util.*

sealed interface DmxExplorerResponse<T> {
    fun parse(bytes: ByteArray): T?

    object SlotInfo: DmxExplorerResponse<com.evanperrygrove.fwcom.dmx.SlotInfo> {
        override fun parse(bytes: ByteArray): com.evanperrygrove.fwcom.dmx.SlotInfo? {
            if (bytes.size < 7 || (bytes[0] != 2.toByte()))
                return null

            val personalityNumber = bytes[1].toUByte().toInt()
            val slotNumber = bytes[2].toUByte().toInt()
            val slotType = bytes[3].toUByte().toInt()
            val slotId = bytes.parseUint16At(4)!!
            val value = bytes[6].toUByte()
            val name = if (bytes.size == 7) {
                ""
            } else {
                val name = bytes.slice(7 until bytes.size).toByteArray()
                name.toString(Charsets.UTF_8)
            }

            return SlotInfo(
                personalityNumber,
                slotNumber,
                slotType,
                slotId,
                value,
                name
            )
        }
    }

    object PersonalityInfo : DmxExplorerResponse<com.evanperrygrove.fwcom.dmx.PersonalityInfo> {
        override fun parse(bytes: ByteArray): com.evanperrygrove.fwcom.dmx.PersonalityInfo? {
            if (bytes.size < 3 || (bytes[0] != 1.toByte()))
                return null

            val personalityNumber = bytes[1].toUByte().toInt()
            val slotCount = bytes[2].toUByte().toInt()
            val name = if (bytes.size == 3) {
                ""
            } else {
                val name = bytes.slice(3 until bytes.size).toByteArray()
                name.toString(Charsets.UTF_8)
            }

            return PersonalityInfo(
                personalityNumber,
                slotCount,
                name
            )
        }
    }
}
