package com.example.gpsmapcamera.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.ItemTopTemplateBinding
import com.example.gpsmapcamera.databinding.StampTopTemplate1PreviewBinding
import com.example.gpsmapcamera.databinding.StampTopTemplate2PreviewBinding
import com.example.gpsmapcamera.databinding.StampTopTemplate3PreviewBinding
import com.example.gpsmapcamera.databinding.StampTopTemplate4PreviewBinding
import com.example.gpsmapcamera.databinding.StampTopTemplate5PreviewBinding
import com.example.gpsmapcamera.databinding.StampTopTemplate6PreviewBinding
import com.example.gpsmapcamera.models.DynamicStampValues
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.formatDate
import com.example.gpsmapcamera.utils.formatTimeZone
import com.example.gpsmapcamera.utils.toPressure
import com.example.gpsmapcamera.utils.toTemperature
import com.example.gpsmapcamera.utils.toWindSpeed
import com.example.gpsmapcamera.utils.updateAddressWithVisibility

class TopTemplatesAdapter(
    private val layouts: List<Int>,
    private val onItemSelected: (Int) -> Unit
) : RecyclerView.Adapter<TopTemplatesAdapter.TemplateViewHolder>() {

    var selectedPosition = -1
    private var currentDynamics = DynamicStampValues()


    inner class TemplateViewHolder(val binding: ItemTopTemplateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val inflater = LayoutInflater.from(binding.root.context)

        // Inflate once and reuse
        private val templateOneBinding = StampTopTemplate1PreviewBinding.inflate(inflater)
        private val templateTwoBinding = StampTopTemplate2PreviewBinding.inflate(inflater)
        private val templateThreeBinding = StampTopTemplate3PreviewBinding.inflate(inflater)
        private val templateFourBinding = StampTopTemplate4PreviewBinding.inflate(inflater)
        private val templateFiveBinding = StampTopTemplate5PreviewBinding.inflate(inflater)
        private val templateSixBinding = StampTopTemplate6PreviewBinding.inflate(inflater)

        fun bind(position: Int) = binding.run {
            // Remove existing view
            cardRoot.removeAllViews()

            val foregroundDrawable = if (position == selectedPosition)
                R.drawable.bg_template_selected
            else
                null

            cardRoot.foreground =
                foregroundDrawable?.let { ContextCompat.getDrawable(cardRoot.context, it) }

            cardRoot.setOnClickListener {
                val prevSelected = selectedPosition
                selectedPosition = position
                notifyItemChanged(prevSelected)
                notifyItemChanged(selectedPosition)
                onItemSelected(position)
            }

            val viewToAdd = when (position) {
                0 -> templateOneBinding.apply {
                    tvCenterTitle.text = currentDynamics.shortAddress
                    val time = currentDynamics.dateTime
                    tvTimeDate.text = buildString {
                        append(time?.formatDate(root.context, Constants.CLASSIC_TEMPLATE))
                        append(" ")
                        append(time?.formatTimeZone(root.context, Constants.CLASSIC_TEMPLATE))
                    }
                    tvTemperature.text = currentDynamics.weather.toTemperature(
                        root.context,
                        Constants.CLASSIC_TEMPLATE
                    )
                }.root

                1 -> templateTwoBinding.apply {

                    tvCenterTitle.text = currentDynamics.shortAddress
                    val time = currentDynamics.dateTime
                    tvTimeDate.text = buildString {
                        append(time?.formatDate(root.context, Constants.CLASSIC_TEMPLATE))
                        append(" ")
                        append(time?.formatTimeZone(root.context, Constants.CLASSIC_TEMPLATE))
                    }
                    tvTemperature.text = currentDynamics.weather.toTemperature(
                        root.context,
                        Constants.CLASSIC_TEMPLATE
                    )
                    tvLatitude.text = buildString {
                        append("Latitude : ")
                        append(currentDynamics.latLong?.lat)
                    }
                    tvLongitude.text = buildString {
                        append("Longitude : ")
                        append(currentDynamics.latLong?.lon)
                    }
                }.root

                2 -> templateThreeBinding.apply {

                    tvCenterTitle.text = currentDynamics.shortAddress
                    tvCity.text = currentDynamics.shortAddress
                    val formattedAddress = currentDynamics.fullAddress.updateAddressWithVisibility(binding.root.context,
                        Constants.CLASSIC_TEMPLATE)

                    tvFullAddress.text = formattedAddress
                    tvTemperature.text = currentDynamics.weather.toTemperature(
                        root.context,
                        Constants.CLASSIC_TEMPLATE
                    )
                    val time = currentDynamics.dateTime
                    tvDateTime.text = buildString {
                        append(time?.formatDate(root.context, Constants.CLASSIC_TEMPLATE))
                        append(" ")
                        append(time?.formatTimeZone(root.context, Constants.CLASSIC_TEMPLATE))
                    }

                    tvCompass.text = currentDynamics.compass
                    tvMagnetic.text = currentDynamics.magneticField
                    tvPressure.text = currentDynamics.pressure.toPressure(root.context, Constants.CLASSIC_TEMPLATE)
                    tvWind.text = currentDynamics.wind.toWindSpeed(root.context, Constants.CLASSIC_TEMPLATE)
                    tvHumidity.text = currentDynamics.humidity.toString()
                }.root

                3 -> templateFourBinding.apply {


                    tvCenterTitle.text = currentDynamics.shortAddress
                    val formattedAddress = currentDynamics.fullAddress.updateAddressWithVisibility(binding.root.context,
                        Constants.CLASSIC_TEMPLATE)

                    tvFullAddress.text = formattedAddress
                    tvTemperature.text = currentDynamics.weather.toTemperature(
                        root.context,
                        Constants.CLASSIC_TEMPLATE
                    )
                    val time = currentDynamics.dateTime
                    tvDateTime.text = buildString {
                        append(time?.formatDate(root.context, Constants.CLASSIC_TEMPLATE))
                        append(" ")
                        append(time?.formatTimeZone(root.context, Constants.CLASSIC_TEMPLATE))
                    }

                    tvPressure.text = currentDynamics.pressure.toPressure(root.context, Constants.CLASSIC_TEMPLATE)
                    tvWind.text = currentDynamics.wind.toWindSpeed(root.context, Constants.CLASSIC_TEMPLATE)
                    tvHumidity.text = currentDynamics.humidity.toString()

                }.root

                4 -> templateFiveBinding.apply {

                    tvCenterTitle.text = currentDynamics.shortAddress
                    tvLatLong.text = buildString {
                        append("Lat : ")
                        append(currentDynamics.latLong?.lat)
                        append("Lon : ")
                        append(currentDynamics.latLong?.lon)
                    }

                    tvTemperature.text = currentDynamics.weather.toTemperature(
                        root.context,
                        Constants.CLASSIC_TEMPLATE
                    )
                    val time = currentDynamics.dateTime
                    tvDateTime.text = buildString {
                        append(time?.formatDate(root.context, Constants.CLASSIC_TEMPLATE))
                        append(" ")
                        append(time?.formatTimeZone(root.context, Constants.CLASSIC_TEMPLATE))
                    }

                    tvCompass.text = currentDynamics.compass
                    tvMagnetic.text = currentDynamics.magneticField
                    tvPressure.text = currentDynamics.pressure.toPressure(root.context, Constants.CLASSIC_TEMPLATE)
                    tvWind.text = currentDynamics.wind.toWindSpeed(root.context, Constants.CLASSIC_TEMPLATE)
                }.root

                5 -> templateSixBinding.apply {


                    tvCenterTitle.text = currentDynamics.shortAddress
                    tvLatLong.text = buildString {
                        append("Lat : ")
                        append(currentDynamics.latLong?.lat)
                        append("Lon : ")
                        append(currentDynamics.latLong?.lon)
                    }

                    val time = currentDynamics.dateTime
                    tvDateTime.text = buildString {
                        append(time?.formatDate(root.context, Constants.CLASSIC_TEMPLATE))
                        append(" ")
                        append(time?.formatTimeZone(root.context, Constants.CLASSIC_TEMPLATE))
                    }

                    tvPressure.text = currentDynamics.pressure.toPressure(root.context, Constants.CLASSIC_TEMPLATE)
                    tvWind.text = currentDynamics.wind.toWindSpeed(root.context, Constants.CLASSIC_TEMPLATE)

                }.root

                else -> null
            }

            viewToAdd?.let { v ->
                // Detach first if it already has a parent
                (v.parent as? ViewGroup)?.removeView(v)
                binding.cardRoot.addView(v)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val binding = ItemTopTemplateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TemplateViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        holder.bind(position)
    }

    fun updateDynamics(newDynamics: DynamicStampValues) {
        currentDynamics = newDynamics
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = layouts.size
}

