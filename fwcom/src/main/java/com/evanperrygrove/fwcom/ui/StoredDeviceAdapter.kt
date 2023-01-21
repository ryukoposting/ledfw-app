package com.evanperrygrove.fwcom.ui

import android.transition.TransitionManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.evanperrygrove.fwcom.R
import com.evanperrygrove.fwcom.util.DeviceStorage
import com.evanperrygrove.fwcom.util.StoredDevice

class StoredDeviceAdapter(private val recyclerView: RecyclerView, private val storedDevices: Set<StoredDevice>, private val buttonText: String? = null): RecyclerView.Adapter<StoredDeviceAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val nameTextView = itemView.findViewById<TextView>(R.id.name)
        val nicknameTextView = itemView.findViewById<TextView>(R.id.nickname)
        val openButton = itemView.findViewById<Button>(R.id.open_button)
        val detail = itemView.findViewById<View>(R.id.detail)
        val infoButton = itemView.findViewById<View>(R.id.more_details_button)
        val deleteButton = itemView.findViewById<View>(R.id.delete_button)
    }

    private var expandedPosition: Int? = null

    var onButtonClickListener: (View, StoredDevice) -> Unit = { _, device ->
        Log.d("StoredDeviceAdapter", "Device \"${device.name}\" was selected, but onButtonClickListener has not been set.")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(R.layout.fragment_stored_device, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val device: StoredDevice = storedDevices.elementAt(position)
        val isExpanded = expandedPosition?.let { it == position } ?: false

        holder.nameTextView.text = device.name
        holder.nicknameTextView.text = device.nickname ?: device.name
        holder.detail.visibility = if (isExpanded) View.VISIBLE else View.GONE

        holder.itemView.setOnClickListener { _ ->
            expandedPosition = if (isExpanded) null else position
            TransitionManager.beginDelayedTransition(recyclerView)
            notifyDataSetChanged()
        }

        holder.deleteButton.setOnClickListener { view ->
            expandedPosition = null
            DeviceStorage.write(view.context!!) {
                it.remove(device)
            }
            notifyDataSetChanged()
        }

        buttonText?.let {
            holder.openButton.text = it
        }

        holder.openButton.setOnClickListener { view -> onButtonClickListener(view, device) }
    }

    override fun getItemCount(): Int {
        return storedDevices.size
    }
}