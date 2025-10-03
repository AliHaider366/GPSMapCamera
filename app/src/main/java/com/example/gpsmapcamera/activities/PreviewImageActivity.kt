package com.example.gpsmapcamera.activities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.gpsmapcamera.databinding.ActivityPreviewImageBinding
import java.io.File

class PreviewImageActivity : BaseActivity() {

    private val binding by lazy {
        ActivityPreviewImageBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//        val bitmap = intent.getStringExtra("image_uri")?.let { getRotatedBitmap(it.toUri()) }

        Glide.with(this)
            .load( intent.getStringExtra("image_uri"))
            .into(binding.filteredImageView)  // replace with your actual ImageView ID

    }

    fun getRotatedBitmap(imageUri: Uri): Bitmap? {
        val inputStream = contentResolver.openInputStream(imageUri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val file = File(imageUri.path ?: return bitmap)
        val exif = ExifInterface(file.absolutePath)
        val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)

        val rotation = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}