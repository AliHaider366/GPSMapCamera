package com.example.gpsmapcamera.activities.template.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.template.EditTemplateActivity
import com.example.gpsmapcamera.activities.template.basic.AddPersonActivity
import com.example.gpsmapcamera.activities.template.basic.AddNoteActivity
import com.example.gpsmapcamera.activities.template.basic.ContactNumberActivity
import com.example.gpsmapcamera.activities.template.basic.CoordinateLatLongActivity
import com.example.gpsmapcamera.activities.template.basic.DateFormatActivity
import com.example.gpsmapcamera.activities.template.basic.FullAddressActivity
import com.example.gpsmapcamera.activities.template.basic.ReportingTagActivity
import com.example.gpsmapcamera.activities.template.stampsetting.MapPositionActivity
import com.example.gpsmapcamera.activities.template.stampsetting.MapScaleActivity
import com.example.gpsmapcamera.activities.template.stampsetting.MapTypeActivity
import com.example.gpsmapcamera.activities.template.stampsetting.StampFontActivity
import com.example.gpsmapcamera.activities.template.technical.AltitudeAccuracyActivity
import com.example.gpsmapcamera.activities.template.technical.NumberingActivity
import com.example.gpsmapcamera.activities.template.weather.WeatherModuleActivity
import com.example.gpsmapcamera.adapters.TemplateEditAdapter
import com.example.gpsmapcamera.databinding.FragmentBasicBinding
import com.example.gpsmapcamera.models.StampItemName
import com.example.gpsmapcamera.models.TemplateModificationItem
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager
import com.example.gpsmapcamera.utils.StampPreferences
import com.example.gpsmapcamera.utils.launchActivity
import com.example.gpsmapcamera.utils.showToast


class BasicFragment : Fragment() {

    private val binding by lazy {
        FragmentBasicBinding.inflate(layoutInflater)
    }

    private val intentMap: Map<StampItemName, (Context) -> Intent> by lazy {
        mapOf(

            // Date & Time
            StampItemName.DATE_TIME to { ctx ->
                Intent(ctx, DateFormatActivity::class.java)
                    .putExtra(Constants.FROM_TIME_ZONE, false)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },
            StampItemName.TIME_ZONE to { ctx ->
                Intent(ctx, DateFormatActivity::class.java)
                    .putExtra(Constants.FROM_TIME_ZONE, true)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },

            // Map & Stamp Position
            StampItemName.MAP_POSITION to { ctx ->
                Intent(ctx, MapPositionActivity::class.java)
                    .putExtra(Constants.FROM_STAMP_POSITION, false)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },
            StampItemName.STAMP_POSITION to { ctx ->
                Intent(ctx, MapPositionActivity::class.java)
                    .putExtra(Constants.FROM_STAMP_POSITION, true)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },

            // Fonts & Size
            StampItemName.STAMP_FONT to { ctx ->
                Intent(ctx, StampFontActivity::class.java)
                    .putExtra(Constants.FROM_STAMP_SIZE, false)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },
            StampItemName.STAMP_SIZE to { ctx ->
                Intent(ctx, StampFontActivity::class.java)
                    .putExtra(Constants.FROM_STAMP_SIZE, true)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },

            // Reporting
            StampItemName.REPORTING_TAG to { ctx ->
                Intent(ctx, ReportingTagActivity::class.java)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },

            // Map related
            StampItemName.MAP_TYPE to { ctx ->
                Intent(ctx, MapTypeActivity::class.java)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },
            StampItemName.MAP_SCALE to { ctx ->
                Intent(ctx, MapScaleActivity::class.java)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },

            // Weather
            StampItemName.WEATHER to { ctx ->
                Intent(ctx, WeatherModuleActivity::class.java)
                    .putExtra(Constants.FROM_WEATHER_MODULE, Constants.FROM_TEMPERATURE_MODULE)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },
            StampItemName.WIND to { ctx ->
                Intent(ctx, WeatherModuleActivity::class.java)
                    .putExtra(Constants.FROM_WEATHER_MODULE, Constants.FROM_WIND_MODULE)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },
            StampItemName.PRESSURE to { ctx ->
                Intent(ctx, WeatherModuleActivity::class.java)
                    .putExtra(Constants.FROM_WEATHER_MODULE, Constants.FROM_PRESSURE_MODULE)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },

            // Altitude & Accuracy
            StampItemName.ALTITUDE to { ctx ->
                Intent(ctx, AltitudeAccuracyActivity::class.java)
                    .putExtra(Constants.FROM_ALTITUDE, true)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },
            StampItemName.ACCURACY to { ctx ->
                Intent(ctx, AltitudeAccuracyActivity::class.java)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },

            // Addresses
            StampItemName.FULL_ADDRESS to { ctx ->
                Intent(ctx, FullAddressActivity::class.java)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },

            // Miscellaneous
            StampItemName.NUMBERING to { ctx ->
                Intent(ctx, NumberingActivity::class.java)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },
            StampItemName.NOTE to { ctx ->
                Intent(ctx, AddNoteActivity::class.java)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },
            StampItemName.CONTACT_NO to { ctx ->
                Intent(ctx, ContactNumberActivity::class.java)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },
            StampItemName.LAT_LONG to { ctx ->
                Intent(ctx, CoordinateLatLongActivity::class.java)
                    .putExtra(Constants.FROM_PLUS_CODE, false)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },
            StampItemName.PLUS_CODE to { ctx ->
                Intent(ctx, CoordinateLatLongActivity::class.java)
                    .putExtra(Constants.FROM_PLUS_CODE, true)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },
            StampItemName.PERSON_NAME to { ctx ->
                Intent(ctx, AddPersonActivity::class.java)
                    .putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
            },
        )
    }


    private var passedTemplate = ""

    private val appViewModel by lazy {
        (requireActivity().applicationContext as MyApp).appViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentType = arguments?.getString("fragmentType") ?: "Basic"
        passedTemplate = (requireActivity() as EditTemplateActivity).passedTemplate

        setUpRV(fragmentType)
    }

    private fun setUpRV(fragmentType: String) {
        val prefs = StampPreferences(requireActivity())
        val savedList = prefs.getList(passedTemplate)
        val items = mutableListOf<TemplateModificationItem>()

        Log.d("TAG", "setUpRV: $savedList")
        fun isChecked(itemName: StampItemName): Boolean {
            return savedList.find { it.name == itemName }?.visibility ?: false
        }

        when (fragmentType) {
            "Stamp" -> {
                items.add(TemplateModificationItem.Header(getString(R.string.stamp_settings)))
                items.add(TemplateModificationItem.Option(getString(R.string.font_style)))
                items.add(TemplateModificationItem.Option(getString(R.string.stamp_size)))
                items.add(TemplateModificationItem.Option(getString(R.string.stamp_position)))
                items.add(TemplateModificationItem.Option(getString(R.string.map_position)))
                items.add(TemplateModificationItem.Option(getString(R.string.map_scale)))
            }

            "Weather" -> {
                items.add(TemplateModificationItem.Header(getString(R.string.weather_information)))
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.weather),
                        isChecked(StampItemName.WEATHER)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.wind),
                        isChecked(StampItemName.WIND)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.humidity),
                        isChecked(StampItemName.HUMIDITY),
                        false
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.pressure),
                        isChecked(StampItemName.PRESSURE)
                    )
                )
            }

            "Technical" -> {
                items.add(TemplateModificationItem.Header(getString(R.string.technical_details)))
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.compass),
                        isChecked(StampItemName.COMPASS),
                        false
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.magnetic_field),
                        isChecked(StampItemName.MAGNETIC_FIELD),
                        false
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.altitude),
                        isChecked(StampItemName.ALTITUDE)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.accuracy),
                        isChecked(StampItemName.ACCURACY)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.sound_level),
                        isChecked(StampItemName.SOUND_LEVEL),
                        false
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.numbering),
                        isChecked(StampItemName.NUMBERING)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.logo), isChecked(StampItemName.LOGO),
                        false
                    )
                )
            }

            else -> {
                items.add(TemplateModificationItem.Header(getString(R.string.basic_information)))
                if (passedTemplate == Constants.REPORTING_TEMPLATE
                ) {
                    items.add(
                        TemplateModificationItem.Option(
                            getString(R.string.reporting_tag),
                            isChecked(StampItemName.REPORTING_TAG)
                        )
                    )
                }
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.date_time),
                        isChecked(StampItemName.DATE_TIME)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.map_type),
                        isChecked(StampItemName.MAP_TYPE)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.short_address),
                        isChecked(StampItemName.SHORT_ADDRESS),
                        false
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.full_address),
                        isChecked(StampItemName.FULL_ADDRESS)
                    )
                )
                items.add(TemplateModificationItem.Header(getString(R.string.additional_details)))
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.lat_long),
                        isChecked(StampItemName.LAT_LONG)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.plus_code),
                        isChecked(StampItemName.PLUS_CODE)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.time_zone),
                        isChecked(StampItemName.TIME_ZONE)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.person_name),
                        isChecked(StampItemName.PERSON_NAME)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.contact_number),
                        isChecked(StampItemName.CONTACT_NO)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.note),
                        isChecked(StampItemName.NOTE)
                    )
                )
            }
        }

        val adapter = TemplateEditAdapter(
            items,
            onCheckedChange = { position, isChecked ->
                (items[position] as? TemplateModificationItem.Option)?.isChecked = isChecked

                val optionTitle = (items[position] as TemplateModificationItem.Option).title
                val itemName = mapTitleToStampItemName(optionTitle)

                if (itemName == StampItemName.CONTACT_NO) {
                    if (PrefManager.getString(requireActivity(), Constants.ADDED_PHONE_NUMBER)
                            .isEmpty()
                    ) {
                        requireActivity().showToast(getString(R.string.please_add_number_first))
                    } else {
                        appViewModel.updateStampVisibility(passedTemplate, itemName, isChecked)
                    }
                } else {
                    appViewModel.updateStampVisibility(passedTemplate, itemName, isChecked)

                }


            },
            onItemClick = { item, position ->

                val optionTitle = (items[position] as TemplateModificationItem.Option).title
                val itemName = mapTitleToStampItemName(optionTitle)

                if (!item.isShowArrow) return@TemplateEditAdapter

                val launcher = intentMap[itemName]
                if (launcher != null) {
                    (requireActivity() as? EditTemplateActivity)?.launchOtherActivity(requireContext(), launcher(requireContext()))
                } else {
                    Toast.makeText(requireContext(), "Clicked: ${items[position]}", Toast.LENGTH_SHORT).show()
                }
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun mapTitleToStampItemName(title: String): StampItemName {
        return when (title) {
            getString(R.string.reporting_tag) -> StampItemName.REPORTING_TAG
            getString(R.string.date_time) -> StampItemName.DATE_TIME
            getString(R.string.map_type) -> StampItemName.MAP_TYPE
            getString(R.string.short_address) -> StampItemName.SHORT_ADDRESS
            getString(R.string.full_address) -> StampItemName.FULL_ADDRESS
            getString(R.string.lat_long) -> StampItemName.LAT_LONG
            getString(R.string.plus_code) -> StampItemName.PLUS_CODE
            getString(R.string.time_zone) -> StampItemName.TIME_ZONE
            getString(R.string.person_name) -> StampItemName.PERSON_NAME
            getString(R.string.contact_number) -> StampItemName.CONTACT_NO
            getString(R.string.weather) -> StampItemName.WEATHER
            getString(R.string.wind) -> StampItemName.WIND
            getString(R.string.humidity) -> StampItemName.HUMIDITY
            getString(R.string.pressure) -> StampItemName.PRESSURE
            getString(R.string.compass) -> StampItemName.COMPASS
            getString(R.string.magnetic_field) -> StampItemName.MAGNETIC_FIELD
            getString(R.string.altitude) -> StampItemName.ALTITUDE
            getString(R.string.accuracy) -> StampItemName.ACCURACY
            getString(R.string.sound_level) -> StampItemName.SOUND_LEVEL
            getString(R.string.numbering) -> StampItemName.NUMBERING
            getString(R.string.logo) -> StampItemName.LOGO
            getString(R.string.note) -> StampItemName.NOTE
            getString(R.string.map_scale) -> StampItemName.MAP_SCALE
            getString(R.string.font_style) -> StampItemName.STAMP_FONT
            getString(R.string.stamp_position) -> StampItemName.STAMP_POSITION
            getString(R.string.map_position) -> StampItemName.MAP_POSITION
            getString(R.string.stamp_size) -> StampItemName.STAMP_SIZE
            getString(R.string.map_position) -> StampItemName.MAP_POSITION
            else -> StampItemName.MAP_POSITION
        }
    }


/*
    private fun setUpRV(fragmentType: String) {
        val prefs = StampPreferences(requireActivity())
        val savedList = prefs.getList(passedTemplate)
        val items = mutableListOf<TemplateModificationItem>()

        Log.d("TAG", "setUpRV: $savedList")
        fun isChecked(itemName: StampItemName): Boolean {
            return savedList.find { it.name == itemName }?.visibility ?: false
        }

        when (fragmentType) {
            "Stamp" -> {
                items.add(TemplateModificationItem.Header(getString(R.string.stamp_settings)))
                items.add(TemplateModificationItem.Option(getString(R.string.font_style)))
                items.add(TemplateModificationItem.Option(getString(R.string.stamp_size)))
                items.add(TemplateModificationItem.Option(getString(R.string.stamp_position)))
                items.add(TemplateModificationItem.Option(getString(R.string.map_position)))
                items.add(TemplateModificationItem.Option(getString(R.string.map_scale)))
            }

            "Weather" -> {
                items.add(TemplateModificationItem.Header(getString(R.string.weather_information)))
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.weather),
                        isChecked(StampItemName.WEATHER)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.wind),
                        isChecked(StampItemName.WIND)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.humidity),
                        isChecked(StampItemName.HUMIDITY),
                        false
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.pressure),
                        isChecked(StampItemName.PRESSURE)
                    )
                )
            }

            "Technical" -> {
                items.add(TemplateModificationItem.Header(getString(R.string.technical_details)))
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.compass),
                        isChecked(StampItemName.COMPASS),
                        false
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.magnetic_field),
                        isChecked(StampItemName.MAGNETIC_FIELD),
                        false
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.altitude),
                        isChecked(StampItemName.ALTITUDE)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.accuracy),
                        isChecked(StampItemName.ACCURACY)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.sound_level),
                        isChecked(StampItemName.SOUND_LEVEL),
                        false
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.numbering),
                        isChecked(StampItemName.NUMBERING)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.logo), isChecked(StampItemName.LOGO),
                        false
                    )
                )
            }

            else -> {
                items.add(TemplateModificationItem.Header(getString(R.string.basic_information)))
                if (passedTemplate == Constants.REPORTING_TEMPLATE
                ) {
                    items.add(
                        TemplateModificationItem.Option(
                            getString(R.string.reporting_tag),
                            isChecked(StampItemName.REPORTING_TAG)
                        )
                    )
                }
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.date_time),
                        isChecked(StampItemName.DATE_TIME)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.map_type),
                        isChecked(StampItemName.MAP_TYPE)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.short_address),
                        isChecked(StampItemName.SHORT_ADDRESS),
                        false
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.full_address),
                        isChecked(StampItemName.FULL_ADDRESS)
                    )
                )
                items.add(TemplateModificationItem.Header(getString(R.string.additional_details)))
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.lat_long),
                        isChecked(StampItemName.LAT_LONG)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.plus_code),
                        isChecked(StampItemName.PLUS_CODE)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.time_zone),
                        isChecked(StampItemName.TIME_ZONE)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.person_name),
                        isChecked(StampItemName.PERSON_NAME)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.contact_number),
                        isChecked(StampItemName.CONTACT_NO)
                    )
                )
                items.add(
                    TemplateModificationItem.Option(
                        getString(R.string.note),
                        isChecked(StampItemName.NOTE)
                    )
                )
            }
        }

        val adapter = TemplateEditAdapter(
            items,
            onCheckedChange = { position, isChecked ->
                (items[position] as? TemplateModificationItem.Option)?.isChecked = isChecked

                val optionTitle = (items[position] as TemplateModificationItem.Option).title
                val itemName = mapTitleToStampItemName(optionTitle)

                if (itemName == StampItemName.CONTACT_NO) {
                    if (PrefManager.getString(requireActivity(), Constants.ADDED_PHONE_NUMBER)
                            .isEmpty()
                    ) {
                        requireActivity().showToast(getString(R.string.please_add_number_first))
                    } else {
                        appViewModel.updateStampVisibility(passedTemplate, itemName, isChecked)
                    }
                } else {
                    appViewModel.updateStampVisibility(passedTemplate, itemName, isChecked)

                }


            },
            onItemClick = { item, position ->
                val optionTitle = (items[position] as TemplateModificationItem.Option).title
                val itemName = mapTitleToStampItemName(optionTitle)

                if (item.isShowArrow) {

                    if (itemName == StampItemName.DATE_TIME || itemName == StampItemName.TIME_ZONE) {
                        requireActivity().launchActivity<DateFormatActivity> {
                            putExtra(Constants.FROM_TIME_ZONE, itemName == StampItemName.TIME_ZONE)
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
                    } else if (itemName == StampItemName.MAP_POSITION || itemName == StampItemName.STAMP_POSITION) {
                        requireActivity().launchActivity<MapPositionActivity> {
                            putExtra(Constants.FROM_STAMP_POSITION, itemName == StampItemName.STAMP_POSITION)
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
                    } else if (itemName == StampItemName.STAMP_FONT || itemName == StampItemName.STAMP_SIZE) {
                        requireActivity().launchActivity<StampFontActivity> {
                            putExtra(Constants.FROM_STAMP_SIZE, itemName == StampItemName.STAMP_SIZE)
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
                    } else if (itemName == StampItemName.REPORTING_TAG) {
                        requireActivity().launchActivity<ReportingTagActivity>() {
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
                    }else if (itemName == StampItemName.MAP_TYPE) {
                        requireActivity().launchActivity<MapTypeActivity>() {
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
                    }else if (itemName == StampItemName.MAP_SCALE) {
                        requireActivity().launchActivity<MapScaleActivity>() {
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
                    }else if (itemName == StampItemName.WEATHER) {
                        requireActivity().launchActivity<WeatherModuleActivity>() {
                            putExtra(Constants.FROM_WEATHER_MODULE, Constants.FROM_TEMPERATURE_MODULE)
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
                    }else if (itemName == StampItemName.WIND) {
                        requireActivity().launchActivity<WeatherModuleActivity>() {
                            putExtra(Constants.FROM_WEATHER_MODULE, Constants.FROM_WIND_MODULE)
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
                    }else if (itemName == StampItemName.PRESSURE) {
                        requireActivity().launchActivity<WeatherModuleActivity>() {
                            putExtra(Constants.FROM_WEATHER_MODULE, Constants.FROM_PRESSURE_MODULE)
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
                    }else if (itemName == StampItemName.ALTITUDE) {
                        requireActivity().launchActivity<AltitudeAccuracyActivity>() {
                            putExtra(Constants.FROM_ALTITUDE, true)
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
                    }else if (itemName == StampItemName.ACCURACY) {
                        requireActivity().launchActivity<AltitudeAccuracyActivity>() {
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
                    } else if (itemName == StampItemName.FULL_ADDRESS) {
                        requireActivity().launchActivity<FullAddressActivity>() {
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
                    } else if (itemName == StampItemName.NUMBERING) {
                        requireActivity().launchActivity<NumberingActivity>() {
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
                    } else if (itemName == StampItemName.NOTE) {
                        requireActivity().launchActivity<AddNoteActivity>() {
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
                    } else if (itemName == StampItemName.CONTACT_NO) {
//                        if (PrefManager.getString(requireActivity(), Constants.ADDED_PHONE_NUMBER).isEmpty()){
//                            requireActivity().showToast(getString(R.string.please_add_number_first))
//                        }else {
                        requireActivity().launchActivity<ContactNumberActivity>() {
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
//                        }
                    } else if (itemName == StampItemName.LAT_LONG || itemName == StampItemName.PLUS_CODE) {
                        requireActivity().launchActivity<CoordinateLatLongActivity>() {
                            putExtra(Constants.FROM_PLUS_CODE, itemName == StampItemName.PLUS_CODE)
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
                    } else if (itemName == StampItemName.PERSON_NAME) {
                        requireActivity().launchActivity<AddPersonActivity>() {
                            putExtra(Constants.PASSED_STAMP_TEMPLATE, passedTemplate)
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Clicked: ${items[position]}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }
    */
}