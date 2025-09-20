package com.example.gpsmapcamera.adapters

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.SingleItemTemplateBinding
import com.example.gpsmapcamera.models.DynamicStampValues
import com.example.gpsmapcamera.models.StampConfig
import com.example.gpsmapcamera.models.StampItemName
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager
import com.example.gpsmapcamera.utils.getFontSizeFactor
import com.example.gpsmapcamera.utils.stampFontList
import com.example.gpsmapcamera.utils.toAccuracy
import com.example.gpsmapcamera.utils.toAltitude
import com.example.gpsmapcamera.utils.toPressure
import com.example.gpsmapcamera.utils.toTemperature
import com.example.gpsmapcamera.utils.toWindSpeed

/*

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.databinding.SingleItemTemplateBinding
import com.example.gpsmapcamera.models.StampModel
import com.example.gpsmapcamera.utils.gone
import com.example.gpsmapcamera.utils.visible

class StampAdapter(
    private val items: List<StampModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    // ViewHolders using ViewBinding
    inner class TextViewHolder(val binding: SingleItemTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StampModel) {
            binding.titleTv.text = item.title
            item.icon?.let {
                binding.ivMain.visible()
                binding.ivMain.setImageResource(it)
            }?:run {
                binding.ivMain.gone()
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = SingleItemTemplateBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TextViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is TextViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount() = items.size
}
*/



class StampAdapter(
    var template: String = ""
) : RecyclerView.Adapter<StampAdapter.ViewHolder>() {
    private var configs: ArrayList<StampConfig> = arrayListOf()

    private var currentDynamics = DynamicStampValues()

    inner class ViewHolder(val binding: SingleItemTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(config: StampConfig) = binding.run {

            val getScaleValue = root.context.getFontSizeFactor(template)
            val baseTextSize =
                root.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._6sdp).toFloat()
            titleTv.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSize * getScaleValue)


            val typeface = ResourcesCompat.getFont(
                root.context, stampFontList[PrefManager.getInt(
                    root.context,
                    Constants.SELECTED_STAMP_FONT + template,
                    0
                )]
            )
            titleTv.typeface = typeface

            if (config.name == StampItemName.CONTACT_NO) {
                titleTv.text = PrefManager.getString(root.context, Constants.ADDED_PHONE_NUMBER)
            } else if (config.name == StampItemName.WEATHER) {
                titleTv.text = currentDynamics.weather.toTemperature(root.context, template)
            } else if (config.name == StampItemName.WIND) {
                titleTv.text = currentDynamics.wind.toWindSpeed(root.context, template)
            } else if (config.name == StampItemName.PRESSURE) {
                titleTv.text = currentDynamics.pressure.toPressure(root.context, template)
            } else if (config.name == StampItemName.ALTITUDE) {
                titleTv.text = currentDynamics.altitude.toAltitude(root.context, template)
            } else if (config.name == StampItemName.ACCURACY) {
                titleTv.text = currentDynamics.accuracy.toAccuracy(root.context, template)
            } else {
                titleTv.text =
                    (root.context.applicationContext as MyApp).appViewModel.getEffectiveTitle(
                        config,
                        currentDynamics
                    )
            }
            Glide.with(root.context).load(config.icon).into(ivMain)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            SingleItemTemplateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val config = configs[position]
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

    fun updateDynamics(newDynamics: DynamicStampValues) {
        currentDynamics = newDynamics
        notifyDataSetChanged()
    }
}