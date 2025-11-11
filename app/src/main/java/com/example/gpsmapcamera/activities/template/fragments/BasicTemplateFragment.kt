package com.example.gpsmapcamera.activities.template.fragments

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.template.AllTemplateActivity
import com.example.gpsmapcamera.activities.template.EditTemplateActivity
import com.example.gpsmapcamera.adapters.StampAdapter
import com.example.gpsmapcamera.adapters.StampCenterAdapter
import com.example.gpsmapcamera.databinding.ActivityAllTemplateBinding
import com.example.gpsmapcamera.databinding.FragmentBasicTemplateBinding
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

class BasicTemplateFragment : Fragment() {


    private val binding by lazy {
        FragmentBasicTemplateBinding.inflate(layoutInflater)
    }

    // Activity Result Launcher
    private val activityLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                setUpTemplate()
            }
        }


    private val appViewModel by lazy {
        (requireActivity().applicationContext as MyApp).appViewModel
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if ((requireActivity() as AllTemplateActivity).isTemplateChanged == 1) {
            (requireActivity() as AllTemplateActivity).isTemplateChanged = 0
            binding.ivEditClassicTemplate.invisible()
            binding.ivEditAdvanceTemplate.invisible()
            binding.ivEditReportingTemplate.invisible()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
        setUpTemplate()
        clickListeners()
    }


    private fun initUI() {
        val stamp = PrefManager.getString(
            requireActivity(), Constants.SELECTED_STAMP_TEMPLATE,
            Constants.CLASSIC_TEMPLATE
        )
        if (PrefManager.getBoolean(requireActivity(), Constants.IS_TOP_TEMPLATE_SELECTED, false)) {
            binding.ivEditClassicTemplate.invisible()
            binding.ivEditAdvanceTemplate.invisible()
            binding.ivEditReportingTemplate.invisible()
        } else {
            selectTemplate(stamp)
        }
    }

    private fun clickListeners() = binding.run {


        ivEditClassicTemplate.setOnClickListener {
            val intent = Intent(requireActivity(), EditTemplateActivity::class.java)
            intent.putExtra(Constants.PASSED_STAMP_TEMPLATE, Constants.CLASSIC_TEMPLATE)
            activityLauncher.launch(intent)
        }

        ivEditAdvanceTemplate.setOnClickListener {
            requireActivity().launchActivity<EditTemplateActivity>() {
                putExtra(Constants.PASSED_STAMP_TEMPLATE, Constants.ADVANCE_TEMPLATE)
            }
        }
        ivEditReportingTemplate.setOnClickListener {
            requireActivity().launchActivity<EditTemplateActivity>() {
                putExtra(Constants.PASSED_STAMP_TEMPLATE, Constants.REPORTING_TEMPLATE)
            }
        }

        lytClassicClick.setOnClickListener {
            selectTemplate(Constants.CLASSIC_TEMPLATE)
            PrefManager.setBoolean(requireActivity(), Constants.IS_TOP_TEMPLATE_SELECTED, false)
            (requireActivity() as AllTemplateActivity).isTemplateChanged = -1
        }
        lytAdvanceClick.setOnClickListener {
            selectTemplate(Constants.ADVANCE_TEMPLATE)
            PrefManager.setBoolean(requireActivity(), Constants.IS_TOP_TEMPLATE_SELECTED, false)
            (requireActivity() as AllTemplateActivity).isTemplateChanged = -1
        }
        lytReportingClick.setOnClickListener {
            selectTemplate(Constants.REPORTING_TEMPLATE)
            PrefManager.setBoolean(requireActivity(), Constants.IS_TOP_TEMPLATE_SELECTED, false)
            (requireActivity() as AllTemplateActivity).isTemplateChanged = -1
        }


    }

    private fun selectTemplate(selectedTemplate: String) {
        PrefManager.setString(
            requireActivity(),
            Constants.SELECTED_STAMP_TEMPLATE,
            selectedTemplate
        )
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
        appViewModel.classicStampConfigs.observe(requireActivity()) { allConfigs ->
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
                map.setBackgroundResource(requireActivity().getSelectedMapDrawable(Constants.CLASSIC_TEMPLATE))


                val selectedIndex = PrefManager.getInt(
                    root.context,
                    Constants.SELECTED_STAMP_FONT + Constants.CLASSIC_TEMPLATE,
                    0
                )

                val fontRes = stampFontList.getOrNull(selectedIndex)
                val typeface = if (fontRes != null) {
                    ResourcesCompat.getFont(root.context, fontRes)
                } else {
                    Typeface.DEFAULT // system default font
                }

                tvCenterTitle.typeface = typeface




                requireActivity().setUpMapPositionForClassicTemplate(this@run)

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
        appViewModel.advanceStampConfigs.observe(requireActivity()) { allConfigs ->
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

                map.setBackgroundResource(requireActivity().getSelectedMapDrawable(Constants.ADVANCE_TEMPLATE))


                val selectedIndex = PrefManager.getInt(
                    root.context,
                    Constants.SELECTED_STAMP_FONT + Constants.ADVANCE_TEMPLATE,
                    0
                )

                val fontRes = stampFontList.getOrNull(selectedIndex)
                val typeface = if (fontRes != null) {
                    ResourcesCompat.getFont(root.context, fontRes)
                } else {
                    Typeface.DEFAULT // system default font
                }


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

                requireActivity().setUpMapPositionForAdvancedTemplate(this@run)


            }
        }

        // Observe Reporting template configurations
        appViewModel.reportingStampConfigs.observe(requireActivity()) { allConfigs ->
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

                map.setBackgroundResource(requireActivity().getSelectedMapDrawable(Constants.REPORTING_TEMPLATE))

                val selectedIndex = PrefManager.getInt(
                    root.context,
                    Constants.SELECTED_STAMP_FONT + Constants.REPORTING_TEMPLATE,
                    0
                )

                val fontRes = stampFontList.getOrNull(selectedIndex)
                val typeface = if (fontRes != null) {
                    ResourcesCompat.getFont(root.context, fontRes)
                } else {
                    Typeface.DEFAULT // system default font
                }


                tvCenterTitle.typeface = typeface
                tvEnvironment.typeface = typeface


//                root.scaleX = 1.5f
//                root.scaleY = 1.2f
                // Optional: Set pivot for scaling from center (adjust if needed)
//                root.pivotX = root.width / 2f
//                root.pivotY = root.height / 2f

                requireActivity().setUpMapPositionForReportingTemplate(this@run)

                if (allConfigs.find { it.name == StampItemName.MAP_TYPE }?.visibility == true) {
                    map.visible()
                } else {
                    map.gone()
                }

                if (allConfigs.find { it.name == StampItemName.REPORTING_TAG }?.visibility == true) {
                    tvEnvironment.visible()
                    val itemIndex = PrefManager.getInt(
                        requireActivity(),
                        Constants.SELECTED_REPORTING_TAG, 0
                    )
                    val reportingTagSavedList =
                        StampPreferences(requireActivity()).getWholeList(
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
        appViewModel.dynamicValues.observeOnce(requireActivity()) { newDynamics ->
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


}