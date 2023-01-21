package com.evanperrygrove.fwcom.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.evanperrygrove.fwcom.R
import com.evanperrygrove.fwcom.util.DeviceStorage
import com.evanperrygrove.fwcom.util.StoredDevice

class ScanAdapter(private val storedDevices: Set<StoredDevice>, private val buttonText: String? = null): RecyclerView.Adapter<ScanAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val addressTextView = itemView.findViewById<TextView>(R.id.address)
        val nameTextView = itemView.findViewById<TextView>(R.id.name)
        val nicknameTextView = itemView.findViewById<TextView>(R.id.nickname)
        val addButton = itemView.findViewById<Button>(R.id.add_button)
    }

    val onButtonClickListener: (View, StoredDevice) -> Unit = { view, device ->
        DeviceStorage.write(view.context!!) {
            it.add(device)
        }
        view.isEnabled = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_scanned_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device: StoredDevice = storedDevices.elementAt(position)
        holder.nameTextView.text = device.name
        holder.nicknameTextView.text = device.nickname ?: device.name
        holder.addressTextView.text = device.address

        buttonText?.let {
            holder.addButton.text = it
        }

        holder.addButton.setOnClickListener { view -> onButtonClickListener(view, device) }

        holder.addButton.isEnabled = DeviceStorage.read(holder.addButton.context!!) {
            !it.contains(device)
        }
    }

    override fun getItemCount(): Int {
        return storedDevices.size
    }
}