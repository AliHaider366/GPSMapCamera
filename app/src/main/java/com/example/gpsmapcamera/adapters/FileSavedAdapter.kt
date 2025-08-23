package com.example.gpsmapcamera.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.databinding.FileSavedAdapterLayout1Binding
import com.example.gpsmapcamera.databinding.FileSavedAdapterLayout2Binding
import com.example.gpsmapcamera.utils.gone
import com.example.gpsmapcamera.utils.visible

class FileSavedAdapter(
    private val items: List<String>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val SWITCH_VIEW = 0
        private const val ITEM_VIEW = 1
    }

    // ViewHolders using ViewBinding
   inner class TextViewHolder(val binding: FileSavedAdapterLayout1Binding) : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(item: String) {
           binding.titleTv.text=item
        }
   }
    var currentPos=-1
    var previousPre=-1
   inner class ImageViewHolder(val binding: FileSavedAdapterLayout2Binding) : RecyclerView.ViewHolder(binding.root){

       fun bind(item: String) {
           binding.titleTv.text=item

           if (position==currentPos)
           {
               binding.checkIc.visible()

           }
           else binding.checkIc.gone()
           binding.root.setOnClickListener{
               previousPre=currentPos
               currentPos=position
               notifyDataSetChanged()
           }
       }

   }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> SWITCH_VIEW
            else -> ITEM_VIEW
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            SWITCH_VIEW -> {
                val binding = FileSavedAdapterLayout1Binding.inflate(inflater, parent, false)
                TextViewHolder(binding)
            }
            ITEM_VIEW -> {
                val binding = FileSavedAdapterLayout2Binding.inflate(inflater, parent, false)
                ImageViewHolder(binding)
            }

            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]

        when (holder) {
            is TextViewHolder -> holder.bind(item)
            is ImageViewHolder -> holder.bind(item)

        }
    }

    override fun getItemCount() = items.size
}
