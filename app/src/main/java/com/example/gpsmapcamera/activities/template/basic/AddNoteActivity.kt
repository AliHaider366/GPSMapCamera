package com.example.gpsmapcamera.activities.template.basic

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.template.basic.CoordinateLatLongActivity
import com.example.gpsmapcamera.activities.template.basic.ReportingTagActivity
import com.example.gpsmapcamera.adapters.LatLongAdapter
import com.example.gpsmapcamera.adapters.RecentNotesAdapter
import com.example.gpsmapcamera.adapters.ReportingTagAdapter
import com.example.gpsmapcamera.databinding.ActivityAddNoteBinding
import com.example.gpsmapcamera.models.NoteModel
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager.getInt
import com.example.gpsmapcamera.utils.PrefManager.setInt
import com.example.gpsmapcamera.utils.StampPreferences
import com.example.gpsmapcamera.utils.coordinateFormats
import com.example.gpsmapcamera.utils.plusCodeFormats
import com.example.gpsmapcamera.utils.recentNotesDefault
import com.example.gpsmapcamera.utils.reportingTagsDefault
import com.example.gpsmapcamera.utils.showToast

class AddNoteActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAddNoteBinding.inflate(layoutInflater)
    }


    private val passedTemplate by lazy {
        intent.getStringExtra(Constants.PASSED_STAMP_TEMPLATE)?: Constants.CLASSIC_TEMPLATE
    }



    private var recentNotesList = arrayListOf<NoteModel>()



    private val stampPref by lazy {
        StampPreferences(this@AddNoteActivity)
    }


    private val recentNotesAdapter by lazy {
        RecentNotesAdapter { position ->
            setInt(this@AddNoteActivity, Constants.SELECTED_NOTE, position)
            finish()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initUI()
        setUpRV()
        clickListeners()
    }

    private fun clickListeners() = binding.run {
        tvAdd.setOnClickListener {
            if (etMain.text.trim().isNotEmpty()){
                val noteModel = (NoteModel(title = etMain.text.trim().toString(), note = etNote.text.trim().toString()))
                addRecentNote(noteModel)
            }else{
                showToast(getString(R.string.please_enter_tag))
            }
        }
    }

    private fun addRecentNote(tag: NoteModel) {
        var newListToSave = arrayListOf<NoteModel>()
        newListToSave.add(tag)
        val savedList = stampPref.getNoteModel(Constants.KEY_RECENT_NOTES)
        if (savedList.isEmpty()){
            newListToSave.addAll(recentNotesDefault)
        }else{
            newListToSave.addAll(savedList)
        }
        stampPref.saveNoteModel(Constants.KEY_RECENT_NOTES,newListToSave)
        setInt(this@AddNoteActivity, Constants.SELECTED_NOTE, 0)
        recentNotesAdapter.setList(newListToSave, getInt(this@AddNoteActivity, Constants.SELECTED_NOTE, 0))
        binding.rvRecentNotes.scrollToPosition(0)

    }

    private fun initUI() {
        val savedList = stampPref.getNoteModel(Constants.KEY_RECENT_NOTES)

        recentNotesList = if (savedList.isEmpty()){
            recentNotesDefault
        }else{
            savedList as ArrayList
        }


        val item = recentNotesList[getInt(this@AddNoteActivity, Constants.SELECTED_NOTE, 0)]

        binding.etNote.setText(item.note)
        binding.etMain.setText(item.title)

    }

    private fun setUpRV() = binding.run {
        // Initialize RecyclerView
        rvRecentNotes.layoutManager = LinearLayoutManager(this@AddNoteActivity)
        rvRecentNotes.adapter = recentNotesAdapter
        recentNotesAdapter.setList(recentNotesList, getInt(this@AddNoteActivity, Constants.SELECTED_NOTE, 0))
    }


}