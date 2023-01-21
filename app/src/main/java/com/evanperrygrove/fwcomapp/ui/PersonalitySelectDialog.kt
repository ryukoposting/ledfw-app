package com.evanperrygrove.fwcomapp.ui

import android.content.Context
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import com.evanperrygrove.fwcom.device.BleDevice
import com.evanperrygrove.fwcom.dmx.PersonalityInfo

class PersonalitySelectDialog(
    context: Context,
    private val personalityInfos: List<PersonalityInfo>,
    private val onPersonalitySelected: OnPersonalitySelected)
{
    fun interface OnPersonalitySelected {
        fun onPersonalitySelected(info: PersonalityInfo)
    }

    private val dialog = AlertDialog.Builder(context).apply {
        setTitle(com.evanperrygrove.fwcom.R.string.change_personality)
        val labels = personalityInfos.map { it.name }.toTypedArray()
        setItems(labels) { dialog, index ->
            onPersonalitySelected.onPersonalitySelected(personalityInfos[index])
            dialog.dismiss()
        }
    }

    fun show() {
        dialog.create().show()
    }
}