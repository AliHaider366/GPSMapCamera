package com.example.gpsmapcamera.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.gpsmapcamera.databinding.FragmentStartMenu3Binding
import com.example.gpsmapcamera.utils.setSelectionState


class StartMenuFragment3 : Fragment() {

    private val binding by lazy {
        FragmentStartMenu3Binding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        init()
    }

    private fun init()=binding.apply {

        /* by default selection*/
        gpsView.setSelectionState(  selectedCheckBox = checkboxGPS,
            manualEntryView to checkboxManualEntry,
            donotTagView to checkboxDonotTag)

        gpsView.setOnClickListener{
            gpsView.setSelectionState(  selectedCheckBox = checkboxGPS,
                manualEntryView to checkboxManualEntry,
                donotTagView to checkboxDonotTag)
        }

        manualEntryView.setOnClickListener{
            manualEntryView.setSelectionState(  selectedCheckBox = checkboxManualEntry,
                gpsView to checkboxGPS,
                donotTagView to checkboxDonotTag)
        }

        donotTagView.setOnClickListener{
            donotTagView.setSelectionState(  selectedCheckBox = checkboxDonotTag,
                gpsView to checkboxGPS,
                manualEntryView to checkboxManualEntry)
        }

    }



}