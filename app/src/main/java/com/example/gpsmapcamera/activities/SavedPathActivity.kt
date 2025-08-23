package com.example.gpsmapcamera.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapters.FileSavedAdapter
import com.example.gpsmapcamera.databinding.ActivitySavedPathBinding

class SavedPathActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySavedPathBinding.inflate(layoutInflater)
    }
    private val list by lazy {
        mutableListOf(
            getString(R.string.save_original_photos),
            getString(R.string.default_),
            getString(R.string.site_1),
            getString(R.string.site_2)

        )

    }
    private val rvAdapter by lazy {
        FileSavedAdapter(list)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()

    }

    private fun init()=binding.apply {

        setupRecycler()
    }

    private fun setupRecycler()=binding.recyclerView.apply {

        adapter=rvAdapter
        layoutManager = LinearLayoutManager(this@SavedPathActivity)
    }

}