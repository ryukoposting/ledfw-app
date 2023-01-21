package com.evanperrygrove.fwcom.util

fun ByteArray.parseUint16At(offset: Int): Int? {
    if (offset >= size - 1)
        return null

    val b0 = this[offset].toUByte().toUInt()
    val b1 = this[offset + 1].toUByte().toUInt()
    val result = b0 or (b1 shl 8)

    return result.toInt()
}
