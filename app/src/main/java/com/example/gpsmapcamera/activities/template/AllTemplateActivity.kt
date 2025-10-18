package com.example.gpsmapcamera.activities.template

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.transition.TransitionManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.res.ResourcesCompat
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.BaseActivity
import com.example.gpsmapcamera.adapters.StampAdapter
import com.example.gpsmapcamera.adapters.StampCenterAdapter
import com.example.gpsmapcamera.databinding.ActivityAllTemplateBinding
import com.example.gpsmapcamera.databinding.StampAdvanceTemplateLayoutBinding
import com.example.gpsmapcamera.databinding.StampClassicTemplateLayoutBinding
import com.example.gpsmapcamera.databinding.StampReportingTemplateLayoutBinding
import com.example.gpsmapcamera.models.StampItemName
import com.example.gpsmapcamera.models.StampPosition
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager
import com.example.gpsmapcamera.utils.StampPreferences
import com.example.gpsmapcamera.utils.getSelectedMapDrawable
import com.example.gpsmapcamera.utils.gone
import com.example.gpsmapcamera.utils.invisible
import com.example.gpsmapcamera.utils.launchActivity
import com.example.gpsmapcamera.utils.observeOnce
import com.example.gpsmapcamera.utils.reportingTagsDefault
import com.example.gpsmapcamera.utils.setUpMapPositionForAdvancedTemplate
import com.example.gpsmapcamera.utils.setUpMapPositionForClassicTemplate
import com.example.gpsmapcamera.utils.setUpMapPositionForReportingTemplate
import com.example.gpsmapcamera.utils.stampFontList
import com.example.gpsmapcamera.utils.visible


class AllTemplateActivity : BaseActivity() {

    private val binding by lazy {
        ActivityAllTemplateBinding.inflate(layoutInflater)
    }

    // Activity Result Launcher
    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setUpTemplate()
            }
        }


    private val appViewModel by lazy {
        (applicationContext as MyApp).appViewModel
    }

    // Separate adapters for each template
    private val classicAdapterBottom = StampAdapter(Constants.CLASSIC_TEMPLATE)
    private val classicAdapterCenter = StampCenterAdapter(Constants.CLASSIC_TEMPLATE)
    private val classicAdapterRight = StampAdapter(Constants.CLASSIC_TEMPLATE)

    private val advanceAdapterBottom = StampAdapter(Constants.ADVANCE_TEMPLATE)
    private val advanceAdapterCenter = StampCenterAdapter(Constants.ADVANCE_TEMPLATE)
    private val advanceAdapterRight = StampAdapter(Constants.ADVANCE_TEMPLATE)

    private val reportingAdapterBottom = StampAdapter(Constants.REPORTING_TEMPLATE)
    private val reportingAdapterCenter = StampCenterAdapter(Constants.REPORTING_TEMPLATE)
    private val reportingAdapterRight = StampAdapter(Constants.REPORTING_TEMPLATE)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        initUI()
        setUpTemplate()
        clickListeners()
    }

    private fun initUI() {
        val stamp = PrefManager.getString(
            this@AllTemplateActivity, Constants.SELECTED_STAMP_TEMPLATE,
            Constants.CLASSIC_TEMPLATE
        )
        selectTemplate(stamp)
    }

    private fun clickListeners() = binding.run {

        backBtn.setOnClickListener {
            backPressedCallback.handleOnBackPressed()
        }

        ivEditClassicTemplate.setOnClickListener {
            val intent = Intent(this@AllTemplateActivity, EditTemplateActivity::class.java)
            intent.putExtra(Constants.PASSED_STAMP_TEMPLATE, Constants.CLASSIC_TEMPLATE)
            activityLauncher.launch(intent)
        }

        ivEditAdvanceTemplate.setOnClickListener {
            launchActivity<EditTemplateActivity>() {
                putExtra(Constants.PASSED_STAMP_TEMPLATE, Constants.ADVANCE_TEMPLATE)
            }
        }
        ivEditReportingTemplate.setOnClickListener {
            launchActivity<EditTemplateActivity>() {
                putExtra(Constants.PASSED_STAMP_TEMPLATE, Constants.REPORTING_TEMPLATE)
            }
        }

        lytClassicClick.setOnClickListener {
            selectTemplate(Constants.CLASSIC_TEMPLATE)
        }
        lytAdvanceClick.setOnClickListener {
            selectTemplate(Constants.ADVANCE_TEMPLATE)
        }
        lytReportingClick.setOnClickListener {
            selectTemplate(Constants.REPORTING_TEMPLATE)
        }


    }

    private fun selectTemplate(selectedTemplate: String) {
        PrefManager.setString(this, Constants.SELECTED_STAMP_TEMPLATE, selectedTemplate)
        when (selectedTemplate) {
            Constants.CLASSIC_TEMPLATE -> {
                binding.ivEditClassicTemplate.visible()
                binding.ivEditAdvanceTemplate.invisible()
                binding.ivEditReportingTemplate.invisible()
            }

            Constants.ADVANCE_TEMPLATE -> {
                binding.ivEditClassicTemplate.invisible()
                binding.ivEditAdvanceTemplate.visible()
                binding.ivEditReportingTemplate.invisible()
            }

            Constants.REPORTING_TEMPLATE -> {
                binding.ivEditClassicTemplate.invisible()
                binding.ivEditAdvanceTemplate.invisible()
                binding.ivEditReportingTemplate.visible()
            }
        }
    }

    private fun setUpTemplate() {
        // Observe Classic template configurations
        appViewModel.classicStampConfigs.observe(this) { allConfigs ->
            val bottomItems =
                allConfigs.filter { it.position == StampPosition.BOTTOM && it.visibility }
            val centerItems =
                allConfigs.filter { it.position == StampPosition.CENTER && it.visibility }
            val rightItems =
                allConfigs.filter { it.position == StampPosition.RIGHT && it.visibility }

            classicAdapterBottom.submitList(bottomItems as ArrayList)
            classicAdapterCenter.submitList(centerItems as ArrayList)
            classicAdapterRight.submitList(rightItems as ArrayList)

            binding.lytClassicTemplate.run {
                rvBottom.adapter = classicAdapterBottom
                rvCenter.adapter = classicAdapterCenter
                rvRight.adapter = classicAdapterRight
                map.setBackgroundResource(getSelectedMapDrawable(Constants.CLASSIC_TEMPLATE))

                val typeface = ResourcesCompat.getFont(
                    root.context, stampFontList[PrefManager.getInt(
                        root.context,
                        Constants.SELECTED_STAMP_FONT + Constants.CLASSIC_TEMPLATE,
                        0
                    )]
                )
                tvCenterTitle.typeface = typeface



                setUpMapPositionForClassicTemplate(this@run)

                if (allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == true) {
                    map.visible()
                } else {
                    map.gone()
                }

//                if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == true) {
//                    tvEnvironment.visible()
//                } else {
//                    tvEnvironment.gone()
//                }

                if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == false &&
                    allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == false
                ) {
                    mapContainer.gone()
                } else {
                    mapContainer.visible()
                }


                if (allConfigs.find { it.name == StampItemName.LOGO }?.visibility == true) {
                    ivLogo.visible()
                } else {
                    ivLogo.gone()
                }

            }
        }

        // Observe Advance template configurations
        appViewModel.advanceStampConfigs.observe(this) { allConfigs ->
            val bottomItems =
                allConfigs.filter { it.position == StampPosition.BOTTOM && it.visibility }
            val centerItems =
                allConfigs.filter { it.position == StampPosition.CENTER && it.visibility }
            val rightItems =
                allConfigs.filter { it.position == StampPosition.RIGHT && it.visibility }

            advanceAdapterBottom.submitList(bottomItems as ArrayList)
            advanceAdapterCenter.submitList(centerItems as ArrayList)
            advanceAdapterRight.submitList(rightItems as ArrayList)

            binding.lytAdvanceTemplate.run {
                rvBottom.adapter = advanceAdapterBottom
                rvCenter.adapter = advanceAdapterCenter
                rvRight.adapter = advanceAdapterRight

                map.setBackgroundResource(getSelectedMapDrawable(Constants.ADVANCE_TEMPLATE))

                val typeface = ResourcesCompat.getFont(
                    root.context, stampFontList[PrefManager.getInt(
                        root.context,
                        Constants.SELECTED_STAMP_FONT + Constants.ADVANCE_TEMPLATE,
                        0
                    )]
                )
                tvCenterTitle.typeface = typeface



                if (allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == true) {
                    map.visible()
                } else {
                    map.gone()
                }

//                if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == true) {
//                    tvEnvironment.visible()
//                } else {
//                    tvEnvironment.gone()
//                }

                if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == false &&
                    allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == false
                ) {
                    mapContainer.gone()
                } else {
                    mapContainer.visible()
                }



                if (allConfigs.find { it.name == StampItemName.LOGO }?.visibility == true) {
                    ivLogo.visible()
                } else {
                    ivLogo.gone()
                }

                setUpMapPositionForAdvancedTemplate(this@run)


            }
        }

        // Observe Reporting template configurations
        appViewModel.reportingStampConfigs.observe(this) { allConfigs ->
            val bottomItems =
                allConfigs.filter { it.position == StampPosition.BOTTOM && it.visibility }
            val centerItems =
                allConfigs.filter { it.position == StampPosition.CENTER && it.visibility }
            val rightItems =
                allConfigs.filter { it.position == StampPosition.RIGHT && it.visibility }

            reportingAdapterBottom.submitList(bottomItems as ArrayList)
            reportingAdapterCenter.submitList(centerItems as ArrayList)
            reportingAdapterRight.submitList(rightItems as ArrayList)

            binding.lytReportingTemplate.run {
                rvBottom.adapter = reportingAdapterBottom
                rvCenter.adapter = reportingAdapterCenter
                rvRight.adapter = reportingAdapterRight

                map.setBackgroundResource(getSelectedMapDrawable(Constants.REPORTING_TEMPLATE))

                val typeface = ResourcesCompat.getFont(
                    root.context, stampFontList[PrefManager.getInt(
                        root.context,
                        Constants.SELECTED_STAMP_FONT + Constants.REPORTING_TEMPLATE,
                        0
                    )]
                )
                tvCenterTitle.typeface = typeface
                tvEnvironment.typeface = typeface


//                root.scaleX = 1.5f
//                root.scaleY = 1.2f
                // Optional: Set pivot for scaling from center (adjust if needed)
//                root.pivotX = root.width / 2f
//                root.pivotY = root.height / 2f

                setUpMapPositionForReportingTemplate(this@run)

                if (allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == true) {
                    map.visible()
                } else {
                    map.gone()
                }

                if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == true) {
                    tvEnvironment.visible()
                    val itemIndex = PrefManager.getInt(
                        this@AllTemplateActivity,
                        Constants.SELECTED_REPORTING_TAG, 0
                    )
                    val reportingTagSavedList =
                        StampPreferences(this@AllTemplateActivity).getWholeList(
                            Constants.KEY_REPORTING_TAG
                        )
                    if (reportingTagSavedList.isEmpty()) {
                        if (itemIndex < reportingTagsDefault.size) {
                            tvEnvironment.text = reportingTagsDefault[itemIndex]
                        }
                    } else {
                        if (itemIndex < reportingTagSavedList.size) {
                            tvEnvironment.text = reportingTagSavedList[itemIndex]
                        }
                    }
                } else {
                    tvEnvironment.gone()
                }

                if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == false &&
                    allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == false
                ) {
                    mapContainer.gone()
                } else {
                    mapContainer.visible()
                }


                if (allConfigs.find { it.name == StampItemName.LOGO }?.visibility == true) {
                    ivLogo.visible()
                } else {
                    ivLogo.gone()
                }
            }
        }

        // Observe dynamic values (shared across all templates)
        appViewModel.dynamicValues.observeOnce(this) { newDynamics ->
            classicAdapterBottom.updateDynamics(newDynamics)
            classicAdapterCenter.updateDynamics(newDynamics)
            classicAdapterRight.updateDynamics(newDynamics)

            advanceAdapterBottom.updateDynamics(newDynamics)
            advanceAdapterCenter.updateDynamics(newDynamics)
            advanceAdapterRight.updateDynamics(newDynamics)

            reportingAdapterBottom.updateDynamics(newDynamics)
            reportingAdapterCenter.updateDynamics(newDynamics)
            reportingAdapterRight.updateDynamics(newDynamics)

            binding.lytReportingTemplate.tvCenterTitle.text = newDynamics.shortAddress
            binding.lytAdvanceTemplate.tvCenterTitle.text = newDynamics.shortAddress
            binding.lytClassicTemplate.tvCenterTitle.text = newDynamics.shortAddress
        }
    }


    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            setResult(RESULT_OK)
            finish()
        }
    }


}