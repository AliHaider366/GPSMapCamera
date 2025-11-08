package com.example.gpsmapcamera.activities.saved

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.BuildConfig
import com.example.gpsmapcamera.activities.BaseActivity
import com.example.gpsmapcamera.adapters.FolderAdapter
import com.example.gpsmapcamera.adapters.MediaMainAdapter
import com.example.gpsmapcamera.databinding.ActivitySavedMediaBinding
import com.example.gpsmapcamera.models.DateGroup
import com.example.gpsmapcamera.models.MediaItem
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.DeleteConfirmationDialog
import com.example.gpsmapcamera.utils.gone
import com.example.gpsmapcamera.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SavedMediaActivity : BaseActivity() {

    private val binding by lazy {
        ActivitySavedMediaBinding.inflate(layoutInflater)
    }

    private lateinit var mediaAdapter: MediaMainAdapter
    private lateinit var folderAdapter: FolderAdapter

    private var allFolders = mutableListOf<String>()
    private var currentFolder = "Default"
    private var groupedData = listOf<DateGroup>()

    private var mediaPreviewLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val deletedUri = result.data?.getStringExtra(Constants.DELETED_MEDIA_URI)
            if (!deletedUri.isNullOrEmpty()) {
                loadMediaForFolder(currentFolder)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initViews()
        loadFolders()
        clickListeners()
    }

    private fun clickListeners() = binding.run {
        backBtn.setOnClickListener {
            finish()
        }

        btnDelete.setOnClickListener {
            DeleteConfirmationDialog(this@SavedMediaActivity) {
                deleteSelectedMedia()
            }.show()
        }
    }

    private fun initViews() = binding.run {
        btnDelete.isEnabled = false
        btnDelete.alpha = 0.5f
        mediaRecycler.layoutManager = LinearLayoutManager(this@SavedMediaActivity)
        folderRecycler.layoutManager = LinearLayoutManager(this@SavedMediaActivity, LinearLayoutManager.HORIZONTAL, false)
    }

    private fun loadFolders() {
        val baseDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            BuildConfig.APPLICATION_ID
        )
        if (!baseDir.exists()) baseDir.mkdirs()

        val defaultFolders = listOf("Default", "Site 1", "Site 2")
        val existing = baseDir.listFiles()?.filter { it.isDirectory }?.map { it.name } ?: emptyList()

        allFolders.clear()
        allFolders.addAll((defaultFolders + existing).distinct())

        folderAdapter = FolderAdapter(allFolders) { folderName ->
            currentFolder = folderName
            loadMediaForFolder(folderName)
        }
        binding.folderRecycler.adapter = folderAdapter

        // Default folder load
        loadMediaForFolder("Default")
    }

    private fun loadMediaForFolder(folderName: String) {
        binding.btnDelete.isEnabled = false
        binding.btnDelete.alpha = 0.5f
        lifecycleScope.launch(Dispatchers.IO) {
            val mediaList = fetchSavedMediaForFolder(this@SavedMediaActivity, folderName)
            groupedData = mediaList.groupBy { it.date }.map { DateGroup(it.key, it.value) }

            withContext(Dispatchers.Main) {
                if (groupedData.isEmpty()) {
                    binding.mediaRecycler.gone()
                    binding.emptyView.visible()
                } else {
                    binding.mediaRecycler.visible()
                    binding.emptyView.gone()
                    mediaAdapter= MediaMainAdapter(
                        this@SavedMediaActivity,
                        groupedData,
                        onSelectionChanged = { hasSelection ->
                            binding.btnDelete.isEnabled = hasSelection
                            binding.btnDelete.alpha = if (hasSelection) 1f else 0.5f
                        },
                        onPreview = { openMediaPreview(it) }
                    )

                    binding.mediaRecycler.adapter = mediaAdapter
                }
            }
        }
    }


    private fun openMediaPreview(item: MediaItem) {
        val intent = Intent(this, MediaPreviewActivity::class.java)
        intent.putExtra(Constants.PASSED_MEDIA, item.uri.toString())
        intent.putExtra(Constants.IS_VIDEO, item.isVideo)
        mediaPreviewLauncher.launch(intent)
    }



    private fun deleteSelectedMedia() {
        val selectedItems = groupedData.flatMap { it.mediaList }.filter { it.isSelected }
        lifecycleScope.launch(Dispatchers.IO) {
            selectedItems.forEach {
                try {
                    contentResolver.delete(it.uri, null, null)
                } catch (_: Exception) {}
            }
            withContext(Dispatchers.Main) {
                loadMediaForFolder(currentFolder)
            }
        }
    }


    @SuppressLint("Range")
    fun fetchSavedMediaForFolder(context: Context, folderName: String): List<MediaItem> {
        val result = mutableListOf<MediaItem>()
        val basePath = "DCIM/${BuildConfig.APPLICATION_ID}/$folderName"

        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.RELATIVE_PATH
        )

        val selection = "${MediaStore.Files.FileColumns.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%$basePath%")

        val sortOrder = "${MediaStore.Files.FileColumns.DATE_ADDED} DESC"
        val queryUri = MediaStore.Files.getContentUri("external")

        context.contentResolver.query(queryUri, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID)
            val typeCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE)
            val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val type = cursor.getInt(typeCol)
                val dateAdded = cursor.getLong(dateCol) * 1000L

                val uri = when (type) {
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE ->
                        ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO ->
                        ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)
                    else -> null
                } ?: continue

                val isVideo = type == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
                val dateStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date(dateAdded))

                result.add(MediaItem(uri, isVideo, dateStr))
            }
        }

        return result
    }


}