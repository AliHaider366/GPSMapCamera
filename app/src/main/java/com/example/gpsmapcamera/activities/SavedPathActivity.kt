package com.example.gpsmapcamera.activities

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.BuildConfig
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapters.FileSavedAdapter
import com.example.gpsmapcamera.databinding.ActivitySavedPathBinding
import com.example.gpsmapcamera.databinding.AddFolderDialogBinding
import com.example.gpsmapcamera.utils.Constants.CUSTOM_SAVED_FILE_PATH_ROOT
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager.KEY_SELECTED_FOLDER_PATH
import com.example.gpsmapcamera.utils.PrefManager.getFolderList
import com.example.gpsmapcamera.utils.PrefManager.getInt
import com.example.gpsmapcamera.utils.PrefManager.saveFolder
import com.example.gpsmapcamera.utils.PrefManager.saveInt
import com.example.gpsmapcamera.utils.showCustomDialog
import com.example.gpsmapcamera.utils.showToast
import java.io.File

class SavedPathActivity : BaseActivity() {

    private val binding by lazy {
        ActivitySavedPathBinding.inflate(layoutInflater)
    }
    private val appViewModel by lazy {
        (application as MyApp).appViewModel
    }
    private val list by lazy {
        val defaults = mutableListOf(
            getString(R.string.save_original_photos),
            getString(R.string.default_),
            getString(R.string.site_1),
            getString(R.string.site_2)
        )

        val folders = getFolderList(this)

        defaults.apply { addAll(folders) }
    }
    private val rvAdapter by lazy {
        FileSavedAdapter(list, getInt(this,KEY_SELECTED_FOLDER_PATH,1)){ pos, folder->
            appViewModel.setFileSavedPath("$CUSTOM_SAVED_FILE_PATH_ROOT/$folder",folder)
            saveInt(this,KEY_SELECTED_FOLDER_PATH,pos)
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        init()

    }

    private fun init()=binding.apply {


        setupRecycler()

        backBtn.setOnClickListener {
            finish()
        }
        addPathBtn.setOnClickListener {
            showCustomDialog(AddFolderDialogBinding::inflate){binding, dialog ->

                binding.apply {
                    addBtn.setOnClickListener{
                        val folderName = nameTv.text.toString().trim()
                        createNewFolder(folderName)
                        dialog.dismiss()
                    }

                }

            }
        }
    }

    private fun createNewFolder(folderName:String){
        if (folderName.isNotEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, ".nomedia") // create dummy file
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "$CUSTOM_SAVED_FILE_PATH_ROOT/$folderName")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                }

                val uri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    values
                )

                if (uri != null) {
                    showToast("Folder created: DCIM/Camera/$folderName")
                    list.add(folderName)
                    saveFolder(this, folderName)
                    rvAdapter.notifyItemInserted(list.size)
                    contentResolver.delete(uri, null, null)


                } else {
                    showToast(getString(R.string.failed_to_create_folder))
                }

            }
            else {
                // Android 10 and below
                val cameraDir = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    "${BuildConfig.APPLICATION_ID}/$folderName"
                )

                if (!cameraDir.exists()) {
                    val created = cameraDir.mkdirs()
                    if (created) {
                        showToast("Folder created: ${cameraDir.absolutePath}")
                        list.add(folderName)
                        rvAdapter.notifyItemInserted(list.size)
                    } else {
                        showToast(getString(R.string.failed_to_create_folder))
                    }
                } else {
//                    showToast("Folder already exists!")
                }
            }
        } else {
            showToast(getString(R.string.please_enter_folder_name))
        }
    }

    private fun setupRecycler()=binding.recyclerView.apply {

        adapter=rvAdapter
        layoutManager = LinearLayoutManager(this@SavedPathActivity)

    }

}