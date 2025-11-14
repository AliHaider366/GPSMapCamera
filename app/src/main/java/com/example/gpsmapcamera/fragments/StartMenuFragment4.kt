package com.example.gpsmapcamera.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapters.OnBoardingAdapter
import com.example.gpsmapcamera.adapters.StartMenu4Adapter
import com.example.gpsmapcamera.databinding.FragmentStartMenu4Binding
import com.example.gpsmapcamera.utils.setSelectionState
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class StartMenuFragment4 :  Fragment() {

    private val binding by lazy {
        FragmentStartMenu4Binding.inflate(layoutInflater)
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

        val itemsList = listOf(
            Pair(getString(R.string.classic), R.drawable.classic_stam_tem),
            Pair(getString(R.string.modern), R.drawable.modern_stam_tem),
            Pair(getString(R.string.minimal), R.drawable.minimal_stam_tem),
            Pair(getString(R.string.vintage), R.drawable.vintage_stam_tem)
        )

        val adapter = StartMenu4Adapter(itemsList) { pos, name ->
            // Handle selection
        }
        viewPager.adapter = adapter

        val layoutManager = GridLayoutManager(requireActivity(), 2)
        viewPager.layoutManager = layoutManager

    }



}