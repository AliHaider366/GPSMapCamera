package com.example.gpsmapcamera.activities.template

import android.content.Context
import android.os.Bundle
import android.transition.TransitionManager
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapters.StampAdapter
import com.example.gpsmapcamera.adapters.StampCenterAdapter
import com.example.gpsmapcamera.adapters.ViewPagerAdapter
import com.example.gpsmapcamera.databinding.ActivityEditTemplateBinding
import com.example.gpsmapcamera.databinding.StampAdvanceTemplateLayoutBinding
import com.example.gpsmapcamera.databinding.StampClassicTemplateLayoutBinding
import com.example.gpsmapcamera.databinding.StampReportingTemplateLayoutBinding
import com.example.gpsmapcamera.models.StampConfig
import com.example.gpsmapcamera.models.StampItemName
import com.example.gpsmapcamera.models.StampPosition
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager
import com.example.gpsmapcamera.utils.StampPreferences
import com.example.gpsmapcamera.utils.getFontSizeFactor
import com.example.gpsmapcamera.utils.gone
import com.example.gpsmapcamera.utils.observeOnce
import com.example.gpsmapcamera.utils.reportingTagsDefault
import com.example.gpsmapcamera.utils.setUpMapPositionForAdvancedTemplate
import com.example.gpsmapcamera.utils.setUpMapPositionForClassicTemplate
import com.example.gpsmapcamera.utils.setUpMapPositionForReportingTemplate
import com.example.gpsmapcamera.utils.stampFontList
import com.example.gpsmapcamera.utils.visible
import com.google.android.material.tabs.TabLayoutMediator

class EditTemplateActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityEditTemplateBinding.inflate(layoutInflater)
    }


    private val appViewModel by lazy {
        (applicationContext as MyApp).appViewModel
    }

    val passedTemplate by lazy {
        intent.getStringExtra(Constants.PASSED_STAMP_TEMPLATE) ?: Constants.CLASSIC_TEMPLATE
    }


    private val templateAdapterBottom by lazy { StampAdapter(passedTemplate) }
    private val templateAdapterCenter by lazy {
        StampCenterAdapter(passedTemplate)
    }
    private val templateAdapterRight by lazy { StampAdapter(passedTemplate) }


    // Keep references for your template bindings
    private val classicTemplateBinding by lazy {
        StampClassicTemplateLayoutBinding.inflate(layoutInflater, binding.layoutContainer, true)
    }

    private val advanceTemplateBinding by lazy {
        StampAdvanceTemplateLayoutBinding.inflate(layoutInflater, binding.layoutContainer, true)
    }

    private val reportingTemplateBinding by lazy {
        StampReportingTemplateLayoutBinding.inflate(layoutInflater, binding.layoutContainer, true)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        setUpTemplate()
        setUpViewPager()
    }

    private fun setUpViewPager() {

        // Tab Titles
        val titles = listOf(
            getString(R.string.basic),
            getString(R.string.stamp), getString(R.string.weather), getString(R.string.technical)
        )


        val adapter = ViewPagerAdapter(titles, this)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = titles[position]
        }.attach()
    }

    private fun setUpTemplate() {

        // Observe the appropriate LiveData based on the selected template
        val stampConfigs = when (passedTemplate) {
            Constants.CLASSIC_TEMPLATE -> appViewModel.classicStampConfigs
            Constants.ADVANCE_TEMPLATE -> appViewModel.advanceStampConfigs
            Constants.REPORTING_TEMPLATE -> appViewModel.reportingStampConfigs
            else -> appViewModel.classicStampConfigs // Fallback to classic
        }

        stampConfigs.observe(this) { allConfigs ->


            templateAdapterBottom.submitList(allConfigs.filter { it.position == StampPosition.BOTTOM && it.visibility } as ArrayList)
            templateAdapterCenter.submitList(allConfigs.filter { it.position == StampPosition.CENTER && it.visibility } as ArrayList)
            templateAdapterRight.submitList(allConfigs.filter { it.position == StampPosition.RIGHT && it.visibility } as ArrayList)

            // Inflate selected layout into FrameLayout
            when (passedTemplate) {
                Constants.CLASSIC_TEMPLATE -> {
                    setupClassicUI(allConfigs)
                }

                Constants.ADVANCE_TEMPLATE -> {
                    setupAdvanceUI(allConfigs)
                }

                Constants.REPORTING_TEMPLATE -> {
                    setupReportingUI(allConfigs)
                }
            }


        }

        appViewModel.dynamicValues.observeOnce(this) { newDynamics ->
            templateAdapterBottom.updateDynamics(newDynamics)
            templateAdapterCenter.updateDynamics(newDynamics)
            templateAdapterRight.updateDynamics(newDynamics)

           when (passedTemplate) {
                Constants.CLASSIC_TEMPLATE -> classicTemplateBinding.tvCenterTitle.text = newDynamics.shortAddress
                Constants.ADVANCE_TEMPLATE -> advanceTemplateBinding.tvCenterTitle.text = newDynamics.shortAddress
                Constants.REPORTING_TEMPLATE -> reportingTemplateBinding.tvCenterTitle.text = newDynamics.shortAddress
                else -> classicTemplateBinding.tvCenterTitle.text = newDynamics.shortAddress // Fallback to classic
            }


        }
    }

    private fun setupReportingUI(allConfigs: List<StampConfig>) {

        reportingTemplateBinding.run {
            rvBottom.adapter = templateAdapterBottom
            rvCenter.adapter = templateAdapterCenter
            rvRight.adapter = templateAdapterRight

            val typeface = ResourcesCompat.getFont(
                root.context, stampFontList[PrefManager.getInt(
                    root.context,
                    Constants.SELECTED_STAMP_FONT + passedTemplate,
                    0
                )]
            )
            tvCenterTitle.typeface = typeface
            tvEnvironment.typeface = typeface

            setUpMapPositionForReportingTemplate(this@run)

            val getScaleValue = root.context.getFontSizeFactor(passedTemplate)
            val baseTextSize =
                root.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._13sdp).toFloat()
            tvCenterTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSize * getScaleValue)

            val baseTextSizeFortvEnv =
                root.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._9sdp).toFloat()
            tvEnvironment.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSizeFortvEnv * getScaleValue)

        }


        if (allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == true) {
            reportingTemplateBinding.map.visible()
        } else {
            reportingTemplateBinding.map.gone()
        }



        if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == true) {
            reportingTemplateBinding.tvEnvironment.visible()
            val itemIndex = PrefManager.getInt(
                this@EditTemplateActivity,
                Constants.SELECTED_REPORTING_TAG, 0
            )
            val reportingTagSavedList = StampPreferences(this@EditTemplateActivity).getWholeList(
                Constants.KEY_REPORTING_TAG
            )
            if (reportingTagSavedList.isEmpty()) {
                if (itemIndex < reportingTagsDefault.size) {
                    reportingTemplateBinding.tvEnvironment.text = reportingTagsDefault[itemIndex]
                }
            } else {
                if (itemIndex < reportingTagSavedList.size) {
                    reportingTemplateBinding.tvEnvironment.text = reportingTagSavedList[itemIndex]
                }
            }
        } else {
            reportingTemplateBinding.tvEnvironment.gone()
        }

        if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == false &&
            allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == false
        ) {
            reportingTemplateBinding.mapContainer.gone()
        } else {
            reportingTemplateBinding.mapContainer.visible()
        }



        if (allConfigs.find { it.name == StampItemName.LOGO }?.visibility == true) {
            reportingTemplateBinding.ivLogo.visible()
        } else {
            reportingTemplateBinding.ivLogo.gone()
        }

    }

    private fun setupAdvanceUI(allConfigs: List<StampConfig>) {

        advanceTemplateBinding.run {
            rvBottom.adapter = templateAdapterBottom
            rvCenter.adapter = templateAdapterCenter
            rvRight.adapter = templateAdapterRight

            val typeface = ResourcesCompat.getFont(
                root.context, stampFontList[PrefManager.getInt(
                    root.context,
                    Constants.SELECTED_STAMP_FONT + passedTemplate,
                    0
                )]
            )
            tvCenterTitle.typeface = typeface

            setUpMapPositionForAdvancedTemplate(this@run)
            val getScaleValue = root.context.getFontSizeFactor(passedTemplate)
            val baseTextSize =
                root.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._13sdp).toFloat()
            tvCenterTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSize * getScaleValue)

        }


        if (allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == true) {
            advanceTemplateBinding.mapContainer.visible()
        } else {
            advanceTemplateBinding.mapContainer.gone()
        }



        if (allConfigs.find { it.name == StampItemName.LOGO }?.visibility == true) {
            advanceTemplateBinding.ivLogo.visible()
        } else {
            advanceTemplateBinding.ivLogo.gone()
        }

    }


    private fun setupClassicUI(allConfigs: List<StampConfig>) {


        classicTemplateBinding.run {
            rvBottom.adapter = templateAdapterBottom
            rvCenter.adapter = templateAdapterCenter
            rvRight.adapter = templateAdapterRight

            val typeface = ResourcesCompat.getFont(
                root.context, stampFontList[PrefManager.getInt(
                    root.context,
                    Constants.SELECTED_STAMP_FONT + passedTemplate,
                    0
                )]
            )
            tvCenterTitle.typeface = typeface

            setUpMapPositionForClassicTemplate(this@run)
            val getScaleValue = root.context.getFontSizeFactor(passedTemplate)
            val baseTextSize =
                root.context.resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._13sdp).toFloat()
            tvCenterTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, baseTextSize * getScaleValue)

        }


        if (allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == true) {
            classicTemplateBinding.mapContainer.visible()
        } else {
            classicTemplateBinding.mapContainer.gone()
        }


        if (allConfigs.find { it.name == StampItemName.LOGO }?.visibility == true) {
            classicTemplateBinding.ivLogo.visible()
        } else {
            classicTemplateBinding.ivLogo.gone()
        }

    }
}

