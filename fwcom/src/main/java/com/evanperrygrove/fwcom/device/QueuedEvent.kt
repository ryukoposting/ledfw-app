package com.evanperrygrove.fwcom.device

import com.evanperrygrove.fwcom.Device

internal sealed class QueuedEvent {
    data class ReadSerialNumber(val callback: Device.OnSerialNumberReceived): QueuedEvent()
    data class ReadManufacturerName(val callback: Device.OnManufacturerNameReceived): QueuedEvent()
    data class RequestDmxPersonalityInfoRange(var expecting: Int, val last: Int): QueuedEvent()
}
