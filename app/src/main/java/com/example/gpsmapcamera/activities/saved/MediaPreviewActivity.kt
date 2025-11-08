package com.example.gpsmapcamera.activities.saved

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.BaseActivity
import com.example.gpsmapcamera.databinding.ActivityMediaPreviewBinding
import com.example.gpsmapcamera.utils.Constants
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.gpsmapcamera.utils.DeleteConfirmationDialog
import com.example.gpsmapcamera.utils.gone
import com.example.gpsmapcamera.utils.visible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class MediaPreviewActivity : BaseActivity() {

    private val binding by lazy {
        ActivityMediaPreviewBinding.inflate(layoutInflater)
    }

    private var mediaUri: Uri? = null
    private var isVideo: Boolean = false
    private var isPlaying = false
    private var duration = 0
    private var progressJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initViews()
        setupPreview()
        clickListeners()
        if (isVideo) setupVideoPreview() else setupImagePreview()
    }

    private fun setupImagePreview() = binding.run {
        imagePreview.visible()
        Glide.with(this@MediaPreviewActivity).load(mediaUri).into(imagePreview)
    }

    private fun setupVideoPreview()  = binding.run {
        imagePreview.gone()
        videoContainer.visible()

        videoView.setVideoURI(mediaUri)
        videoView.setOnPreparedListener { mp ->
            duration = mp.duration
            tvTotalTime.text = formatTime(duration)
            seekBar.max = duration
            isPlaying = true
            videoView.start()
            btnPlayPause.setImageResource(R.drawable.ic_play)
            startProgressUpdates()  // ✅ Start progress updates
        }

        videoView.setOnCompletionListener {
            isPlaying = false
            binding.btnPlayPause.setImageResource(R.drawable.ic_play)
            seekBar.progress = seekBar.max
            binding.tvCurrentTime.text = binding.tvTotalTime.text
        }

        btnPlayPause.setOnClickListener {
            togglePlayPause()
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    videoView.seekTo(progress)
                    binding.tvCurrentTime.text = formatTime(progress)
                }
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }


    private fun togglePlayPause() {
        if (isPlaying) {
            binding.videoView.pause()
            binding.btnPlayPause.setImageResource(R.drawable.ic_play)
            stopProgressUpdates()
        } else {
            // Restart playback if finished
            if (binding.seekBar.progress >= binding.seekBar.max - 500) {
                binding.videoView.seekTo(0)
            }
            binding.videoView.start()
            binding.btnPlayPause.setImageResource(R.drawable.ic_play)
            startProgressUpdates()  // ✅ restart progress tracking
        }
        isPlaying = !isPlaying
    }


    // ✅ Start updating seekbar + time
    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = lifecycleScope.launch(Dispatchers.IO) {
            while (isPlaying) {
                withContext(Dispatchers.Main) {
                    val pos =  binding.videoView.currentPosition
                    binding.seekBar.progress = pos
                    binding.tvCurrentTime.text = formatTime(pos)
                }
                delay(500)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun formatTime(ms: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms.toLong()) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }



    private fun deleteMedia() {
        mediaUri?.let { uri ->
            try {
                contentResolver.delete(uri, null, null)
                Toast.makeText(this, "Deleted successfully", Toast.LENGTH_SHORT).show()
                // ✅ Send deleted URI back
                val resultIntent = Intent().apply {
                    putExtra(Constants.DELETED_MEDIA_URI, uri.toString())
                }
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error deleting file", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (isPlaying) {
            binding.videoView.pause()
            binding.btnPlayPause.setImageResource(R.drawable.ic_play)
        }
        stopProgressUpdates()
        isPlaying = false
    }


    private fun initViews() {
        val uriStr = intent.getStringExtra(Constants.PASSED_MEDIA)
        isVideo = intent.getBooleanExtra(Constants.IS_VIDEO, false)
        mediaUri = uriStr?.toUri()
    }


    private fun setupPreview() = binding.run {
        if (isVideo) {
            videoContainer.visible()
            imagePreview.gone()

            videoView.setVideoURI(mediaUri)
            videoView.setOnPreparedListener { mp ->
                mp.setOnVideoSizeChangedListener { _, _, _ ->
                    btnPlayPause.visible()
                }
            }
            videoView.setOnCompletionListener {
                isPlaying = false
                btnPlayPause.setImageResource(R.drawable.ic_play)
            }

            btnPlayPause.setOnClickListener {
                togglePlayPause()
            }

        } else {
            imagePreview.visible()
            videoContainer.gone()

            Glide.with(this@MediaPreviewActivity)
                .load(mediaUri)
                .into(imagePreview)
        }
    }



    private fun clickListeners() = binding.run {
        btnShare.setOnClickListener {
            mediaUri?.let { uri ->
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = if (isVideo) "video/*" else "image/*"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(shareIntent, "Share via"))
            }
        }

        btnDelete.setOnClickListener {
            DeleteConfirmationDialog(this@MediaPreviewActivity){
                deleteMedia()
            }.show()
        }
    }


}