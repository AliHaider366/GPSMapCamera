package com.example.gpsmapcamera.activities.template.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.template.AllTemplateActivity
import com.example.gpsmapcamera.adapters.TopTemplatesAdapter
import com.example.gpsmapcamera.databinding.FragmentTopTemplateBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.EventConstants
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager
import com.example.gpsmapcamera.utils.enableView
import com.example.gpsmapcamera.utils.invisible
import com.example.gpsmapcamera.utils.observeOnce


class TopTemplateFragment : Fragment() {

    private val binding by lazy {
        FragmentTopTemplateBinding.inflate(layoutInflater)
    }


    private val appViewModel by lazy {
        (requireActivity().applicationContext as MyApp).appViewModel
    }

    private val topTemplateAdapter by lazy {
        TopTemplatesAdapter(getTopTemplates()) { position ->
            (requireActivity() as AllTemplateActivity).isTemplateChanged = 1
            (requireActivity() as AllTemplateActivity).isTopSelected = true
            (requireActivity() as AllTemplateActivity).topTemplateSelectedPosition = position
            (requireActivity() as AllTemplateActivity).binding.btnDone.enableView()

            (requireActivity() as AllTemplateActivity).firebaseLogger.logEvent(
                activityName = EventConstants.TEMPLATE_SCREEN,
                eventName =  EventConstants.EVENT_TEMPLATE,
                parameters = mapOf(EventConstants.PARAM_SELECTED to "Advanced_Template_${position+1}")
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if ((requireActivity() as AllTemplateActivity).isTemplateChanged == -1) {
            (requireActivity() as AllTemplateActivity).isTemplateChanged = 0
            topTemplateAdapter.selectedPosition = -1
            topTemplateAdapter.notifyDataSetChanged()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRV()

        // Observe dynamic values (shared across all templates)
        appViewModel.dynamicValues.observeOnce(requireActivity()) { newDynamics ->
            topTemplateAdapter.updateDynamics(newDynamics)
        }

        if (PrefManager.getBoolean(requireActivity(), Constants.IS_TOP_TEMPLATE_SELECTED, false)){
            val position = PrefManager.getInt(requireActivity(), Constants.TOP_TEMPLATE_SELECTED_NUMBER, -1)
            topTemplateAdapter.selectedPosition = position
            topTemplateAdapter.notifyDataSetChanged()
        }

    }

    private fun setupRV() {
        binding.recyclerTopTemplates.layoutManager = GridLayoutManager(requireActivity(), 2)
        binding.recyclerTopTemplates.adapter = topTemplateAdapter
    }


    private fun getTopTemplates(): List<Int> {
        // Return layout IDs of the six XMLs you already added
        return listOf(
            R.layout.stamp_top_template_1_preview,
            R.layout.stamp_top_template_2_preview,
            R.layout.stamp_top_template_3_preview,
            R.layout.stamp_top_template_4_preview,
            R.layout.stamp_top_template_5_preview,
            R.layout.stamp_top_template_6_preview
        )
    }

}