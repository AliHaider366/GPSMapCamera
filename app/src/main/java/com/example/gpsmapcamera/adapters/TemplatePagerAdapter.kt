package com.example.gpsmapcamera.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.gpsmapcamera.activities.template.fragments.BasicTemplateFragment
import com.example.gpsmapcamera.activities.template.fragments.TopTemplateFragment

class TemplatePagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> BasicTemplateFragment()
            else -> TopTemplateFragment()
        }
    }
}
