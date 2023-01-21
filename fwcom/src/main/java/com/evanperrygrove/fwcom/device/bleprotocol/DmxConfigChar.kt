package com.evanperrygrove.fwcom.device.bleprotocol

import com.evanperrygrove.fwcom.dmx.DmxConfig
import com.evanperrygrove.fwcom.userapp.AppInfo
import com.evanperrygrove.fwcom.util.parseUint16At

object DmxConfigChar {
    fun parse(bytes: ByteArray): DmxConfig? {
        if (bytes.size < 6 || bytes[0] != 1.toByte())
            return null

        val slotOffset = bytes.parseUint16At(1)!!.toInt()
        val slotCount = bytes.parseUint16At(3)!!.toInt()
        val personality = bytes[5].toUByte().toInt()

        return DmxConfig(slotOffset, slotCount, personality)
    }

    fun encode(config: DmxConfig): ByteArray? {
        if (config.slotOffset < 0 || config.slotOffset > 512) return null
        if (config.slotCount < 0 || config.slotCount > 512) return null
        if (config.personality < 0 || config.personality > 65535) return null

        return byteArrayOf(
            1.toByte(),
            (config.slotOffset and 0xff).toByte(),
            ((config.slotOffset shr 8) and 0xff).toByte(),
            (config.slotCount and 0xff).toByte(),
            ((config.slotCount shr 8) and 0xff).toByte(),
            (config.personality and 0xff).toByte()
        )
    }
}