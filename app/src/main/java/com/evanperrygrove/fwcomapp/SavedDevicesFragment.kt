package com.evanperrygrove.fwcomapp

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.FragmentNavigatorDestinationBuilder
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.evanperrygrove.fwcom.ui.StoredDeviceAdapter
import com.evanperrygrove.fwcom.util.DeviceStorage
import com.evanperrygrove.fwcomapp.databinding.FragmentSavedBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class SavedDevicesFragment : Fragment() {
    private var _binding: FragmentSavedBinding? = null
    private val binding get() = _binding!!

    /** Contains a UI element for each saved device */
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSavedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById<View>(R.id.saved_recycler_view) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = DeviceStorage.read(view.context!!) { StoredDeviceAdapter(recyclerView, it) }
        adapter.onButtonClickListener = { _, device ->
            val bundle = DeviceControlFragment.createBundle(device)
            findNavController().navigate(R.id.action_FirstFragment_to_DeviceControlFragment, bundle)
        }

        recyclerView.adapter = adapter

        binding.fab.setOnClickListener { _ ->
            findNavController().navigate(R.id.action_FirstFragment_to_ScanFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}