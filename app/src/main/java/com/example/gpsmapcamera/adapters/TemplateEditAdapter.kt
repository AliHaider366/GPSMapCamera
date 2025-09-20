package com.example.gpsmapcamera.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.databinding.ItemHeaderBinding
import com.example.gpsmapcamera.databinding.ItemOptionBinding
import com.example.gpsmapcamera.models.TemplateModificationItem
import com.example.gpsmapcamera.utils.gone
import com.example.gpsmapcamera.utils.visible

class TemplateEditAdapter(
    private val items: List<TemplateModificationItem>,
    private val onCheckedChange: (position: Int, isChecked: Boolean) -> Unit,
    private val onItemClick: (TemplateModificationItem.Option,Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_OPTION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is TemplateModificationItem.Header -> TYPE_HEADER
            is TemplateModificationItem.Option -> TYPE_OPTION
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemHeaderBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemOptionBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                OptionViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(items[position] as TemplateModificationItem.Header)
            is OptionViewHolder -> holder.bind(items[position] as TemplateModificationItem.Option)
        }
    }

    override fun getItemCount(): Int = items.size

    // --- Header Holder ---
    class HeaderViewHolder(private val binding: ItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TemplateModificationItem.Header) {
            binding.tvHeader.text = item.title
        }
    }

    // --- Option Holder ---
    inner class OptionViewHolder(
        private val binding: ItemOptionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TemplateModificationItem.Option) =  binding.run {
            tvTitle.text = item.title
            if (item.isShowArrow){
                ivArrow.visible()
            }else{
                ivArrow.gone()
            }

            item.isChecked?.let {
                checkbox.visible()
                checkbox.isChecked = it
            }?:run {
                checkbox.gone()
            }

            // Avoid duplicate callbacks when recycling
            checkbox.setOnCheckedChangeListener(null)
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                onCheckedChange(adapterPosition, isChecked)
            }

            // Item click (excluding checkbox)
            root.setOnClickListener {
                onItemClick(item,adapterPosition)
            }
        }
    }
}
