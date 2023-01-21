package com.evanperrygrove.fwcom.userapp

data class AppInfo(
    val appState: AppState,
    val storageState: StorageState,
    val abiCode: UByte,
    val archFlagsMask: UInt,
    val archFlags: UInt
) {
    fun validateArchitecture(flags: UInt): Boolean =
        (flags and archFlagsMask) == archFlags
}
