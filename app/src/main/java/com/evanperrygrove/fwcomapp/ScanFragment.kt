package com.evanperrygrove.fwcomapp

import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.evanperrygrove.fwcom.ui.ScanAdapter
import com.evanperrygrove.fwcom.util.Defer
import com.evanperrygrove.fwcom.util.Scanner
import com.evanperrygrove.fwcom.util.StoredDevice
import com.evanperrygrove.fwcom.util.toStoredDevice
import com.evanperrygrove.fwcomapp.databinding.FragmentScanBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!

    private var scanner: Scanner? = null

    /** List all devices found by the scanner */
    private val foundDevices: LinkedHashSet<StoredDevice> = linkedSetOf()

    /** Contains a UI element for each scanned device */
    private lateinit var recyclerView: RecyclerView

    /** Contains a helpful hint messages */
    private lateinit var scanTextView: TextView

    /** `scanTextView` will rotate through these strings */
    private val scanTextValues = arrayOf(
        R.string.make_sure_device_powered_on,
        R.string.make_sure_android_near_device
    )

    /** holds the index of the scanTextValues string currently shown in scanTextView */
    private var scanTextValuesState = 0

    /** number of milliseconds that each string is shown inside scanTextView before changing */
    private val scanTextDisplayTime = 5000L

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scanner = Scanner(requireActivity(), object: ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult?) {
                result?.toStoredDevice()?.let {
                    if (foundDevices.add(it)) {
                        recyclerView.adapter?.notifyItemInserted(foundDevices.size - 1)
                    }
                }
            }
        })

        recyclerView = view.findViewById<View>(R.id.scan_recycler_view) as RecyclerView
        recyclerView.adapter = ScanAdapter(foundDevices)
        recyclerView.layoutManager = LinearLayoutManager(context)

        scanTextView = view.findViewById<View>(R.id.textview_scan) as TextView

        scanner?.onCreate()
        scanner?.startScanning()

        Defer.post(Looper.getMainLooper(), scanTextDisplayTime, this::rotateScanTextView)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("ScanFragment", "onDestroyView")
        Defer.cancel(Looper.getMainLooper(), this::rotateScanTextView)
        scanner?.stopScanning()
        _binding = null
        scanner = null
    }

    private fun rotateScanTextView() {
        scanTextValuesState = (scanTextValuesState + 1) % scanTextValues.size
        try {
            scanTextView.text = getString(scanTextValues[scanTextValuesState])
            Defer.post(Looper.getMainLooper(), scanTextDisplayTime, this::rotateScanTextView)
        } catch (e: IllegalStateException) {
            Log.d("rotateScanTextView", "IllegalStateException in rotateScanTextView, ignoring gracefully")
        }
    }
}