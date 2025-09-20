package com.example.gpsmapcamera.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.SingleLatLongBinding
import com.example.gpsmapcamera.databinding.SingleRecentNoteBinding
import com.example.gpsmapcamera.models.DynamicStampValues
import com.example.gpsmapcamera.models.NoteModel
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.MyApp
import com.example.gpsmapcamera.utils.PrefManager.getInt
import com.example.gpsmapcamera.utils.formatCoordinatesByPosition
import com.example.gpsmapcamera.utils.formatPlusCodeByPosition

class RecentNotesAdapter(
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<RecentNotesAdapter.ViewHolder>() {


    private var selectedPosition: Int = 0
    private var dataList = arrayListOf<NoteModel>()

    fun setList(passedList: ArrayList<NoteModel>, position: Int) {
        selectedPosition = position
        dataList.clear()
        dataList.addAll(passedList)
        notifyDataSetChanged()
    }


    inner class ViewHolder(private val binding: SingleRecentNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NoteModel, position: Int) = binding.run {

            tvTitle.text = buildString {
                append(item.note)
                append(" ")
                append(item.title)
            }
            checkbox.isClickable = false

            checkbox.isChecked = selectedPosition == position

            root.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition) // update old
                notifyItemChanged(selectedPosition) // update new
                onItemClick(position)
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            SingleRecentNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(dataList[position], position)
    }

    override fun getItemCount(): Int = dataList.size
}