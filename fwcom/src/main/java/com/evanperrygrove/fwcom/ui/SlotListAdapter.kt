package com.evanperrygrove.fwcom.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.evanperrygrove.fwcom.R
import com.evanperrygrove.fwcom.dmx.SlotInfo
import com.google.android.material.slider.Slider

class SlotListAdapter(private val slotInfos: List<SlotInfo>, private val onSlotValueChanged: OnSlotValueChanged): RecyclerView.Adapter<SlotListAdapter.ViewHolder>() {
    abstract inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        abstract fun bind(slotInfo: SlotInfo)
        abstract fun setOnValueChanged(callback: (view: View, value: UByte) -> Unit)
    }

    fun interface OnSlotValueChanged {
        fun onSlotListValueChanged(slotInfo: SlotInfo, value: UByte)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val view = inflater.inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.fragment_slot_slider -> SliderViewHolder(view)
            else -> throw IllegalArgumentException()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val slotInfo = slotInfos.elementAt(position)
        holder.bind(slotInfo)
        holder.setOnValueChanged { _, value ->
            slotInfo.value = value
            onSlotValueChanged.onSlotListValueChanged(slotInfo, value)
        }
    }

    override fun getItemCount(): Int {
        return slotInfos.size
    }

    override fun getItemViewType(position: Int): Int {
        val slotInfo = slotInfos.elementAt(position)
        return slotInfo.slotLayoutId
    }

    inner class SliderViewHolder(itemView: View): ViewHolder(itemView) {
        val nameTextView = itemView.findViewById<TextView>(R.id.slot_name)
        val sliderView = itemView.findViewById<Slider>(R.id.slider)

        private var onChangeCallback: (view: View, value: UByte) -> Unit = { _, _ -> }
        private val touchListener = object: Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}

            override fun onStopTrackingTouch(slider: Slider) {
                onChangeCallback(slider, slider.value.toUInt().toUByte())
            }
        }

        override fun bind(slotInfo: SlotInfo) {
            nameTextView.text = slotInfo.name
            sliderView.value = slotInfo.value.toFloat()
            sliderView.removeOnSliderTouchListener(touchListener)
            sliderView.addOnSliderTouchListener(touchListener)
        }

        override fun setOnValueChanged(callback: (view: View, value: UByte) -> Unit) {
            onChangeCallback = callback
        }
    }
}