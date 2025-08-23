package com.example.gpsmapcamera.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.activities.FieldItem
import com.example.gpsmapcamera.databinding.FilenameRecyclerItem1Binding
import com.example.gpsmapcamera.databinding.FilenameRecyclerItem2Binding
import com.example.gpsmapcamera.utils.formatForFile
import com.example.gpsmapcamera.utils.setDrawable
import java.util.Date

class FileNameAdapter(
    private val items:  MutableList<FieldItem>,
    private val onSelectionChanged: (String) -> Unit,
    private val dragStartListener: (RecyclerView.ViewHolder) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_TEXTVIEW = 0
        private const val TYPE_CONTENT = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when(position) {
            0 -> TYPE_CONTENT
           1 -> TYPE_CONTENT
            else -> {1}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_TEXTVIEW -> {
                val binding = FilenameRecyclerItem1Binding.inflate(LayoutInflater.from(parent.context), parent, false)
                TextviewViewHolder(binding)
            }
            TYPE_CONTENT -> {
                val binding = FilenameRecyclerItem2Binding.inflate(LayoutInflater.from(parent.context), parent, false)
                ContentViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is TextviewViewHolder -> holder.bind(item)
            is ContentViewHolder -> holder.bind(item)
        }
    }

    override fun getItemCount() = items.size

   inner class TextviewViewHolder(private val binding: FilenameRecyclerItem1Binding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FieldItem) {
            binding.filenameTv.text = item.name
        }
    }

    var prePos=-1
    var pos=-1
   inner class ContentViewHolder(private val binding: FilenameRecyclerItem2Binding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FieldItem) {

            binding.apply {
                val context=binding.root.context
                titleTv.text = item.name
                checkbox.isChecked=item.isChecked
                updateTopText()

                if (item.isPremium)
                {
                    ///// disable clicks if item is premium
                    /// display premium icon
                    titleTv.isEnabled=false
                    checkbox.isEnabled=false
                }

                when(position)
                {
                    0->{
                        checkbox.isChecked=true
                        checkbox1.isChecked=true
                        checkbox3.isChecked=true

                        checkbox.isEnabled=false
                        checkbox1.isEnabled=false
                        checkbox3.isEnabled=false

                        checkbox1Tv.text= context.getString(R.string.date_and_time)
                        checkbox2Tv.text= context.getString(R.string.day)
                        checkbox3Tv.text= context.getString(R.string.hour_minute_second)
                        checkbox4Tv.text= context.getString(R.string._24_hours)

                        checkbox2.setOnCheckedChangeListener(null)
                        checkbox2.setOnCheckedChangeListener { _, isChecked ->
                            item.isCheckBox2Checked = isChecked
                            updateTopText()
                        }
                        checkbox4.setOnCheckedChangeListener(null)
                        checkbox4.setOnCheckedChangeListener { _, isChecked ->
                            item.isCheckBox4Checked = isChecked
                                val date24 = Date().formatForFile(isChecked)
                                item.value=date24

                            updateTopText()
                        }
                    }
                    1->{
                        checkbox.isEnabled=true
//                        checkbox.isChecked=false
                        dropdownEdittext.inputType=EditorInfo.TYPE_CLASS_NUMBER
                        dropdownEdittext.addTextChangedListener { text ->
                            item.value=text.toString()
                            updateTopText()

                        }

                    }
                    2->{
                        dropdownTvTitle.text= context.getString(R.string.add_custom_name)

                        dropdownEdittext.inputType=EditorInfo.TYPE_CLASS_TEXT
                        dropdownEdittext.addTextChangedListener { text ->
                            item.value=text.toString()
                            updateTopText()

                        }
                    }
                    3->{
                        dropdownTvTitle.text= context.getString(R.string.add_custom_name)

                        dropdownEdittext.addTextChangedListener { text ->
                            item.value=text.toString()
                            updateTopText()

                        }
                    }
                    4->{
                        dropdownTvTitle.text= context.getString(R.string.add_custom_name)

                        dropdownEdittext.addTextChangedListener { text ->
                            item.value=text.toString()
                            updateTopText()

                        }
                    }
                    5->{

                        checkbox1Tv.text= context.getString(R.string.line_1)
                        checkbox2Tv.text= context.getString(R.string.line_2)
                        checkbox3Tv.text= context.getString(R.string.line_3)
                        checkbox4Tv.text= context.getString(R.string.line_4)
                    }
                    6->{
                        row2.visibility=View.GONE
                        checkbox1Tv.text= context.getString(R.string.decimal)
                        checkbox2Tv.text= context.getString(R.string.dms)
                        checkbox1.isChecked=true
                        checkbox1.isEnabled=false
                        checkbox1.setOnCheckedChangeListener(null)
                        checkbox1.setOnCheckedChangeListener { _, isChecked ->
//                            item.isCheckBox1Checked = isChecked
//                            checkbox2.isChecked=false
//                            updateTopText()
                        }

                        checkbox2.setOnCheckedChangeListener(null)
                        checkbox2.setOnCheckedChangeListener { _, isChecked ->
                            item.isCheckBox2Checked = isChecked
//                            checkbox1.isChecked=false
                            updateTopText()
                        }

                    }

                    9->{
                        dropdownTvTitle.text= context.getString(R.string.add_note)
                        dropdownEdittext.addTextChangedListener { text ->
                            item.value=text.toString()
                            updateTopText()
                        }
                    }
                }

                if (item.isDropCheck==null)         /// hide dropdown icon and disable click
                {
                    titleTv.isEnabled=false
                    titleTv.setDrawable(end = null)
                    titleTv.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                }
                else
                {
                    titleTv.isEnabled=true
                    titleTv.setDrawable(end = R.drawable.dropdown_open_ic)
                }


                if (position==pos)      ////on click show dropdown and hide already opened
                {
                    if (item.isDropCheck == true)
                    {
                        if(item.isDropDownVisible==true) {
                            dropdown.visibility = View.GONE
                            item.isDropDownVisible=false
                            titleTv.setDrawable(end = R.drawable.dropdown_open_ic)
                        }
                        else {
                            dropdown.visibility = View.VISIBLE
                            item.isDropDownVisible=true
                            titleTv.setDrawable(end =R.drawable.dropdown_close_ic)
                        }
                    }
                    else if (item.isDropCheck == false)
                    {
                        if(item.isDropDownVisible==true) {
                            dropdownTv.visibility = View.GONE
                            item.isDropDownVisible=false
                            titleTv.setDrawable(end =R.drawable.dropdown_open_ic)

                        }
                        else {
                            dropdownTv.visibility = View.VISIBLE
                            item.isDropDownVisible=true
                            titleTv.setDrawable(end =R.drawable.dropdown_close_ic)

                        }
                    }
                }
                else
                {
                    item.isDropDownVisible=false
                    dropdownTv.visibility = View.GONE
                    dropdown.visibility = View.GONE
                }

                titleTv.setOnClickListener{

                    pos=position
                    notifyDataSetChanged()
                   /* if (item.isDropCheck == true)
                    {
                        if(dropdown.isVisible) dropdown.visibility= View.GONE
                        else dropdown.visibility= View.VISIBLE
                    }
                    else if (item.isDropCheck == false)
                    {
                        if(dropdownTv.isVisible) dropdownTv.visibility= View.GONE
                        else dropdownTv.visibility= View.VISIBLE
                    }*/
                  /*  prePos=pos
                    pos=position
                    if (pos!=prePos)
                    notifyDataSetChanged()
                    else
                    {
                        if (item.isDropCheck == true)
                        {
                            if(dropdown.isVisible) dropdown.visibility= View.GONE
                            else dropdown.visibility= View.VISIBLE
                        }
                        else if (item.isDropCheck == false)
                        {
                            if(dropdownTv.isVisible) dropdownTv.visibility= View.GONE
                            else dropdownTv.visibility= View.VISIBLE
                        }
                    }*/

                }
                checkbox.setOnCheckedChangeListener(null)
     /*           checkbox.setOnCheckedChangeListener { _, isChecked ->

                    item.isChecked = isChecked
                    val date = Date().formatForFile()
                    updateTopText()
                }*/
                checkbox.setOnClickListener{
                    checkbox.isChecked= checkbox.isChecked
                    item.isChecked = checkbox.isChecked
//                    val date = Date().formatForFile()
                    updateTopText()
                }

                reorderBtn.setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        dragStartListener(this@ContentViewHolder)
                    }
                    false
                }
            }
        }
    }

    // Called when dragging an item
    fun onItemMove(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                items[i] = items.set(i + 1, items[i])
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                items[i] = items.set(i - 1, items[i])
            }
        }
        notifyItemMoved(fromPosition, toPosition)
        updateTopText()
    }

    private fun updateTopText() {
        val selectedText = items
            .filter { (it.isChecked) }
            .joinToString("_", postfix = ".jpg") {

                if (it.isCheckBox2Checked && it.day.isNotEmpty())
                {
                    "${it.value}_${it.day}"
                }
                else if(it.isCheckBox2Checked && it.latLongDMS.isNotEmpty())
                {
                    it.latLongDMS
                }
                else
                {
                    it.value
                }

            }
        onSelectionChanged(selectedText)
    }
}