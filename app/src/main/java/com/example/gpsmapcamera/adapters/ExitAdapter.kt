package com.example.gpsmapcamera.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.databinding.ExitItemBinding
import com.example.gpsmapcamera.models.ExitModel

class ExitAdapter(
    private val items: List<ExitModel>,
    private val onItemClick: (ExitModel, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ExitItemBinding.inflate(inflater, parent, false)
        return SingleViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is SingleViewHolder -> holder.bind(item, position)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class SingleViewHolder(private val binding: ExitItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExitModel, position: Int) {
            binding.apply {
                tvMain.text = item.title
                ivMain.setImageResource(item.mainIcon)
                root.setOnClickListener { onItemClick(item, position) }
            }
        }
    }

}
