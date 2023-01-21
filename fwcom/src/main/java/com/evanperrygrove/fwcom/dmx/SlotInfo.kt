package com.evanperrygrove.fwcom.dmx

import com.evanperrygrove.fwcom.R

data class SlotInfo(
    val personalityNumber: Int,
    val slotNumber: Int,
    val slotType: Int,
    val slotId: Int,
    var value: UByte,
    val name: String
) {
    val slotLayoutId: Int
        get() = when (slotId) {
            else -> R.layout.fragment_slot_slider
        }
}
