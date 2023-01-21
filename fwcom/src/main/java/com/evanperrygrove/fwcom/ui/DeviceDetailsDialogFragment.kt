package com.evanperrygrove.fwcomapp.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.DialogFragment
import com.evanperrygrove.fwcom.R
import com.evanperrygrove.fwcom.device.BleDevice

class DeviceDetailsDialogFragment: DialogFragment() {
    private lateinit var serialNumberTextView: AppCompatTextView
    private lateinit var macAddressTextView: AppCompatTextView
    private lateinit var manufacturerNameTextView: AppCompatTextView

    companion object {
        fun newInstance(macAddress: String?, serialNumber: String?, manufacturerName: String?): DeviceDetailsDialogFragment =
            DeviceDetailsDialogFragment().apply {
                arguments = Bundle().apply {
                    putString("macAddress", macAddress ?: "")
                    putString("serialNumber", serialNumber ?: "")
                    putString("manufacturerName", manufacturerName ?: "")
                }
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_device_details, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        serialNumberTextView = view.findViewById(R.id.serial_number_text_view)
        macAddressTextView = view.findViewById(R.id.mac_address_text_view)
        manufacturerNameTextView = view.findViewById(R.id.manufacturer_name_text_view)

        serialNumberTextView.text = arguments?.getString("serialNumber")
        macAddressTextView.text = arguments?.getString("macAddress")
        manufacturerNameTextView.text = arguments?.getString("manufacturerName")
    }
}