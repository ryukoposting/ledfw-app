package com.evanperrygrove.fwcomapp.ui

import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import com.evanperrygrove.fwcomapp.R
import com.google.android.material.card.MaterialCardView

class PersonalityCardView(val view: MaterialCardView) {
    private val titleTextView: AppCompatTextView = view.findViewById(R.id.personality_card_title_text_view)
    private val iconImageView: AppCompatImageView = view.findViewById(R.id.personality_card_config_icon)
    private val personalityNameTextView: AppCompatTextView = view.findViewById(R.id.personality_name_text_view)

    private var onClickListener: View.OnClickListener? = null

    init {
        view.setOnClickListener { view ->
            if (_enabled) {
                onClickListener?.let { it.onClick(view) }
            }
        }
    }


    var _enabled = true
    var enabled: Boolean
        get() = _enabled
        set(value) {
            if (value != _enabled) {
                _enabled = value

                if (_enabled) {
                    iconImageView.visibility = View.VISIBLE
                } else {
                    iconImageView.visibility = View.INVISIBLE
                }
            }
        }


    var title: String?
        get() = titleTextView.text.toString()
        set(value) {
            titleTextView.text = value
        }

    var personalityName: String?
        get() = personalityNameTextView.text.toString()
        set(value) {
            personalityNameTextView.text = value
        }

    fun setOnClickListener(listener: View.OnClickListener) {
        onClickListener = listener
    }

    fun reset() {
        personalityName = ""
    }
}