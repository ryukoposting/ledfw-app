package com.evanperrygrove.fwcom.device.bleprotocol

sealed interface DmxExplorerRequest {
    fun toBytes(): ByteArray

    class GetPersonalityInfo(private val personalityNumber: Int): DmxExplorerRequest {
        override fun toBytes(): ByteArray = byteArrayOf(1, personalityNumber.toByte())
    }

    class GetSlotInfo(private val personalityNumber: Int, private val slotNumber: Int): DmxExplorerRequest {
        override fun toBytes(): ByteArray = byteArrayOf(2, personalityNumber.toByte(), slotNumber.toByte())
    }

    class GetPersonalityAndSlotInfo(private val personalityNumber: Int): DmxExplorerRequest {
        override fun toBytes(): ByteArray = byteArrayOf(3, personalityNumber.toByte())
    }
}
