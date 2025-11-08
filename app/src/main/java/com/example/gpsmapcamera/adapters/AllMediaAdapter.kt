package com.example.gpsmapcamera.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.models.MediaItem

class AllMediaAdapter(
    private val context: Context,
    private val mediaList: MutableList<MediaItem>,
    private val onSelectionChanged: (Boolean) -> Unit,  // callback to update delete button
    private val onPreview: (MediaItem) -> Unit          // callback to open preview
) : RecyclerView.Adapter<AllMediaAdapter.MediaViewHolder>() {


    inner class MediaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val thumbnail: ImageView = view.findViewById(R.id.ivThumbnail)
        val playIcon: ImageView = view.findViewById(R.id.ivPlayIcon)
        val checkBox: CheckBox = view.findViewById(R.id.checkSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_media_thumbnail, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val item = mediaList[position]

        Glide.with(context)
            .load(item.uri)
            .centerCrop()
            .into(holder.thumbnail)

        holder.playIcon.visibility = if (item.isVideo) View.VISIBLE else View.GONE
        holder.checkBox.isChecked = item.isSelected

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = item.isSelected

        // Toggle selection
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            item.isSelected = isChecked
            onSelectionChanged(mediaList.any { it.isSelected })
        }

        // Preview click
        holder.thumbnail.setOnClickListener {
            onPreview(item)
        }
    }

    override fun getItemCount(): Int = mediaList.size
}
