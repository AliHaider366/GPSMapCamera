package com.example.gpsmapcamera.activities

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapters.FileNameAdapter
import com.example.gpsmapcamera.databinding.ActivityFileNameBinding
import com.example.gpsmapcamera.models.FieldItem
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager.loadItemOrder
import com.example.gpsmapcamera.utils.formatForFile
import com.example.gpsmapcamera.utils.getCurrentDay
import com.example.gpsmapcamera.utils.getGmtOffset
import com.example.gpsmapcamera.utils.registerGpsResolutionLauncher
import kotlinx.coroutines.launch
import java.util.Date


class FileNameActivity : BaseActivity() {

    private val binding by lazy {
        ActivityFileNameBinding.inflate(layoutInflater)
    }
    private val appViewModel by lazy {
        (application as MyApp).appViewModel
    }

    private val list by lazy {
        val defaultItems = mutableListOf(
            FieldItem(0, getString(R.string.date_and_time), Date().formatForFile(), day = Date().getCurrentDay(), isDropCheck = true, isChecked = true),
            FieldItem(1, getString(R.string.sequence_number), "", isDropCheck = false),
            FieldItem(2, getString(R.string.custom_name_1), getString(R.string.app_name), isDropCheck = false, isChecked = true),
            FieldItem(3, getString(R.string.custom_name_2), "", isDropCheck = false),
            FieldItem(4, getString(R.string.custom_name_3), "", isDropCheck = false),
            FieldItem(5, getString(R.string.full_address), "", address = appViewModel.address, isDropCheck = true),
            FieldItem(6, getString(R.string.latitude_longitude), "${appViewModel.latLong.first},${appViewModel.latLong.second}",
                latLongDMS = "${appViewModel.latLongDMS.first},${appViewModel.latLongDMS.second}", isDropCheck = true),
            FieldItem(7, getString(R.string.plus_code), appViewModel.plusCode),
            FieldItem(8, getString(R.string.time_zone), Date().getGmtOffset()),
            FieldItem(9, getString(R.string.note), "", isDropCheck = false)
        )

        loadItemOrder(defaultItems, this@FileNameActivity)
    }

    private val gpsResolutionLauncher by lazy {
        registerGpsResolutionLauncher(
            onEnabled = {
                Toast.makeText(this, "GPS Enabled ", Toast.LENGTH_SHORT).show()
            },
            onDenied = {
                Toast.makeText(this, "GPS Denied ", Toast.LENGTH_SHORT).show()
            }
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()
    }

    private lateinit var adapter: FileNameAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    private fun init()=binding.apply {
//        checkAndRequestGps(gpsResolutionLauncher)

        filenameTv.movementMethod = ScrollingMovementMethod()

        backBtn.setOnClickListener {
            finish()
        }
        recyclerView.layoutManager = LinearLayoutManager(this@FileNameActivity)

        lifecycleScope.launch {

            // Adapter setup
            adapter = FileNameAdapter(
                list,
                onSelectionChanged = { selected ->
                    filenameTv.text = selected

                    (application as MyApp).appViewModel.setFileName(selected)
                },
                dragStartListener = { viewHolder ->
                    itemTouchHelper.startDrag(viewHolder)
                }
            )

            recyclerView.adapter = adapter

        }


        // ItemTouchHelper setup
        val touchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition
                adapter.onItemMove(fromPos, toPos,this@FileNameActivity)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // No swipe action
            }

            override fun isLongPressDragEnabled() = false // Drag only from icon
        }

        itemTouchHelper = ItemTouchHelper(touchHelperCallback).also {
            it.attachToRecyclerView(recyclerView)
        }

    }

}