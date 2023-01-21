package com.evanperrygrove.fwcom.userapp

import android.content.Context
import com.evanperrygrove.fwcom.R

enum class AppState(val bleValue: Byte, val stringId: Int) {
    NOT_INITIALIZED(0.toByte(), R.string.not_initialized),
    LOADING_APP(1.toByte(), R.string.loading_user_app),
    APP_LOADED(2.toByte(), R.string.user_app_loaded);

    fun toString(context: Context): String = context.getString(stringId)
}
