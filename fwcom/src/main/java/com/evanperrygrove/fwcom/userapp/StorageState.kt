package com.evanperrygrove.fwcom.userapp

import android.content.Context
import com.evanperrygrove.fwcom.R

enum class StorageState(val bleValue: Byte, val stringId: Int) {
    NOT_INITIALIZED(0.toByte(), R.string.not_initialized),
    EMPTY(1.toByte(), R.string.storage_empty),
    STORING_APP(2.toByte(), R.string.storing_user_app),
    APP_STORED(3.toByte(), R.string.user_app_stored);

    fun toString(context: Context): String = context.getString(stringId)
}
