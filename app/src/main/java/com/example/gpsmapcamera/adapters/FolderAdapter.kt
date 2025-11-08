package com.example.gpsmapcamera.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.R

class FolderAdapter(
    private val folders: List<String>,
    private val onFolderClick: (String) -> Unit
) : RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    private var selectedPosition = 0

    inner class FolderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvFolderName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_folder_tab, parent, false)
        return FolderViewHolder(view)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folders[position]
        holder.name.text = folder

        val context = holder.itemView.context
        val isSelected = position == selectedPosition

        holder.name.apply {
            background = ContextCompat.getDrawable(
                context,
                if (isSelected) R.drawable.bg_folder_selected else R.drawable.bg_folder_unselected
            )
        }

        holder.itemView.setOnClickListener {
            val prev = selectedPosition
            selectedPosition = position
            notifyItemChanged(prev)
            notifyItemChanged(selectedPosition)
            onFolderClick(folder)
        }
    }

    override fun getItemCount(): Int = folders.size
}
