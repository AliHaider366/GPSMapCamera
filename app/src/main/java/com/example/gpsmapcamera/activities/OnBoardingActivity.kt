package com.example.gpsmapcamera.activities

import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapters.OnBoardingAdapter
import com.example.gpsmapcamera.databinding.ActivityOnBoardingBinding
import com.example.gpsmapcamera.utils.gone
import com.example.gpsmapcamera.utils.launchActivity
import com.example.gpsmapcamera.utils.onEnabledChanged
import com.example.gpsmapcamera.utils.startPulseAnimation
import com.google.android.material.tabs.TabLayoutMediator


class OnBoardingActivity : BaseActivity() {
    private val binding by lazy {
        ActivityOnBoardingBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val itemsList = listOf(
            Pair(getString(R.string.capture_beyond_photos), R.drawable.on_board1),
            Pair(getString(R.string.shoot_like_a_pro), R.drawable.on_board2),
            Pair(getString(R.string.stamp_it_save_it_share_it), R.drawable.on_board3),
            Pair(getString(R.string.your_camera_your_way), R.drawable.on_board4)
        )

        val adapter = OnBoardingAdapter(itemsList)
        binding.viewPager.adapter = adapter
//        binding.viewPager.isUserInputEnabled = false // This disables swipe gestures

        binding.continueBtn.setOnClickListener {
            val nextItem = binding.viewPager.currentItem + 1
            if (nextItem < adapter.itemCount) {
                binding.viewPager.currentItem = nextItem
            } else {
                launchActivity<PermissionActivity> { }
//                binding.viewPager.currentItem = 0
            }
        }



        binding.continueBtn.startPulseAnimation()


        // Attach TabLayout and ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
        }.attach()

        binding.viewPager.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                if (position > 2) binding.lottieView.gone() else binding.lottieView.gone()
            }

            override fun onPageSelected(position: Int) {}

            override fun onPageScrollStateChanged(state: Int) {}
        })
    }
}