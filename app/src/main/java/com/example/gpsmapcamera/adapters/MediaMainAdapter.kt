package com.example.gpsmapcamera.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.models.DateGroup
import com.example.gpsmapcamera.models.MediaItem

class MediaMainAdapter(
    private val context: Context,
    private val data: List<DateGroup>,
    private val onSelectionChanged: (Boolean) -> Unit,
    private val onPreview: (MediaItem) -> Unit
) : RecyclerView.Adapter<MediaMainAdapter.DateGroupViewHolder>() {

    inner class DateGroupViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val rvMediaGrid: RecyclerView = view.findViewById(R.id.rvMediaGrid)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateGroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_media_date_group, parent, false)
        return DateGroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: DateGroupViewHolder, position: Int) {
        val group = data[position]
        holder.tvDate.text = group.date

        holder.rvMediaGrid.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = AllMediaAdapter(
                context,
                group.mediaList.toMutableList(),
                onSelectionChanged,
                onPreview
            )
        }
    }


    override fun getItemCount(): Int = data.size
}
