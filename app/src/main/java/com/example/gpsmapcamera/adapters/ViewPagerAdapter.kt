package com.example.gpsmapcamera.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.gpsmapcamera.activities.template.fragments.BasicFragment
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager

class ViewPagerAdapter(private val fragmentTypes : List<String>, private val fragmentActivity: FragmentActivity) :
    FragmentStateAdapter(fragmentActivity) {


    override fun getItemCount(): Int = fragmentTypes.size

    override fun createFragment(position: Int): Fragment {
        val fragment = BasicFragment()
        val args = Bundle().apply {
            putString("fragmentType", fragmentTypes[position])
        }
        fragment.arguments = args
        return fragment
    }
}