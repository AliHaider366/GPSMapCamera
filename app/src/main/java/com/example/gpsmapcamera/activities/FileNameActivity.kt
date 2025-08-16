package com.example.gpsmapcamera.activities

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapters.FileNameAdapter
import com.example.gpsmapcamera.databinding.ActivityCameraBinding
import com.example.gpsmapcamera.databinding.ActivityFileNameBinding
import com.example.gpsmapcamera.utils.formatForFile
import com.example.gpsmapcamera.utils.getCurrentDay
import com.example.gpsmapcamera.utils.getCurrentLatLong
import com.example.gpsmapcamera.utils.getCurrentPlusCode
import com.example.gpsmapcamera.utils.getGmtOffset
import com.example.gpsmapcamera.utils.toDMSPair
import kotlinx.coroutines.launch
import java.util.Date

data class FieldItem(
    val name: String,
    var value: String,
    val day: String="",
    val latLongDMS: String="",
    var isChecked: Boolean = false,
    var isCheckBox1Checked: Boolean = false,
    var isCheckBox2Checked: Boolean = false,
    var isCheckBox3Checked: Boolean = false,
    var isCheckBox4Checked: Boolean = false,
    var isDropCheck: Boolean?=null,
    var isDropDownVisible: Boolean?=null,
    var isPremium: Boolean=false
)


class FileNameActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityFileNameBinding.inflate(layoutInflater)
    }

    private val list by lazy {
        mutableListOf(
            FieldItem("Date and Time",Date().formatForFile(), day = Date().getCurrentDay(), isDropCheck = true, isChecked = true),
            FieldItem("Sequence number","",isDropCheck = false),
            FieldItem("Custom name 1","",isDropCheck = false),
            FieldItem("Custom name 2","",isDropCheck = false),
            FieldItem("Custom name 3","",isDropCheck = false),
            FieldItem("Full Address","", isDropCheck = true),
            FieldItem("Latitude/longitude","", isDropCheck = true),
            FieldItem("Plus code",""),
            FieldItem("Time zone",Date().getGmtOffset()),
            FieldItem("Note","",isDropCheck = false)
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

        recyclerView.layoutManager = LinearLayoutManager(this@FileNameActivity)

        lifecycleScope.launch {
            val pluscode=getCurrentPlusCode()
            list.set(7,FieldItem("Plus code",pluscode))
            val latLong=getCurrentLatLong()
            val latLongDMS=latLong.toDMSPair()
            list.set(6,FieldItem("Latitude/Longitude","${latLong.first},${latLong.second}",
                latLongDMS = "${latLongDMS.first},${latLongDMS.second}",isDropCheck = true))


            // Adapter setup
            adapter = FileNameAdapter(
                list,
                onSelectionChanged = { selected ->
                    filenameTv.text = selected
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
                adapter.onItemMove(fromPos, toPos)
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