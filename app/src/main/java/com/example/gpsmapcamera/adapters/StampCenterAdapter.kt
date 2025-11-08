package com.example.gpsmapcamera.adapters

import android.graphics.Typeface
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.databinding.SingleCenterItemTemplateBinding
import com.example.gpsmapcamera.models.DynamicStampValues
import com.example.gpsmapcamera.models.StampConfig
import com.example.gpsmapcamera.models.StampItemName
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager
import com.example.gpsmapcamera.utils.StampPreferences
import com.example.gpsmapcamera.utils.formatDate
import com.example.gpsmapcamera.utils.formatLatLong
import com.example.gpsmapcamera.utils.formatPlusCode
import com.example.gpsmapcamera.utils.formatTimeZone
import com.example.gpsmapcamera.utils.getFontSizeFactor
import com.example.gpsmapcamera.utils.gone
import com.example.gpsmapcamera.utils.recentNotesDefault
import com.example.gpsmapcamera.utils.stampFontList
import com.example.gpsmapcamera.utils.updateAddressWithVisibility
import com.example.gpsmapcamera.utils.visible


class StampCenterAdapter(
    var template: String = ""
) : RecyclerView.Adapter<StampCenterAdapter.ViewHolder>() {

    private var configs: ArrayList<StampConfig> = arrayListOf()

    private var lastKnownAddress: String? = null

    private var currentDynamics = DynamicStampValues()


    inner class ViewHolder(val binding: SingleCenterItemTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(config: StampConfig) = binding.run {
            // Base text size (adjust to your item layout's text size, e.g., _10sdp)
            val getScaleValue = root.context.getFontSizeFactor(template)
            val baseTextSize =
                root.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._6sdp).toFloat()
            titleTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSize * getScaleValue)


            val selectedIndex = PrefManager.getInt(
                root.context,
                Constants.SELECTED_STAMP_FONT + template,
                0
            )

            val fontRes = stampFontList.getOrNull(selectedIndex)
            val typeface = if (fontRes != null) {
                ResourcesCompat.getFont(root.context, fontRes)
            } else {
                Typeface.DEFAULT // system default font
            }

            titleTv.typeface = typeface

/*
            if (config.name == StampItemName.FULL_ADDRESS) {
                Log.d("TAG", "bind: StampItemName.FULL_ADDRESS ${currentDynamics.fullAddress}")
                val address = currentDynamics.fullAddress
                titleTv.text = address.updateAddressWithVisibility(binding.root.context, template)
                Log.d("TAG", "titleTv.text  ${titleTv.text}")

            }*/
            if (config.name == StampItemName.FULL_ADDRESS) {
                val address = currentDynamics.fullAddress
                val formatted = address.updateAddressWithVisibility(binding.root.context, template)

                if (formatted.isNotBlank()) {
                    lastKnownAddress = formatted
                    titleTv.text = formatted
                } else if (!lastKnownAddress.isNullOrBlank()) {
                    // Use last known address to prevent flicker/disappearance
                    titleTv.text = lastKnownAddress
                } else {
                    titleTv.text = ""
                }
                Log.d("TAG", "titleTv.text  ${titleTv.text}")
            } else if (config.name == StampItemName.DATE_TIME || config.name == StampItemName.TIME_ZONE) {
                val time = currentDynamics.dateTime
                titleTv.text = buildString {
                    append(time?.formatDate(root.context, template))
                    append(" ")
                    append(time?.formatTimeZone(root.context, template))
                }
            } else if (config.name == StampItemName.LAT_LONG) {
                val latLong = currentDynamics.latLong
                titleTv.text = buildString {
                    append(latLong?.formatLatLong(root.context, template))
                }
            } else if (config.name == StampItemName.PERSON_NAME) {
                val personName = PrefManager.getString(
                    root.context,
                    Constants.SELECTED_PERSON_NAME + template,
                    ""
                )
                titleTv.text = buildString {
                    append("Person Name : ")
                    append(personName)
                }
            } else if (config.name == StampItemName.PLUS_CODE) {
                val latLong = currentDynamics.latLong
                titleTv.text = buildString {
                    append(latLong?.formatPlusCode(root.context, template))
                }
            } else if (config.name == StampItemName.NOTE) {
                val prefList =
                    StampPreferences(root.context).getNoteModel(Constants.KEY_RECENT_NOTES)
                val selectedIndex = PrefManager.getInt(
                    root.context, Constants.SELECTED_NOTE, 0
                )
                val noteData =
                    if (prefList.isNotEmpty()) prefList[selectedIndex] else recentNotesDefault[selectedIndex]


                titleTv.text = buildString {
                    append(noteData.note)
                    append(" ")
                    append(noteData.title)
                }
            } else if (config.name == StampItemName.NUMBERING) {
                val prefix =
                    PrefManager.getString(root.context, Constants.NUMBERING_PREFIX + template, "")
                val suffix =
                    PrefManager.getString(root.context, Constants.NUMBERING_SUFFIX + template, "")
                val sequenceNumber = PrefManager.getInt(
                    root.context,
                    Constants.NUMBERING_SEQUENCE_NUMBER + template,
                    1
                )
                titleTv.text = buildString {
                    append(prefix)
                    append(" ")
                    append(sequenceNumber.toString())
                    append(" ")
                    append(suffix)
                }
            } else {
                titleTv.text =
                    (root.context.applicationContext as MyApp).appViewModel.getEffectiveTitle(
                        config, currentDynamics
                    )
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = SingleCenterItemTemplateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val config = configs[position]
        // Skip TIME_ZONE row, because it's merged into DATE_TIME
        if (config.name == StampItemName.TIME_ZONE) {
            // Hide TIME_ZONE item safely
            holder.binding.root.gone()
            holder.binding.root.layoutParams.height = 0
            return
        } else {
            // Reset for reused views (important!)
            holder.binding.root.visible()
            holder.binding.root.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        holder.bind(config)
        // Set icon, etc.
    }

    override fun getItemCount(): Int = configs.size

    fun submitList(newConfigs: ArrayList<StampConfig>) {
        if (configs != newConfigs) {
            configs.clear()
            configs = newConfigs
            notifyDataSetChanged() // Use DiffUtil for better performance if needed
        }
    }

    fun updateDynamics(newDynamics: DynamicStampValues,passedTemplate: String = Constants.CLASSIC_TEMPLATE) {
        currentDynamics = newDynamics
        template = passedTemplate
        notifyDataSetChanged() // not full data set
    }

}

