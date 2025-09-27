package com.example.gpsmapcamera.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.gpsmapcamera.databinding.FragmentStartMenu1Binding
import com.example.gpsmapcamera.utils.setSelectionState


class StartMenuFragment1 : Fragment() {


    private val binding by lazy {
        FragmentStartMenu1Binding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init()
    }

    private fun init()=binding.apply {

        /* by default selection*/
        photoView.setSelectionState(  selectedCheckBox = checkboxPhoto,
            videoView to checkboxVideo,
            shareView to checkboxShare)

        photoView.setOnClickListener{
            photoView.setSelectionState(  selectedCheckBox = checkboxPhoto,
                videoView to checkboxVideo,
                shareView to checkboxShare)
        }

        videoView.setOnClickListener{
            videoView.setSelectionState(  selectedCheckBox = checkboxVideo,
                photoView to checkboxPhoto,
                shareView to checkboxShare)
        }

        shareView.setOnClickListener{
            shareView.setSelectionState(  selectedCheckBox = checkboxShare,
                photoView to checkboxPhoto,
                videoView to checkboxVideo)
        }

    }


}