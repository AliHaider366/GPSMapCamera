package com.example.gpsmapcamera.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.gpsmapcamera.databinding.FragmentStartMenu2Binding
import com.example.gpsmapcamera.utils.setSelectionState


class StartMenuFragment2 : Fragment() {

    private val binding by lazy {
        FragmentStartMenu2Binding.inflate(layoutInflater)
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
        wideView.setSelectionState(  selectedCheckBox = checkboxWide,
            standardView to checkboxStandard)

        wideView.setOnClickListener{
            wideView.setSelectionState(  selectedCheckBox = checkboxWide,
                standardView to checkboxStandard)
        }

        standardView.setOnClickListener{
            standardView.setSelectionState(  selectedCheckBox = checkboxStandard,
                wideView to checkboxWide)
        }


    }


}