package com.evanperrygrove.fwcomapp

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.evanperrygrove.fwcom.Device
import com.evanperrygrove.fwcom.device.BleDevice
import com.evanperrygrove.fwcom.dmx.DmxConfig
import com.evanperrygrove.fwcom.dmx.PersonalityInfo
import com.evanperrygrove.fwcom.dmx.SlotInfo
import com.evanperrygrove.fwcom.ui.SlotListAdapter
import com.evanperrygrove.fwcom.userapp.DmxInfo
import com.evanperrygrove.fwcom.util.Scanner
import com.evanperrygrove.fwcom.util.StoredDevice
import com.evanperrygrove.fwcom.util.toStoredDevice
import com.evanperrygrove.fwcomapp.databinding.FragmentDeviceControlBinding
import com.evanperrygrove.fwcomapp.ui.PersonalityCardView
import com.evanperrygrove.fwcomapp.ui.PersonalitySelectDialog
import com.google.android.material.progressindicator.LinearProgressIndicator

class DeviceControlFragment : Fragment(),
    Device.OnDisconnectFinished,
    Device.OnConnect,
    Device.OnDmxPersonalityInfoReceived,
    Device.OnDmxSlotInfoReceived,
    Device.OnStatusMessageReceived,
    Device.OnSlotValueChanged,
    Device.OnDmxInfoChanged,
    Device.OnManufacturerNameReceived,
    Device.OnSerialNumberReceived
{
    private var _binding: FragmentDeviceControlBinding? = null
    private val binding get() = _binding!!

    private var scanner: Scanner? = null
    private var device: Device? = null

    /** Contains a UI element for each scanned device */
    private lateinit var recyclerView: RecyclerView

    /** Linear progress indicator at top of the fragment */
    private lateinit var progressIndicator: LinearProgressIndicator

    private lateinit var personalityCardView: PersonalityCardView

    private val slotInfos = mutableListOf<SlotInfo>()
    private val availablePersonalities = mutableListOf<PersonalityInfo>()

    private var selectedPersonalityNumber = -1
    private var numberAvailablePersonalities = 0
    private var numberSlotsInPersonality = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Props.reset()
        _binding = FragmentDeviceControlBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val macAddress = arguments?.getParcelable<StoredDevice>("device")!!.address

        val activity = requireActivity()
        val context = requireContext()

        scanner = Scanner(activity, object: ScanCallback() {
            @SuppressLint("MissingPermission")
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                scanner?.stopScanning()
                val storedDevice = result?.toStoredDevice() ?: return
                val btDevice = result.device ?: return
                onDeviceFoundInScan(storedDevice, btDevice)
            }
        })

        recyclerView = view.findViewById<View>(R.id.device_control_recycler_view) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = SlotListAdapter(slotInfos) { slotInfo, value ->
            Log.d("sendSlotValue", "slotInfo=$slotInfo, value=$value")
            device?.sendSlotValue(slotInfo.slotNumber, value)
        }

        progressIndicator = view.findViewById<View>(R.id.linear_progress) as LinearProgressIndicator

        Props.macAddress = macAddress

        personalityCardView = PersonalityCardView(view.findViewById(R.id.personality_card_view))
        personalityCardView.enabled = false
        personalityCardView.setOnClickListener {
            val personalityList = availablePersonalities.filter { it.personalityNumber != selectedPersonalityNumber }
            val dialog = PersonalitySelectDialog(context, personalityList) { personality ->
                val dmxConfig = DmxConfig(0, personality.slotCount, personality.personalityNumber)
                device?.sendDmxConfig(dmxConfig)
            }
            dialog.show()
        }

        scanner?.filterByMacAddress(macAddress)
        scanner?.onCreate()

        startScanning()
    }

    @SuppressLint("MissingPermission")
    override fun onDestroyView() {
        device?.onDisconnect = Device.OnDisconnectFinished { _, _ -> }
        scanner?.stopScanning()
        device?.disconnect()
        Props.reset()
        super.onDestroyView()
    }

    fun startScanning() {
        activity?.runOnUiThread {
            scanner?.startScanning()
            progressIndicator.visibility = View.VISIBLE
            progressIndicator.isIndeterminate = true
        }
    }

    private fun onDeviceFoundInScan(storedDevice: StoredDevice, btDevice: BluetoothDevice) {
        device = BleDevice(requireContext(), storedDevice, btDevice)
        device?.onConnect = this
        device?.onDisconnect = this
        device?.onDmxPersonalityInfo = this
        device?.onDmxSlotInfo = this
        device?.onSlotValueChanged = this
        device?.onDmxInfoChanged = this
        device?.connect()
        activity?.runOnUiThread {
            progressIndicator.isIndeterminate = false
            progressIndicator.progress = 30
        }
    }

    override fun onConnectFinished(didSucceed: Boolean, statusCode: Int) {
        Log.d("onConnectFinished", "didSucceed=$didSucceed statusCode=$statusCode")
        if (didSucceed) {
            activity?.runOnUiThread {
                progressIndicator.visibility = View.VISIBLE
                progressIndicator.progress = 50

                // Get basic info about current DMX personality. Triggers onDmxInfoChanged
                device?.requestDmxInfo()
            }
        }
    }

    override fun onDisconnectFinished(didSucceed: Boolean, statusCode: Int) {
        Log.d("onDisconnectFinished", "didSucceed=$didSucceed statusCode=$statusCode")
        activity?.runOnUiThread {
            Toast.makeText(context,
                com.evanperrygrove.fwcom.R.string.conn_lost_reconnecting,
                Toast.LENGTH_LONG).show()
            slotInfos.clear()
            recyclerView.adapter?.notifyDataSetChanged()
            personalityCardView.reset()
            selectedPersonalityNumber = -1
            numberAvailablePersonalities = 0
            numberSlotsInPersonality = -1
            startScanning()
        }
    }

    override fun onDmxPersonalityInfoReceived(info: PersonalityInfo) {
        Log.d("PersonalityInfo", "$info")

        val index = availablePersonalities.indexOfFirst { it.personalityNumber == info.personalityNumber }
        if (index >= 0) {
            availablePersonalities[index] = info
        } else {
            availablePersonalities.add(info)
        }

        if (info.personalityNumber == selectedPersonalityNumber) {
            numberSlotsInPersonality = info.slotCount
            if (index < 0) {
                device?.requestDmxSlotInfo(info.personalityNumber)
            }
            activity?.runOnUiThread {
                personalityCardView.personalityName = info.name
            }
        }

        if (availablePersonalities.size == numberAvailablePersonalities) {
            // all personalities have been retrieved
            activity?.runOnUiThread {
                personalityCardView.enabled = true
                device?.readManufacturerName(this::onManufacturerNameReceived)
                device?.readSerialNumber(this::onSerialNumberReceived)
            }
        }
    }

    override fun onDmxSlotInfoReceived(info: SlotInfo) {
        Log.d("SlotInfo", "$info")
        if (info.personalityNumber == selectedPersonalityNumber) {
            activity?.runOnUiThread {
                progressIndicator.visibility = View.INVISIBLE
                val index = slotInfos.indexOfFirst { it.slotNumber == info.slotNumber }
                if (index >= 0) {
                    slotInfos[index] = info
                    recyclerView.adapter?.notifyItemChanged(index)
                } else {
                    slotInfos.add(info)
                    recyclerView.adapter?.notifyItemInserted(slotInfos.size - 1)
                }
            }
        }
    }

    override fun onStatusMessageReceived(message: String) {
        Log.w("onStatusMessageReceived", "Device said: $message")
        activity?.runOnUiThread {
            Toast.makeText(context,
                "Device: $message",
                Toast.LENGTH_LONG).show()
        }
    }

    override fun onSlotValueChanged(slotNumber: Int, value: UByte) {
        activity?.runOnUiThread {
            val index = slotInfos.indexOfFirst { it.slotNumber == slotNumber }
            slotInfos[index].value = value
            recyclerView.adapter?.notifyItemChanged(index)
        }
    }

    override fun onDmxInfoChanged(dmxInfo: DmxInfo) {
        Log.d("DmxInfo", "$dmxInfo")
        activity?.runOnUiThread {
            numberAvailablePersonalities = dmxInfo.personalityCount
            if (selectedPersonalityNumber == -1) {
                selectedPersonalityNumber = dmxInfo.selectedPersonality
                device?.requestDmxPersonalityInfo(0 until numberAvailablePersonalities)
            } else {
                slotInfos.clear()
                recyclerView.adapter?.notifyDataSetChanged()
                selectedPersonalityNumber = dmxInfo.selectedPersonality
                device?.requestDmxSlotInfo(selectedPersonalityNumber)
            }
        }
    }

    override fun onSerialNumberReceived(serialNumber: String?) {
        activity?.runOnUiThread {
            Props.serialNumber = serialNumber
        }
    }

    override fun onManufacturerNameReceived(manufacturerName: String?) {
        activity?.runOnUiThread {
            Props.manufacturerName = manufacturerName
        }
    }

    companion object {
        fun createBundle(device: StoredDevice): Bundle =
            bundleOf(
                "device" to device,
                "title" to device.nickname
            )
    }

    object Props {
        var serialNumber: String? = null
        var macAddress: String? = null
        var manufacturerName: String? = null

        internal fun reset() {
            serialNumber = null
            macAddress = null
            manufacturerName = null
        }
    }
}