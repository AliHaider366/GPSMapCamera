package com.example.gpsmapcamera.adapters

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.cameraHelper.TextEditorDialog.EditorOption
import com.example.gpsmapcamera.databinding.EditorOptionAdapterLayoutBinding
import com.example.gpsmapcamera.utils.PrefManager
import com.example.gpsmapcamera.utils.setTextColorRes

class EditorOptionAdapter(
    val onClick: (EditorOption, Int) -> Unit
) : RecyclerView.Adapter<EditorOptionAdapter.EditorOptionViewHolder>() {

    private val items = mutableListOf<EditorOption>()
    private var selectedIndex: Int = RecyclerView.NO_POSITION

    private var bgSelected = -1
    private var textSelected = -1
    private var fontSelected = -1

    fun setInitialSelectedIndices(textColorIndex: Int?, bgColorIndex: Int?, fontIndex: Int?) {
        if (textColorIndex != null && textColorIndex >= 0) textSelected = textColorIndex
        if (bgColorIndex != null && bgColorIndex >= 0) bgSelected = bgColorIndex
        if (fontIndex != null && fontIndex >= 0) fontSelected = fontIndex
    }

    fun submit(newItems: List<EditorOption>, type: Int, context: Context) {
        items.clear()
        selectedIndex = when (type) {
            VIEW_TYPE_COLOR -> textSelected
//            VIEW_TYPE_COLOR -> PrefManager.getInt(context, PrefManager.KEY_TYPE_COLOR,RecyclerView.NO_POSITION)
            VIEW_TYPE_BGCOLOR -> {
//                items.add(0,EditorOption.BGColorOption( Color.parseColor("#000000")))
//                PrefManager.getInt(context, PrefManager.KEY_TYPE_BG_COLOR, RecyclerView.NO_POSITION)
                bgSelected
            }
//            VIEW_TYPE_FONT -> PrefManager.getInt(context, PrefManager.KEY_TYPE_FONT,RecyclerView.NO_POSITION)
            VIEW_TYPE_FONT -> fontSelected
            else -> {
                RecyclerView.NO_POSITION
            }
        }

        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is EditorOption.ColorOption -> VIEW_TYPE_COLOR
        is EditorOption.BGColorOption -> VIEW_TYPE_BGCOLOR
        is EditorOption.FontOption -> VIEW_TYPE_FONT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EditorOptionViewHolder {
        val binding = when (viewType) {
            VIEW_TYPE_COLOR -> EditorOptionAdapterLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            VIEW_TYPE_BGCOLOR -> EditorOptionAdapterLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            VIEW_TYPE_FONT -> EditorOptionAdapterLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            else -> throw IllegalArgumentException("Unknown view type")
        }
        return EditorOptionViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: EditorOptionViewHolder, position: Int) {
        holder.bind(items[position], position, selectedIndex) { newIndex, type ->
            val old = selectedIndex
            selectedIndex = newIndex
            when (type) {
                VIEW_TYPE_COLOR -> PrefManager.saveInt(
                    holder.itemView.context,
                    PrefManager.KEY_TYPE_COLOR,
                    selectedIndex
                )

                VIEW_TYPE_BGCOLOR -> PrefManager.saveInt(
                    holder.itemView.context,
                    PrefManager.KEY_TYPE_BG_COLOR,
                    selectedIndex
                )

                VIEW_TYPE_FONT -> PrefManager.saveInt(
                    holder.itemView.context,
                    PrefManager.KEY_TYPE_FONT,
                    selectedIndex
                )
            }

            if (old != RecyclerView.NO_POSITION) notifyItemChanged(old)
            notifyItemChanged(newIndex)
        }
    }

    // View Holder using ViewBinding
    inner class EditorOptionViewHolder(
        private val binding: ViewBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: EditorOption,
            position: Int,
            selectedIndex: Int,
            selectIndex: (Int, Int) -> Unit
        ) {
            when (item) {
                is EditorOption.ColorOption -> {
                    val colorBinding = binding as EditorOptionAdapterLayoutBinding
                    colorBinding.colorCircleBG.visibility = View.VISIBLE
                    colorBinding.fontPreview.visibility = View.GONE

                    colorBinding.colorCircle.backgroundTintList = ColorStateList.valueOf(item.color)
                    colorBinding.colorCircleBG.setBackgroundResource(
                        if (position == selectedIndex) R.drawable.bg_selected_circle
                        else R.drawable.bg_circle
                    )
                    colorBinding.root.setOnClickListener {
                        selectIndex(position, VIEW_TYPE_COLOR)
                        onClick(item, position)
                        textSelected = position
                    }
                }

                is EditorOption.BGColorOption -> {
                    val colorBinding = binding as EditorOptionAdapterLayoutBinding
                    colorBinding.colorCircleBG.visibility = View.VISIBLE
                    colorBinding.fontPreview.visibility = View.GONE

                    if (position == 0) {
                        colorBinding.colorCircleBG.setBackgroundResource(
                            if (position == selectedIndex)
                                R.drawable.bg_transparent_stroke_selected
                            else
                                R.drawable.bg_transparent_stroke
                        )
                    } else {
                        colorBinding.colorCircle.backgroundTintList =
                            ColorStateList.valueOf(item.color)

                        colorBinding.colorCircleBG.setBackgroundResource(
                            if (position == selectedIndex) R.drawable.bg_selected_circle
                            else R.drawable.bg_circle
                        )
                    }

                    colorBinding.root.setOnClickListener {
                        selectIndex(position, VIEW_TYPE_BGCOLOR)
                        onClick(item, position)
                        bgSelected = position
                    }
                }

                is EditorOption.FontOption -> {
                    val fontBinding = binding as EditorOptionAdapterLayoutBinding
                    fontBinding.colorCircleBG.visibility = View.GONE
                    fontBinding.fontPreview.visibility = View.VISIBLE

                    fontBinding.fontPreview.text = item.sample
                    fontBinding.fontPreview.typeface = item.typeface

                    fontBinding.fontPreview.setTextColorRes(
                        if (position == selectedIndex) {
                            R.color.blue
                        } else R.color.white
                    )

                    fontBinding.root.setOnClickListener {
                        selectIndex(position, VIEW_TYPE_FONT)
                        onClick(item, position)
                    }
                }
            }
        }
    }

    companion object {
        private const val VIEW_TYPE_COLOR = 0
        private const val VIEW_TYPE_BGCOLOR = 1
        private const val VIEW_TYPE_FONT = 2
    }
}
