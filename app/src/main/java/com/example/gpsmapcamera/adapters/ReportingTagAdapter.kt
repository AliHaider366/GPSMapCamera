package com.example.gpsmapcamera.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.SingleDateFormatBinding
import com.example.gpsmapcamera.utils.Constants
import com.example.gpsmapcamera.utils.PrefManager.getInt
import com.example.gpsmapcamera.utils.formatToString
import java.util.Date

class ReportingTagAdapter(
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ReportingTagAdapter.ViewHolder>() {

    private var selectedPosition: Int = 0
    private var reportingTag = arrayListOf<String>()

    fun setList(passedList : ArrayList<String>, position: Int){
        selectedPosition = position
        reportingTag.clear()
        reportingTag.addAll(passedList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: SingleDateFormatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(tag: String, position: Int) = binding.run {
            tvMain.text = tag


            // Update background based on selection
            if (position == selectedPosition) {
                root.setBackgroundResource(R.drawable.bg_selected_stroke_5)
                tvMain.setTextColor(root.context.getColor(R.color.blue))
            } else {
                root.setBackgroundResource(R.drawable.bg_white_stroke_5)
                tvMain.setTextColor(root.context.getColor(R.color.textMainColor))
            }

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
        val binding = SingleDateFormatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(reportingTag[position], position)
    }

    override fun getItemCount(): Int = reportingTag.size
}