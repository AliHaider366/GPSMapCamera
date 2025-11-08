package com.example.gpsmapcamera.cameraHelper

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.Window
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gpsmapcamera.R
import com.example.gpsmapcamera.adapters.EditorOptionAdapter
import com.example.gpsmapcamera.colorPicker.ColorPickerDialog
import com.example.gpsmapcamera.databinding.DialogTextEditorBinding
import com.example.gpsmapcamera.utils.PrefManager
import com.example.gpsmapcamera.utils.setTintColor

class TextEditorDialog(context: Context) : Dialog(context) {

    private var onTextSaved: ((String,Int,Int,Typeface?,Boolean,Boolean,Boolean,Int) -> Unit)? = null
    private lateinit var binding: DialogTextEditorBinding
    private var initialText: String? = null
    private var initialTextColor: Int? = null
    private var initialBgColor: Int? = null
    private var initialTypeface: Typeface? = null
    private var initialIsBold: Boolean = false
    private var initialIsItalic: Boolean = false
    private var initialIsUnderline: Boolean = false
    private var initialGravity: Int = android.view.Gravity.CENTER

    constructor(context: Context, initialText: String?, initialTextColor: Int?, initialBgColor: Int?, initialTypeface: Typeface?,
                initialIsBold: Boolean = false, initialIsItalic: Boolean = false, initialIsUnderline: Boolean = false, initialGravity: Int = android.view.Gravity.CENTER) : this(context) {
        this.initialText = initialText
        this.initialTextColor = initialTextColor
        this.initialBgColor = initialBgColor
        this.initialTypeface = initialTypeface
        this.initialIsBold = initialIsBold
        this.initialIsItalic = initialIsItalic
        this.initialIsUnderline = initialIsUnderline
        this.initialGravity = initialGravity
        // Setup views after initial values are set
        setupViews()
    }

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        binding = DialogTextEditorBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Make dialog transparent
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(
            android.view.WindowManager.LayoutParams.MATCH_PARENT,
            android.view.WindowManager.LayoutParams.MATCH_PARENT
        )

        // Only setup views if this is the primary constructor (no initial values)
        // Secondary constructor will call setupViews() after setting values
        if (initialText == null && initialTextColor == null && initialBgColor == null && initialTypeface == null) {
            setupViews()
        }
    }

    private fun setupViews()=binding.apply {

        // Apply initial values if provided
        initialText?.let { textEditor.setText(it) }
        initialTextColor?.let { textEditor.setTextColor(it) }
        initialBgColor?.let {
            if (it == Color.TRANSPARENT) {
                textEditor.setBackgroundColor(Color.TRANSPARENT)
            } else {
                textEditor.setBackgroundColor(it)
            }
        }
        // Style state - use initial values if provided, otherwise extract from initialTypeface
        var isBold = initialIsBold
        var isItalic = initialIsItalic
        var isUnderline = initialIsUnderline

        // Apply initial typeface and extract styles
        initialTypeface?.let { typeface ->
            textEditor.typeface = typeface
            // Extract bold and italic from the initial typeface if not explicitly provided
            if (!initialIsBold && !initialIsItalic) {
                val typefaceStyle = typeface.style
                isBold = (typefaceStyle and Typeface.BOLD) != 0
                isItalic = (typefaceStyle and Typeface.ITALIC) != 0
            }
        }

        // Apply typeface state after setting initial values
//        applyTypefaceFromState()

        // Apply initial gravity
        textEditor.gravity = initialGravity

        // Apply initial underline
        if (initialIsUnderline) {
            textEditor.paintFlags = textEditor.paintFlags or Paint.UNDERLINE_TEXT_FLAG
        }

        optionsRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // Font resource IDs from res/font folder
        val fontResourceIds = listOf(
            R.font.montserrat_regular,
            R.font.lora_regular,
            R.font.poppins,
            R.font.playfair_regular,
            R.font.roboto_mono_regular,
            R.font.short_stack,
            R.font.single_day,
            R.font.skranji,
            R.font.source_serif_pro_semibold,
            R.font.squada_one,
            R.font.stylish,
        )

        // Store currently selected font index
        var currentFontIndex: Int? = null

        val adapter = EditorOptionAdapter { item,pos ->
//            applyOption(item, tabBgColor.backgroundTintList == context.getColorStateList(R.color.blue))
            when (item) {
                is EditorOption.ColorOption -> {
                    textEditor.setTextColor(item.color)
                }
                is EditorOption.BGColorOption->{
                    when(pos)
                    {
                        0->{
                            textEditor.setBackgroundColor(Color.TRANSPARENT)
                        }
                        else -> {
                            textEditor.setBackgroundColor(item.color)
                        }
                    }
                }
                is EditorOption.FontOption -> {
//                    textEditor.typeface = item.typeface
                    // Store the font index when selected
                    currentFontIndex = pos
                    // Apply the font with current bold/italic style from state variables
                    val style = when {
                        isBold && isItalic -> Typeface.BOLD_ITALIC
                        isBold -> Typeface.BOLD
                        isItalic -> Typeface.ITALIC
                        else -> Typeface.NORMAL
                    }
                    textEditor.setTypeface(item.typeface, style)
//                    textEditor.setText(item.sample.ifBlank { textEditor.text })
                }
            }
        }
        optionsRecycler.adapter = adapter


//        val baseTypeface = textEditor.typeface

        fun applyTypefaceFromState() {
            // Get the current typeface from textEditor to preserve the selected font family
            val currentTypeface = textEditor.typeface ?: Typeface.DEFAULT
            // Extract the base typeface (without style) from current typeface
            val currentBaseTypeface = Typeface.create(currentTypeface, Typeface.NORMAL)

            val style = when {
                isBold && isItalic -> Typeface.BOLD_ITALIC
                isBold -> Typeface.BOLD
                isItalic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }
            textEditor.setTypeface(currentBaseTypeface, style)
        }

        fun setStyleActive(view: ImageView, active: Boolean) {
//            view.setTextColor(context.getColor(if (active) R.color.blue else R.color.white))
            view.setTintColor((if (active) R.color.blue else R.color.white))
        }

        // Bold toggle
        btnBold.setOnClickListener {
            isBold = !isBold
            setStyleActive(btnBold, isBold)
            applyTypefaceFromState()

        }
        // Italic toggle
        btnItalic.setOnClickListener {
            isItalic = !isItalic
            setStyleActive(btnItalic, isItalic)
            applyTypefaceFromState()
        }
        // Underline toggle
        btnUnderline.setOnClickListener {
            isUnderline = !isUnderline
            setStyleActive(btnUnderline, isUnderline)
            textEditor.paintFlags = if (isUnderline) {
                textEditor.paintFlags or Paint.UNDERLINE_TEXT_FLAG
            } else {
                textEditor.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
            }
        }

        // Alignment selection
        fun selectAlign(selected: ImageView) {
            alignLeft.setTintColor(R.color.white)
            alignCenter.setTintColor(R.color.white)
            alignRight.setTintColor(R.color.white)
            selected.setTintColor(R.color.blue)
        }
        alignLeft.setOnClickListener {
            textEditor.gravity = android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL
            selectAlign(alignLeft)
        }
        alignCenter.setOnClickListener {
            textEditor.gravity = android.view.Gravity.CENTER
            selectAlign(alignCenter)
        }
        alignRight.setOnClickListener {
            textEditor.gravity = android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL
            selectAlign(alignRight)
        }

        // Handle cancel button
        cancelBtn.setOnClickListener {
            dismiss()
        }

        // Handle save button
        saveBtn.setOnClickListener {
            val text = textEditor.text?.toString() ?: ""
            val textBgColor = (textEditor.background as? ColorDrawable)?.color ?: R.color.transparent
            val textColor = textEditor.currentTextColor
            // Extract style information
            val currentTypeface = textEditor.typeface
            val currentGravity = textEditor.gravity
            val hasUnderline = (textEditor.paintFlags and Paint.UNDERLINE_TEXT_FLAG) != 0
            // Extract bold and italic from typeface
            val typefaceStyle = currentTypeface?.style ?: Typeface.NORMAL
            val isBold = (typefaceStyle and Typeface.BOLD) != 0
            val isItalic = (typefaceStyle and Typeface.ITALIC) != 0
            // Get base typeface (without style)
//            val savedBaseTypeface = currentTypeface?.let { Typeface.create(it, Typeface.NORMAL) }
            // Get base typeface (without style) - use the currently selected font if available
            val savedBaseTypeface = if (currentFontIndex != null && currentFontIndex!! >= 0 && currentFontIndex!! < fontResourceIds.size) {
                try {
                    ResourcesCompat.getFont(context, fontResourceIds[currentFontIndex!!])
                } catch (e: Exception) {
                    currentTypeface?.let { Typeface.create(it, Typeface.NORMAL) }
                }
            } else {
                currentTypeface?.let { Typeface.create(it, Typeface.NORMAL) }
            }

            // Save the current font index for future reference
            if (currentFontIndex != null) {
                PrefManager.saveInt(context, PrefManager.KEY_TYPE_FONT, currentFontIndex!!)
            }
            if (text.isNotBlank()) {
                onTextSaved?.invoke(text, textColor, textBgColor, savedBaseTypeface, isBold, isItalic, hasUnderline, currentGravity)
                dismiss()
            } else {
                Toast.makeText(context, "Please enter some text", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle tab clicks and update dataset
        val tabs = listOf(tabColor, tabBgColor, tabFonts, tabStyle, tabAlignment)
        var selectedTabIndex = 0 // 0 = text color, 1 = bg color, 2 = fonts, etc.
        fun selectTab(selected: TextView) {
            tabs.forEach { it.backgroundTintList = context.getColorStateList(R.color.transparent)
                it.setTextColor(context.getColorStateList(R.color.white))
            }
//            selected.backgroundTintList = context.getColorStateList(R.color.blue)
            selected.setTextColor(context.getColorStateList(R.color.blue))
        }

      /*  fun refreshFontsSample() = listOf(
            EditorOption.FontOption(textEditor.text?.toString().orEmpty().ifBlank { "Text" }, Typeface.SANS_SERIF),
            EditorOption.FontOption(textEditor.text?.toString().orEmpty().ifBlank { "Text" }, Typeface.SERIF),
            EditorOption.FontOption(textEditor.text?.toString().orEmpty().ifBlank { "Text" }, Typeface.MONOSPACE)
        )*/
      fun refreshFontsSample(): List<EditorOption.FontOption> {
          val sampleText = textEditor.text?.toString().orEmpty().ifBlank { "Text" }
          return fontResourceIds.mapIndexed { index, fontResId ->
              val typeface = try {
                  ResourcesCompat.getFont(context, fontResId) ?: Typeface.DEFAULT
              } catch (e: Exception) {
                  Typeface.DEFAULT
              }
              EditorOption.FontOption(sampleText, typeface, fontResId)
          }
      }
        val colors = listOf(
            Color.parseColor("#666666"), Color.parseColor("#000066"), Color.parseColor("#FFFF00"),
            Color.parseColor("#66FF00"), Color.parseColor("#FFFFFF"), Color.parseColor("#FF0000"),
            Color.parseColor("#00FF00"), Color.parseColor("#0000FF"), Color.parseColor("#FF00FF"),
            Color.parseColor("#00FFFF")
        )

        fun buildColorOptions() = colors.map { EditorOption.ColorOption(it) }
        fun buildBGColorOptions() = colors.map { EditorOption.BGColorOption(it) }

        // Find matching color indices
        fun findMatchingColorIndex(targetColor: Int): Int? {
            return colors.indexOfFirst {
                Math.abs(Color.red(it) - Color.red(targetColor)) < 5 &&
                        Math.abs(Color.green(it) - Color.green(targetColor)) < 5 &&
                        Math.abs(Color.blue(it) - Color.blue(targetColor)) < 5
            }.takeIf { it >= 0 }
        }
/*
        fun findMatchingFontIndex(targetTypeface: Typeface?): Int? {
            if (targetTypeface == null) return null

            // Normalize to base (remove bold/italic)
            val base = Typeface.create(targetTypeface, Typeface.NORMAL)

            // Try matching directly (sometimes works)
            when (base) {
                Typeface.SANS_SERIF -> return 0
                Typeface.SERIF -> return 1
                Typeface.MONOSPACE -> return 2
            }

            return null
        }
*/
        fun findMatchingFontIndex(targetTypeface: Typeface?): Int? {
    if (targetTypeface == null) return null

    // Get base typeface (without style) for comparison
    val baseTargetTypeface = Typeface.create(targetTypeface, Typeface.NORMAL)

    // Strategy 1: Compare by object reference (most reliable if same instance)
    fontResourceIds.forEachIndexed { index, fontResId ->
        try {
            val font = ResourcesCompat.getFont(context, fontResId)
            if (font != null) {
                val baseFont = Typeface.create(font, Typeface.NORMAL)
                // Direct object comparison
                if (baseFont === baseTargetTypeface) {
                    return index
                }
            }
        } catch (e: Exception) {
            // Continue to next font
        }
    }

    // Strategy 2: Compare by creating new instances and checking if they're equivalent
    fontResourceIds.forEachIndexed { index, fontResId ->
        try {
            val font = ResourcesCompat.getFont(context, fontResId)
            if (font != null) {
                val baseFont = Typeface.create(font, Typeface.NORMAL)
                // Try creating new instances from both and compare
                val newTarget = Typeface.create(baseTargetTypeface, Typeface.NORMAL)
                val newFont = Typeface.create(baseFont, Typeface.NORMAL)
                if (newFont === newTarget || newFont == newTarget) {
                    return index
                }
            }
        } catch (e: Exception) {
            // Continue to next font
        }
    }

    // Strategy 3: Compare by string representation (less reliable but sometimes works)
    val targetString = baseTargetTypeface.toString()
    fontResourceIds.forEachIndexed { index, fontResId ->
        try {
            val font = ResourcesCompat.getFont(context, fontResId)
            if (font != null) {
                val baseFont = Typeface.create(font, Typeface.NORMAL)
                if (baseFont.toString() == targetString) {
                    return index
                }
            }
        } catch (e: Exception) {
            // Continue to next font
        }
    }

    // If all matching strategies fail, return null
    // The code will fall back to using saved preference or default
    return null
}

        // Fallback matching strategy: Try applying each font and see if it produces a similar result
        fun findMatchingFontIndexByApplying(targetTypeface: Typeface?): Int? {
            if (targetTypeface == null) return null

            val targetBase = Typeface.create(targetTypeface, Typeface.NORMAL)

            // Try each font and see if applying it results in the same or similar typeface
            fontResourceIds.forEachIndexed { index, fontResId ->
                try {
                    val font = ResourcesCompat.getFont(context, fontResId)
                    if (font != null) {
                        val fontBase = Typeface.create(font, Typeface.NORMAL)
                        // Try comparing the actual font files or characteristics
                        // Since direct comparison might not work, we check if they're from the same source
                        // by checking if creating them produces the same result
                        val test1 = Typeface.create(targetBase, Typeface.NORMAL)
                        val test2 = Typeface.create(fontBase, Typeface.NORMAL)

                        // If both create the same typeface when normalized, they might be the same
                        if (test1 === test2 || test1 == test2) {
                            return index
                        }
                    }
                } catch (e: Exception) {
                    // Continue to next font
                }
            }

            return null
        }

        // Set initial selected indices in adapter
        val textColorIndex = initialTextColor?.let { findMatchingColorIndex(it) }
        val bgColorIndex = initialBgColor?.let {
            if (it == Color.TRANSPARENT) {
                0 // Position 0 is treated as transparent in the onClick handler
            } else {
                // For BG colors, position 0 is transparent, so colors start at position 1
                findMatchingColorIndex(it)?.let { it  } ?: 0
            }
        }
//        val fontIndex = findMatchingFontIndex(initialTypeface ?: textEditor.typeface)
        // Get current typeface and find matching font index
        var fontIndex = findMatchingFontIndex(initialTypeface ?: textEditor.typeface)

        // If we couldn't find a match and there's a stored font index from preferences, use that
        if (fontIndex == null) {
            val savedFontIndex = PrefManager.getInt(context, PrefManager.KEY_TYPE_FONT, -1)
            if (savedFontIndex >= 0 && savedFontIndex < fontResourceIds.size) {
                fontIndex = savedFontIndex
            } else {
                // Last resort: try to match by applying fonts and checking if they produce similar results
                // This is a fallback when all other methods fail
                if (initialTypeface != null) {
                    fontIndex = findMatchingFontIndexByApplying(initialTypeface)
                }
                // If still no match, default to first font (index 0)
                if (fontIndex == null && fontResourceIds.isNotEmpty()) {
                    fontIndex = 0
                }
            }
        }

        // Store the matched font index
        currentFontIndex = fontIndex

        // Apply the matched font with correct style if we found a match and have initial values
        // This ensures the font matches what's displayed and what's selected in RecyclerView
        if (fontIndex != null && fontIndex >= 0 && fontIndex < fontResourceIds.size) {
            try {
                val matchedFont = ResourcesCompat.getFont(context, fontResourceIds[fontIndex])
                if (matchedFont != null) {
                    val style = when {
                        initialIsBold && initialIsItalic -> Typeface.BOLD_ITALIC
                        initialIsBold -> Typeface.BOLD
                        initialIsItalic -> Typeface.ITALIC
                        else -> Typeface.NORMAL
                    }
                    // Only apply if we have an initial typeface (editing existing text)
                    // This ensures the RecyclerView selection matches the displayed font
                    if (initialTypeface != null) {
                        textEditor.setTypeface(matchedFont, style)
                    }
                }
            } catch (e: Exception) {
                // Font loading failed, keep existing font
            }
        }

        adapter.setInitialSelectedIndices(textColorIndex, bgColorIndex, fontIndex)
        fun showRecycler(showRecycler: Boolean,showColorPicker: Boolean=true) {
//            optionsRecycler.visibility = if (show) View.VISIBLE else View.GONE
            colorPalletView.visibility = if (showRecycler) View.VISIBLE else View.GONE
            colorPickerBtn.visibility= if (showColorPicker) View.VISIBLE else View.GONE
        }
        fun showStyle(show: Boolean) { styleRow.visibility = if (show) View.VISIBLE else View.GONE }
        fun showAlign(show: Boolean) { alignmentRow.visibility = if (show) View.VISIBLE else View.GONE }

        tabColor.setOnClickListener { selectedTabIndex = 0; selectTab(tabColor); showRecycler(true,); showStyle(false); showAlign(false); adapter.submit(buildColorOptions(),0,context) }
        tabBgColor.setOnClickListener { selectedTabIndex = 1; selectTab(tabBgColor); showRecycler(true,); showStyle(false); showAlign(false); adapter.submit(buildBGColorOptions(),1,context) }
        tabFonts.setOnClickListener { selectedTabIndex = 2; selectTab(tabFonts); showRecycler(true,false); showStyle(false); showAlign(false); adapter.submit(refreshFontsSample(),2,context) }
        tabStyle.setOnClickListener { selectTab(tabStyle); showRecycler(false); showStyle(true); showAlign(false) }
        tabAlignment.setOnClickListener { selectTab(tabAlignment); showRecycler(false); showStyle(false); showAlign(true) }

        // Apply initial styles and select appropriate alignment
        when (initialGravity) {
            (android.view.Gravity.START or android.view.Gravity.CENTER_VERTICAL) -> alignLeft.performClick()
            android.view.Gravity.CENTER -> alignCenter.performClick()
            (android.view.Gravity.END or android.view.Gravity.CENTER_VERTICAL) -> alignRight.performClick()
            else -> alignCenter.performClick()
        }

        setStyleActive(btnBold, isBold)
        setStyleActive(btnItalic, isItalic)
        setStyleActive(btnUnderline, isUnderline)

        // Apply typeface state after setting initial values
        applyTypefaceFromState()

        // initial dataset
        tabColor.performClick()

        // Show keyboard
        textEditor.requestFocus()

        colorPickerBtn.setOnClickListener {
            val initialColor = when (selectedTabIndex) {
                1 -> {
                    // background; default to white if transparent
                    (textEditor.background as? ColorDrawable)?.color ?: Color.WHITE
                }
                else -> textEditor.currentTextColor
            }
            ColorPickerDialog(context, initialColor) { color ->
                if (selectedTabIndex == 1) {
                    textEditor.setBackgroundColor(color)
                } else {
                    textEditor.setTextColor(color)
                }
            }.show()
        }
    }


    // Models and adapter
    sealed class EditorOption {
        data class ColorOption(val color: Int) : EditorOption()
        data class BGColorOption(val color: Int) : EditorOption()
//        data class FontOption(val sample: String, val typeface: Typeface) : EditorOption()
        data class FontOption(val sample: String, val typeface: Typeface, val fontResId: Int) : EditorOption()

    }


    fun setOnTextSavedListener(listener: (String,Int,Int,Typeface?,Boolean,Boolean,Boolean,Int) -> Unit) {
        onTextSaved = listener
    }
}