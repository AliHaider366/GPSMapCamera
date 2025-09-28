package com.example.gpsmapcamera.adapters

import android.content.Context
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.databinding.FilenameRecyclerItem1Binding
import com.example.gpsmapcamera.databinding.FilenameRecyclerItem2Binding
import com.example.gpsmapcamera.enums.FilePart
import com.example.gpsmapcamera.models.FieldItem
import com.example.gpsmapcamera.utils.PrefManager.KEY_24HOURS_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_ADDRESS_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_ADDRESS_LINE1_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_ADDRESS_LINE2_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_ADDRESS_LINE3_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_ADDRESS_LINE4_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_CUSTOM_NAME_1_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_CUSTOM_NAME_1_VALUE
import com.example.gpsmapcamera.utils.PrefManager.KEY_CUSTOM_NAME_2_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_CUSTOM_NAME_2_VALUE
import com.example.gpsmapcamera.utils.PrefManager.KEY_CUSTOM_NAME_3_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_CUSTOM_NAME_3_VALUE
import com.example.gpsmapcamera.utils.PrefManager.KEY_DATE_VALUE_INDEX
import com.example.gpsmapcamera.utils.PrefManager.KEY_DAY_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_DMS_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_FILENAME_PATTERN
import com.example.gpsmapcamera.utils.PrefManager.KEY_FILE_NAME
import com.example.gpsmapcamera.utils.PrefManager.KEY_LAT_LONG_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_LAT_LONG_VALUE_INDEX
import com.example.gpsmapcamera.utils.PrefManager.KEY_NOTE_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_NOTE_VALUE
import com.example.gpsmapcamera.utils.PrefManager.KEY_PLUS_CODE_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_PLUS_CODE_VALUE_INDEX
import com.example.gpsmapcamera.utils.PrefManager.KEY_SEQUENCE_NUMBER_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_SEQUENCE_NUMBER_VALUE
import com.example.gpsmapcamera.utils.PrefManager.KEY_TIME_ZONE_CHECK
import com.example.gpsmapcamera.utils.PrefManager.KEY_TIME_ZONE_VALUE_INDEX
import com.example.gpsmapcamera.utils.PrefManager.getBoolean
import com.example.gpsmapcamera.utils.PrefManager.getString
import com.example.gpsmapcamera.utils.PrefManager.saveBoolean
import com.example.gpsmapcamera.utils.PrefManager.saveInt
import com.example.gpsmapcamera.utils.PrefManager.saveItemOrder
import com.example.gpsmapcamera.utils.PrefManager.savePatternList
import com.example.gpsmapcamera.utils.PrefManager.saveString
import com.example.gpsmapcamera.utils.addTextChanged
import com.example.gpsmapcamera.utils.formatForFile
import com.example.gpsmapcamera.utils.gone
import com.example.gpsmapcamera.utils.setDrawable
import com.example.gpsmapcamera.utils.showToast
import com.example.gpsmapcamera.utils.visible
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

                if (item.isPremium)
                {
                    ///// disable clicks if item is premium
                    /// display premium icon
                    titleTv.isEnabled=false
                    checkbox.isEnabled=false
                }

                when(item.index)
                {

                    0->{
                        checkbox.isChecked=true
                        checkbox1.isChecked=true
                        checkbox3.isChecked=true

                        checkbox.isEnabled=false
                        checkbox1.isEnabled=false
                        checkbox3.isEnabled=false

                        checkbox.alpha=0.5f
                        checkbox1.alpha=0.5f
                        checkbox3.alpha=0.5f

                        checkbox1Tv.text= context.getString(R.string.date_and_time)
                        checkbox2Tv.text= context.getString(R.string.day)
                        checkbox3Tv.text= context.getString(R.string.hour_minute_second)
                        checkbox4Tv.text= context.getString(R.string._24_hours)

                        setupDropCheckItem(checkbox2 = checkbox2, checkbox4 = checkbox4, item = item,
                            checkBox2State = getBoolean(context,KEY_DAY_CHECK), checkBox4State = getBoolean(context,KEY_24HOURS_CHECK) )

                        if(getBoolean(context,KEY_24HOURS_CHECK))
                        {
                            val date24 = Date().formatForFile(true)
                            item.value=date24
                            updateTopText(context)

                        }
                        checkbox2.setOnCheckedChangeListener(null)
                        checkbox2.setOnCheckedChangeListener { _, isChecked ->
                            item.isCheckBox2Checked = isChecked
                            updateTopText(context)
                            saveBoolean(context,KEY_DAY_CHECK,isChecked)
                        }
                        checkbox4.setOnCheckedChangeListener(null)
                        checkbox4.setOnCheckedChangeListener { _, isChecked ->
                            item.isCheckBox4Checked = isChecked
                                val date24 = Date().formatForFile(isChecked)
                                item.value=date24

                            updateTopText(context)
                            saveBoolean(context,KEY_24HOURS_CHECK,isChecked)
                        }
                    }
                    1->{
                        checkbox.isEnabled=true
                        dropdownEdittext.visible()
                        noteEdittext.gone()

                        setupTextItem(checkbox,dropdownEdittext,item,
                            getBoolean(context,KEY_SEQUENCE_NUMBER_CHECK), getString(context,KEY_SEQUENCE_NUMBER_VALUE,"0")
                        )

                        dropdownEdittext.inputType=EditorInfo.TYPE_CLASS_NUMBER
                         dropdownEdittext.addTextChangedListener { text ->
                            if (item.isDropDownVisible==true)
                            {
                                item.value=text.toString()
                                updateTopText(context)
                                saveString(context,KEY_SEQUENCE_NUMBER_VALUE,text.toString())
                            }

                        }

                    }
                    2->{
                        dropdownTvTitle.text= context.getString(R.string.add_custom_name)

                        setupTextItem(checkbox,dropdownEdittext,item,
                            getBoolean(context,KEY_CUSTOM_NAME_1_CHECK), getString(context,KEY_CUSTOM_NAME_1_VALUE)
                        )

                        dropdownEdittext.inputType=EditorInfo.TYPE_CLASS_TEXT
                         dropdownEdittext.addTextChanged { text ->
                            if (item.isDropDownVisible==true)
                            {
                                item.value=text.toString()
                                updateTopText(context)
                                saveString(context,KEY_CUSTOM_NAME_1_VALUE,text.toString())
                            }
                        }
                    }
                    3->{
                        dropdownTvTitle.text= context.getString(R.string.add_custom_name)
                        customName2DropdownEdittext.visible()
                        dropdownEdittext.gone()
                        noteEdittext.gone()

                        setupTextItem(checkbox,customName2DropdownEdittext,item,
                            getBoolean(context,KEY_CUSTOM_NAME_2_CHECK), getString(context,KEY_CUSTOM_NAME_2_VALUE)
                        )

                        customName2DropdownEdittext.inputType=EditorInfo.TYPE_CLASS_TEXT
                        customName2DropdownEdittext.addTextChanged { text ->
                            if (item.isDropDownVisible==true)
                            {
                                item.value=text
                                updateTopText(context)
                                saveString(context,KEY_CUSTOM_NAME_2_VALUE, text)
                            }

                        }
                    }
                    4->{
                        dropdownTvTitle.text= context.getString(R.string.add_custom_name)

                        customName3DropdownEdittext.visible()
                        customName2DropdownEdittext.gone()
                        dropdownEdittext.gone()
                        noteEdittext.gone()

                        setupTextItem(checkbox,customName3DropdownEdittext,item,
                            getBoolean(context,KEY_CUSTOM_NAME_3_CHECK), getString(context,KEY_CUSTOM_NAME_3_VALUE)
                        )

                        customName3DropdownEdittext.inputType=EditorInfo.TYPE_CLASS_TEXT
                        customName3DropdownEdittext.addTextChanged { text ->
                            if (item.isDropDownVisible==true)
                            {
                                item.value= text
                                updateTopText(context)
//                                saveCustomName3Check(context, text)
                                saveString(context,KEY_CUSTOM_NAME_3_VALUE,text.toString())

                            }

                        }
                    }
                    5->{

                        checkbox1Tv.text= context.getString(R.string.line_1)
                        checkbox2Tv.text= context.getString(R.string.line_2)
                        checkbox3Tv.text= context.getString(R.string.line_3)
                        checkbox4Tv.text= context.getString(R.string.line_4)

                        setupDropCheckItem(checkbox=checkbox,checkbox1=checkbox1,checkbox2 = checkbox2, checkbox3 = checkbox3, checkbox4 = checkbox4, item = item,
                            checkBoxState = getBoolean(context,KEY_ADDRESS_CHECK),checkBox1State = getBoolean(context,KEY_ADDRESS_LINE1_CHECK),checkBox2State = getBoolean(context,KEY_ADDRESS_LINE2_CHECK),
                            checkBox3State = getBoolean(context,KEY_ADDRESS_LINE3_CHECK), checkBox4State = getBoolean(context,KEY_ADDRESS_LINE4_CHECK) )

                        checkbox1.setOnClickListener{
                            checkbox1.isChecked= checkbox1.isChecked
                            item.isCheckBox1Checked = checkbox1.isChecked
//                            saveAddressLine1Check(context, checkbox1.isChecked)     /// save in pref
                            saveBoolean(context,KEY_ADDRESS_LINE1_CHECK,checkbox1.isChecked)
                            updateTopText(context)

                        }

                        checkbox2.setOnClickListener{
                            checkbox2.isChecked= checkbox2.isChecked
                            item.isCheckBox2Checked = checkbox2.isChecked
//                            saveAddressLine2Check(context, checkbox2.isChecked)     /// save in pref
                            saveBoolean(context,KEY_ADDRESS_LINE2_CHECK,checkbox2.isChecked)
                            updateTopText(context)
                        }

                        checkbox3.setOnClickListener{
                            checkbox3.isChecked= checkbox3.isChecked
                            item.isCheckBox3Checked = checkbox3.isChecked
//                            saveAddressLine3Check(context, checkbox3.isChecked)     /// save in pref
                            saveBoolean(context,KEY_ADDRESS_LINE3_CHECK,checkbox3.isChecked)
                            updateTopText( context)

                        }

                        checkbox4.setOnClickListener{
                            checkbox4.isChecked= checkbox4.isChecked
                            item.isCheckBox4Checked = checkbox4.isChecked
//                            saveAddressLine4Check(context, checkbox4.isChecked)     /// save in pref
                            saveBoolean(context,KEY_ADDRESS_LINE4_CHECK,checkbox4.isChecked)
                            updateTopText(context)
                        }

                    }
                    6->{
                        row2.visibility=View.GONE
                        checkbox1Tv.text= context.getString(R.string.decimal)
                        checkbox2Tv.text= context.getString(R.string.dms)
                        checkbox1.isChecked=true
                        checkbox1.isEnabled=false
                        checkbox1.alpha=0.5f
                        checkbox1.setOnCheckedChangeListener(null)
                        checkbox1.setOnCheckedChangeListener { _, isChecked ->
//                            item.isCheckBox1Checked = isChecked
//                            checkbox2.isChecked=false
//                            updateTopText()
                        }


                        setupDropCheckItem(checkbox = checkbox,checkbox2 = checkbox2, item = item,
                            checkBoxState = getBoolean(context,KEY_LAT_LONG_CHECK) ,checkBox2State = getBoolean(context,KEY_DMS_CHECK)
                        )

                        checkbox2.setOnCheckedChangeListener(null)
                        checkbox2.setOnCheckedChangeListener { _, isChecked ->
                            item.isCheckBox2Checked = isChecked
//                            checkbox1.isChecked=false
                            updateTopText(context)
//                            saveDMSCheck(context,isChecked)
                            saveBoolean(context,KEY_DMS_CHECK,isChecked)

                        }

                    }
                    7->{
                        checkbox.isChecked= getBoolean(context,KEY_PLUS_CODE_CHECK)
                        item.isChecked= getBoolean(context,KEY_PLUS_CODE_CHECK)
                    }
                    8->{
                        checkbox.isChecked= getBoolean(context,KEY_TIME_ZONE_CHECK)
                        item.isChecked= getBoolean(context,KEY_TIME_ZONE_CHECK)
                    }
                    9->{
                        dropdownTvTitle.text= context.getString(R.string.add_note)
                        dropdownEdittext.gone()
                        noteEdittext.visible()
                        setupTextItem(checkbox,noteEdittext,item, getBoolean(context,KEY_NOTE_CHECK),
                            getString(context,KEY_NOTE_VALUE)
                        )

                        noteEdittext.inputType=EditorInfo.TYPE_CLASS_TEXT
                        noteEdittext.addTextChanged  { text ->
                            if (item.isDropDownVisible==true)
                            {
                                item.value= text
                                updateTopText(context)
//                                saveNoteValue(context, text)
                                saveString(context,KEY_NOTE_VALUE,text)
                            }
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

                checkbox.setOnClickListener{
                    checkbox.isChecked= checkbox.isChecked
                    item.isChecked = checkbox.isChecked
//                    val date = Date().formatForFile()
                    updateTopText(context)
                    when(item.index) {

                        1 -> {

                            if (dropdownEdittext.text.isNotEmpty())
                            {
                                saveBoolean(context,KEY_SEQUENCE_NUMBER_CHECK,checkbox.isChecked)
                                saveString(context,KEY_SEQUENCE_NUMBER_VALUE,dropdownEdittext.text.toString())
                            }
                            else {
                                context.showToast(context.getString(R.string.please_enter_sequence_number))
                                return@setOnClickListener
                            }

                        }
                        2->{

                            if (dropdownEdittext.text.isNotEmpty())
                            {
                                saveBoolean(context,KEY_CUSTOM_NAME_1_CHECK,checkbox.isChecked)
                                saveString(context,KEY_CUSTOM_NAME_1_VALUE,dropdownEdittext.text.toString())
                            }
                            else {
                                context.showToast(context.getString(R.string.please_enter_custom_name))
                                return@setOnClickListener
                            }
                        }
                        3->{

                            if (customName2DropdownEdittext.text.isNotEmpty())
                            {
                                saveBoolean(context,KEY_CUSTOM_NAME_2_CHECK,checkbox.isChecked)
                                saveString(context,KEY_CUSTOM_NAME_2_VALUE,customName2DropdownEdittext.text.toString())
                            }
                            else {
                                context.showToast(context.getString(R.string.please_enter_custom_name))
                                return@setOnClickListener
                            }
                        }
                        4->{

                            if (customName3DropdownEdittext.text.isNotEmpty())
                            {
                                saveBoolean(context,KEY_CUSTOM_NAME_3_CHECK,checkbox.isChecked)
                                saveString(context,KEY_CUSTOM_NAME_3_VALUE,customName3DropdownEdittext.text.toString())
                            }
                            else {
                                context.showToast(context.getString(R.string.please_enter_custom_name))
                                return@setOnClickListener
                            }
                        }
                        5->{
                            saveBoolean(context,KEY_ADDRESS_CHECK,checkbox.isChecked)

                        }
                        6->{
                            saveBoolean(context,KEY_LAT_LONG_CHECK,checkbox.isChecked)
                        }
                        7-> {
                            saveBoolean(context,KEY_PLUS_CODE_CHECK,checkbox.isChecked)
                        }
                        8->{
                            saveBoolean(context,KEY_TIME_ZONE_CHECK,checkbox.isChecked)
                        }
                        9->{

                            saveBoolean(context,KEY_NOTE_CHECK,checkbox.isChecked)
                        }
                    }
                }

                reorderBtn.setOnTouchListener { _, event ->
                    if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                        dragStartListener(this@ContentViewHolder)
                    }
                    false
                }

                updateTopText(context)
            }


        }
    }

    fun setupTextItem(
        checkbox: CheckBox,
        dropdownEditText: EditText,
        item: FieldItem,
        checkBoxState: Boolean,
        editTextValue:  String
    ) {
        checkbox.isChecked= checkBoxState      /// set init value
        dropdownEditText.setText(editTextValue)
        item.value=editTextValue
        item.isChecked = checkBoxState
    }
    fun setupDropCheckItem(
        checkbox: CheckBox?=null,
        checkbox1: CheckBox?=null,
        checkbox2: CheckBox,
        checkbox3: CheckBox?=null,
        checkbox4: CheckBox?=null,
        item: FieldItem,
        checkBoxState: Boolean?=null,
        checkBox2State: Boolean,
        checkBox4State: Boolean?=null,
        checkBox1State: Boolean?=null,
        checkBox3State: Boolean?=null,
    ) {
        checkbox2.isChecked= checkBox2State
        item.isCheckBox2Checked=checkBox2State
        if (checkBox4State != null && checkbox4 != null) {
            item.isCheckBox4Checked= checkBox4State
                checkbox4.isChecked= checkBox4State
        }
        if (checkBoxState != null && checkbox != null) {
            item.isChecked = checkBoxState
            checkbox.isChecked= checkBoxState
        }

        if (checkBox1State != null  && checkbox1 != null) {
            checkbox1.isChecked= checkBox1State
            item.isCheckBox1Checked=checkBox1State
        }

        if (checkBox3State != null  && checkbox3 != null) {
            checkbox3.isChecked= checkBox3State
            item.isCheckBox3Checked=checkBox3State
        }
    }

    // Called when dragging an item
    fun onItemMove(fromPosition: Int, toPosition: Int,context: Context) {
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
        updateTopText(context)

        saveItemOrder(items, context)
    }

    private fun updateTopText2(context: Context) {

    /*    val selectedText = items
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
                else if(it.address != null)
                {
                    val selected = buildList {
                        if (it.isCheckBox1Checked) add(it.address.line1)
                        if (it.isCheckBox2Checked) add(it.address.line2)
                        if (it.isCheckBox3Checked) add(it.address.line3)
                        if (it.isCheckBox4Checked) add(it.address.line4)
                    }
                    if (selected.isEmpty()) {
                        it.value
                    } else {
                        selected.joinToString("_")
                    }

                }
                else
                {
                    it.value
                }

            }*/

        val selectedParts = items
            .filter { it.isChecked }
            .mapIndexed { joinedIndex, it ->

                val value = when {
                    it.isCheckBox2Checked && it.day.isNotEmpty() -> {
                        "${it.value}_${it.day}"
//                        Log.d("SelectedText", "${it.day} is at joined index ${joinedIndex}")
                    }

                    it.isCheckBox2Checked && it.latLongDMS.isNotEmpty() ->
                        it.latLongDMS

                    it.address != null -> {
                        val selected = buildList<Pair<String, Int>> {
                            if (it.isCheckBox1Checked) add(it.address.line1 to 0)
                            if (it.isCheckBox2Checked) add(it.address.line2 to 1)
                            if (it.isCheckBox3Checked) add(it.address.line3 to 2)
                            if (it.isCheckBox4Checked) add(it.address.line4 to 3)
                        }

                        if (selected.isEmpty()) it.value
                        else {
                            selected.joinToString("_") { (text, index) ->
                                text
                            }
                        }
//                        else selected.joinToString("_")
                    }

                    else -> {
                        it.value
                    }
                }

                // 🔹 log the value and its index in the final joined string
                Log.d("SelectedText", "${it.value} is at joined index $joinedIndex")
                when(it.index)
                {
                    0->{
                        saveInt(context,KEY_DATE_VALUE_INDEX,joinedIndex)
                    }
                    6->{
                        saveInt(context,KEY_LAT_LONG_VALUE_INDEX,joinedIndex)
                    }
                    7->{
                        saveInt(context,KEY_PLUS_CODE_VALUE_INDEX,joinedIndex)
                    }
                    8->{
                        saveInt(context,KEY_TIME_ZONE_VALUE_INDEX,joinedIndex)
                    }
                }
                value
            }

        val selectedText = selectedParts.joinToString("_", postfix = ".jpg")
        onSelectionChanged(selectedText)
    }

    private fun updateTopText(context: Context) {
        val parts = mutableListOf<Pair<FilePart, String>>()

        items.filter { it.isChecked }.forEach { item ->
            when {
                item.isCheckBox2Checked && item.day.isNotEmpty() -> {
                    parts += FilePart.DATETIME to item.value
                    parts += FilePart.DAY to item.day
                }

                item.isCheckBox2Checked && item.latLongDMS.isNotEmpty() -> {
                    parts += FilePart.LATLONG to item.latLongDMS
                }

                item.address != null -> {
                    if (item.isCheckBox1Checked) {
                        parts += FilePart.ADDRESS_LINE1 to item.address.line1
                    }
                    if (item.isCheckBox2Checked) {
                        parts += FilePart.ADDRESS_LINE2 to item.address.line2
                    }
                    if (item.isCheckBox3Checked ) {
                        parts += FilePart.ADDRESS_LINE3 to item.address.line3
                    }
                    if (item.isCheckBox4Checked) {
                        parts += FilePart.ADDRESS_LINE4 to item.address.line4
                    }
                }

                else -> {
                    when(item.index)
                    {
                        0-> parts += FilePart.DATETIME to item.value
                        1-> parts += FilePart.SEQ to item.value
                        2-> parts += FilePart.CUSTOM_NAME1 to item.value
                        3-> parts += FilePart.CUSTOM_NAME2 to item.value
                        4-> parts += FilePart.CUSTOM_NAME3 to item.value
                        6-> parts += FilePart.LATLONG to item.value
                        7-> parts += FilePart.PLUSCODE to item.value
                        8-> parts += FilePart.TIMEZONE to item.value
                        9-> parts += FilePart.NOTE to item.value
                    }

                }
            }
        }

        //  Save the pattern order for later
        val pattern = parts.map { it.first.name } // ["DATETIME", "DAY", "SEQ", "ADDRESS"]

        val joinedList = pattern.joinToString(",")   // convert list to string "DATETIME,DAY,SEQ,ADDRESS_LINE1,LATLONG"
        saveString(context,KEY_FILENAME_PATTERN,joinedList)

        //  Build final filename
        val selectedText = parts.joinToString("_") { it.second } + ".jpg"
        saveString(context, KEY_FILE_NAME, selectedText)
//        Log.d("FinalFileName", selectedText)
//        Log.d("FinalFileName", pattern.toString())
        onSelectionChanged(selectedText)

    }
}

