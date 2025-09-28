package com.example.gpsmapcamera.adapters

import android.app.Application
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.SettingAboutItemBinding
import com.example.gpsmapcamera.databinding.SettingFeatureItemBinding
import com.example.gpsmapcamera.databinding.SettingGeneralItemBinding
import com.example.gpsmapcamera.databinding.SettingHeadingItemBinding
import com.example.gpsmapcamera.utils.setCompoundDrawableTintAndTextColor
import com.example.gpsmapcamera.utils.setDrawable
import com.example.mycam.models.SettingsModel

class SettingsAdapter(
    val userList: MutableList<SettingsModel>,
    val onItemClick : (String) -> Unit,
    val onMenuClick : (TextView,String) -> Unit,
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private val VIEW_TYPE_HEADING = 0
    private val VIEW_TYPE_GENERAL_ITEM = 1
    private val VIEW_TYPE_FEATURE_ITEM = 2
    private val VIEW_TYPE_ABOUT_ITEM = 3


    override fun getItemCount(): Int = userList.size

    override fun getItemViewType(position: Int): Int {
        return when (userList[position]) {
            is SettingsModel.Heading -> VIEW_TYPE_HEADING
            is SettingsModel.GeneralItem -> VIEW_TYPE_GENERAL_ITEM
            is SettingsModel.FeaturesItem -> VIEW_TYPE_FEATURE_ITEM
            is SettingsModel.AboutItem -> VIEW_TYPE_ABOUT_ITEM
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {

            VIEW_TYPE_HEADING -> {
                val binding = SettingHeadingItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                HeadingViewHolder(binding)
            }

            VIEW_TYPE_GENERAL_ITEM -> {
                val binding = SettingGeneralItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                GeneralViewHolder(binding)
            }

            VIEW_TYPE_FEATURE_ITEM -> {
                val binding = SettingFeatureItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                FeatureViewHolder(binding)
            }

            VIEW_TYPE_ABOUT_ITEM -> {
                val binding = SettingAboutItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )

                AboutViewHolder(binding)
            }


            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val user = userList[position]

        when (user) {
            is SettingsModel.Heading -> (holder as HeadingViewHolder).bind(user)
            is SettingsModel.GeneralItem -> (holder as GeneralViewHolder).bind(user)
            is SettingsModel.FeaturesItem -> (holder as FeatureViewHolder).bind(user)
            is SettingsModel.AboutItem -> (holder as AboutViewHolder).bind(user)

        }
    }

    // ─────────────── ViewHolders ───────────────

    inner class HeadingViewHolder(private val binding: SettingHeadingItemBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(item: SettingsModel.Heading) {
            binding.apply {
                val context=root.context
                headingName.text=item.heading

                if (position==userList.lastIndex)
                {
                    headingName.gravity=Gravity.CENTER
                    headingName.setCompoundDrawableTintAndTextColor(
                       textColorRes =  R.color.grey_g3
                    )
                    headingName.setTypeface(null, Typeface.NORMAL) // normal
                    headingName.setTextSize(
                        TypedValue.COMPLEX_UNIT_PX,
                        context.resources.getDimension(com.intuit.sdp.R.dimen._12sdp)
                    )
                }
            }
        }
    }

    inner class GeneralViewHolder(private val binding: SettingGeneralItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SettingsModel.GeneralItem) {

            binding.apply {
                Glide.with(binding.root.context)
                    .load(item.icon)
                    .into(icon)

                itemName.text=item.title
                selectedOptionName.text=item.selectedOpt
                selectedOptionName.setDrawable(end = item.selectedOptIcon)

                root.setOnClickListener {
                    onItemClick(item.title)
                }
                selectedOptionName.setOnClickListener {
                    onMenuClick(selectedOptionName,item.title)
                }

            }
        }
    }

    class FeatureViewHolder(private val binding: SettingFeatureItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SettingsModel.FeaturesItem) {

            binding.apply {
                Glide.with(binding.root.context)
                    .load(item.icon)
                    .into(icon)
                itemName.text=item.title

            }

        }
    }


    inner class AboutViewHolder(private val binding: SettingAboutItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: SettingsModel.AboutItem,
        ) {

            binding.apply {
                Glide.with(binding.root.context)
                    .load(item.icon)
                    .into(icon)
                itemName.text=item.title

                root.setOnClickListener {
                    onItemClick(item.title)
                }
            }

        }
    }




}

