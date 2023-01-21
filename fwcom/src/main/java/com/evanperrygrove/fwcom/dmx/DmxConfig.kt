package com.evanperrygrove.fwcom.dmx

data class DmxConfig(
    val slotOffset: Int,
    val slotCount: Int,
    val personality: Int
)
