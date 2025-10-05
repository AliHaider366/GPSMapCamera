package com.example.gpsmapcamera.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapters.OnBoardingAdapter
import com.example.gpsmapcamera.adapters.StartMenu4Adapter
import com.example.gpsmapcamera.databinding.FragmentStartMenu4Binding
import com.example.gpsmapcamera.utils.StartEdgeSnapHelper
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
            Pair(getString(R.string.vintage), R.drawable.vintage_stam_tem),
            Pair(getString(R.string.bold), R.drawable.bold_stam_tem)
        )

        val adapter = StartMenu4Adapter(itemsList) { pos, name ->
            // Handle selection
        }
        viewPager.adapter = adapter

        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        viewPager.layoutManager = layoutManager

// Center snap
        val snapHelper = StartEdgeSnapHelper(60)
        snapHelper.attachToRecyclerView(viewPager)

        viewPager.setHasFixedSize(true)
        viewPager.clipToPadding = false
        viewPager.clipChildren = false

// Scale + fade animation for center focus
        viewPager.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val center = recyclerView.width / 2
                for (i in 0 until recyclerView.childCount) {
                    val child = recyclerView.getChildAt(i)
                    val childCenter = (child.left + child.right) / 2
                    val distanceFromCenter = kotlin.math.abs(center - childCenter)
                    val scale = 1 - (distanceFromCenter.toFloat() / recyclerView.width) * 0.3f
                    child.scaleY = scale
                    child.scaleX = scale
                    child.alpha = 0.7f + (scale - 0.7f)
                }
            }
        })



// Create tab indicators equal to item count
        repeat(itemsList.size) {
            tabLayout.addTab(tabLayout.newTab())
        }

// Sync tabs when RecyclerView scrolls
        viewPager.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val view = snapHelper.findSnapView(layoutManager)
                    val position = layoutManager.getPosition(view!!)
                    tabLayout.selectTab(tabLayout.getTabAt(position))
                }
            }
        })

// Click on dot → scroll RecyclerView
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    viewPager.smoothScrollToPosition(it.position)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


    }



}