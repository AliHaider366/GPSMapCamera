package com.example.gpsmapcamera.activities.template.basic

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapters.ReportingTagAdapter
import com.example.gpsmapcamera.databinding.ActivityReportingTagBinding
import com.example.gpsmapcamera.models.StampItemName
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager.getInt
import com.example.gpsmapcamera.utils.PrefManager.setInt
import com.example.gpsmapcamera.utils.StampPreferences
import com.example.gpsmapcamera.utils.reportingTagsDefault
import com.example.gpsmapcamera.utils.showToast

class ReportingTagActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityReportingTagBinding.inflate(layoutInflater)
    }

    private val appViewModel by lazy {
        (applicationContext as MyApp).appViewModel
    }


    private var reportingTagList = arrayListOf<String>()

    private val savedList by lazy {
        stampPref.getWholeList(Constants.KEY_REPORTING_TAG)
    }

    private val reportingAdapter by lazy {
        ReportingTagAdapter { position ->
            setInt(this@ReportingTagActivity, Constants.SELECTED_REPORTING_TAG, position)
            updateAppLevelTag()
        }
    }

    private lateinit var stampPref: StampPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        initUI()
        setUpRV()
        clickListeners()
    }

    private fun clickListeners() = binding.run {
        btnAdd.setOnClickListener {
            if (etMain.text.trim().isNotEmpty()) {
                addReportingTag(etMain.text.trim().toString())
                etMain.setText("")
            } else {
                showToast(getString(R.string.please_enter_tag))
            }
        }


        backBtn.setOnClickListener {
            backPressedCallback.handleOnBackPressed()
        }

    }

    private fun addReportingTag(tag: String) {
        var newListToSave = arrayListOf<String>()
        newListToSave.add(tag)
        if (savedList.isEmpty()) {
            newListToSave.addAll(reportingTagsDefault)
        } else {
            newListToSave.addAll(savedList)
        }

        stampPref.saveWholeList(Constants.KEY_REPORTING_TAG, newListToSave)
        setInt(this@ReportingTagActivity, Constants.SELECTED_REPORTING_TAG, 0)
        reportingAdapter.setList(
            newListToSave,
            getInt(this@ReportingTagActivity, Constants.SELECTED_REPORTING_TAG, 0)
        )
        binding.recyclerView.scrollToPosition(0)

        updateAppLevelTag()

    }

    private fun initUI() {

        stampPref = StampPreferences(this@ReportingTagActivity)


        reportingTagList = if (savedList.isEmpty()) {
            reportingTagsDefault
        } else {
            savedList as ArrayList
        }

    }

    private fun setUpRV() = binding.run {
        // Initialize RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this@ReportingTagActivity)
        recyclerView.adapter = reportingAdapter
        reportingAdapter.setList(
            reportingTagList,
            getInt(this@ReportingTagActivity, Constants.SELECTED_REPORTING_TAG, 0)
        )
    }

    private fun updateAppLevelTag() {
        appViewModel.updateStampVisibility(
            Constants.REPORTING_TEMPLATE,
            StampItemName.REPORTING_TAG,
            true
        )
    }


    private val backPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            setResult(RESULT_OK)
            finish()
        }
    }




}