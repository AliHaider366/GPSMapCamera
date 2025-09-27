package com.example.gpsmapcamera.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gpsmapcamera.adapters.StartMenuAdapter
import com.example.gpsmapcamera.databinding.ActivityStartMenuBinding
import com.example.gpsmapcamera.fragments.StartMenuFragment1
import com.example.gpsmapcamera.fragments.StartMenuFragment2
import com.example.gpsmapcamera.fragments.StartMenuFragment3
import com.example.gpsmapcamera.fragments.StartMenuFragment4


class StartMenuActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityStartMenuBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        init()

    }

    private fun init()=binding.apply {
        val fragments = listOf(StartMenuFragment1(), StartMenuFragment2(), StartMenuFragment3(),StartMenuFragment4())

        val adapter = StartMenuAdapter(this@StartMenuActivity, fragments)
        viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false // This disables swipe gestures


        binding.continueBtn.setOnClickListener {
            val nextItem = binding.viewPager.currentItem + 1
            if (nextItem < adapter.itemCount) {
                binding.viewPager.currentItem = nextItem
            } else {
//                binding.viewPager.currentItem = 0
            }
        }
    }

}