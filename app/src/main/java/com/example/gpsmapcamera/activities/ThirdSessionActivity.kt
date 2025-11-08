package com.example.gpsmapcamera.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapters.OnBoardingAdapter
import com.example.gpsmapcamera.databinding.ActivityOnBoardingBinding
import com.example.gpsmapcamera.databinding.ActivityThirdSessionBinding
import com.example.gpsmapcamera.utils.launchActivity

class ThirdSessionActivity : BaseActivity() {
    private val binding by lazy {
        ActivityThirdSessionBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val itemsList = listOf(
            Pair(getString(R.string.add_quick_tags_to_your_images), R.drawable.third_session_img_1),
            Pair(getString(R.string.stamp_your_photo_with_location_and_time_zone), R.drawable.third_session_img_2),
        )

        val adapter = OnBoardingAdapter(itemsList)
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false // This disables swipe gestures

        binding.continueBtn.setOnClickListener {
            val nextItem = binding.viewPager.currentItem + 1
            if (nextItem < adapter.itemCount) {
                binding.viewPager.currentItem = nextItem
            } else {
                launchActivity<CameraActivity> {  }
            }
        }
    }
}