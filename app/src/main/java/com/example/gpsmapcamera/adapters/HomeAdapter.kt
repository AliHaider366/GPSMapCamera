package com.example.gpsmapcamera.adapters


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.databinding.HomeSquareItemBinding
import com.example.gpsmapcamera.databinding.HomeTopItemBinding
import com.example.gpsmapcamera.models.HomeModel

class HomeAdapter(
    private val items: List<HomeModel>,
    private val onItemClick: (HomeModel, Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SINGLE = 0
        private const val TYPE_SQUARE = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_SINGLE else TYPE_SQUARE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_SINGLE -> {
                val binding = HomeTopItemBinding.inflate(inflater, parent, false)
                SingleViewHolder(binding)
            }
            else -> {
                val binding = HomeSquareItemBinding.inflate(inflater, parent, false)
                SquareViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is SingleViewHolder -> holder.bind(item, position)
            is SquareViewHolder -> holder.bind(item, position)
        }
    }

    override fun getItemCount(): Int = items.size

    inner class SingleViewHolder(private val binding: HomeTopItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeModel, position: Int) {
            binding.apply {
                tvMain.text = item.title
                tvDesc.text = item.desc
                ivMain.setImageResource(item.mainIcon)
                ivBg.setImageResource(item.bgIcon)
                main.setBackgroundResource(item.bg)
                root.setOnClickListener { onItemClick(item, position) }
            }
        }
    }

    inner class SquareViewHolder(private val binding: HomeSquareItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: HomeModel, position: Int) {
            binding.apply {
                tvMain.text = item.title
                tvDesc.text = item.desc
                ivMain.setImageResource(item.mainIcon)
                ivBg.setImageResource(item.bgIcon)
                main.setBackgroundResource(item.bg)

                root.setOnClickListener { onItemClick(item, position) }
            }
        }
    }
}
