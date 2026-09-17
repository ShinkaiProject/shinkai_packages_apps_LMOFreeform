package com.libremobileos.sidebar.preference

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.SeekBar
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceViewHolder
import com.libremobileos.sidebar.R
import java.util.Locale

class FloatSliderPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes), SeekBar.OnSeekBarChangeListener {

    companion object {
        private val CONVERSION_REGEX = Regex("%(?:\\d+\\$)?([a-zA-Z])")
        private val INTEGER_CONVERSIONS = setOf("d", "o", "x")

        fun formatValue(template: String, value: Float): String {
            val conversion = CONVERSION_REGEX.findAll(template)
                .mapNotNull { it.groupValues.getOrNull(1)?.lowercase() }
                .lastOrNull()
            val arg: Any = if (conversion in INTEGER_CONVERSIONS) value.toInt() else value
            return String.format(Locale.US, template, arg)
        }
    }

    private var minValue = 0f
    private var maxValue = 1f
    private var step = 0.1f
    private var floatValue = 0f
    private var titleTemplate: String? = null
    private var seekBar: SeekBar? = null

    init {
        layoutResource = R.layout.preference_float_slider
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.FloatSliderPreference)
            minValue = a.getFloat(R.styleable.FloatSliderPreference_floatMin, 0f)
            maxValue = a.getFloat(R.styleable.FloatSliderPreference_floatMax, 1f)
            step = a.getFloat(R.styleable.FloatSliderPreference_floatStep, 0.1f)
            a.recycle()
        }
    }

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager) {
        super.onAttachedToHierarchy(preferenceManager)
        titleTemplate = title?.toString()
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        val def = (defaultValue as? Number)?.toFloat() ?: 0f
        floatValue = if (shouldPersist()) getPersistedFloat(def) else def
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getFloat(index, 0f)
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        seekBar = holder.findViewById(R.id.seekbar) as? SeekBar
        seekBar?.let { bar ->
            val totalSteps = if (step > 0) ((maxValue - minValue) / step).toInt() else 1
            bar.max = totalSteps
            val progress = if (step > 0) ((floatValue - minValue) / step).toInt() else 0
            bar.progress = progress
            bar.setOnSeekBarChangeListener(this)
        }
        val titleView = holder.findViewById(android.R.id.title) as? TextView
        titleTemplate?.let { template ->
            titleView?.text = formatValue(template, floatValue)
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            val newValue = (minValue + progress * step).coerceIn(minValue, maxValue)
            updateTitle(newValue)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        val newValue = (minValue + seekBar.progress * step).coerceIn(minValue, maxValue)
        if (newValue != floatValue) {
            if (callChangeListener(newValue)) {
                persistValue(newValue)
            }
        }
    }

    fun getFloatValue(): Float = floatValue

    fun setValue(value: Float) {
        val clamped = value.coerceIn(minValue, maxValue)
        if (clamped != floatValue) {
            if (callChangeListener(clamped)) {
                persistValue(clamped)
            }
        }
        updateTitle(floatValue)
    }

    private fun persistValue(value: Float) {
        if (shouldPersist()) {
            persistFloat(value)
        }
        floatValue = value
        notifyChanged()
    }

    private fun updateTitle(value: Float) {
        titleTemplate?.let { template ->
            title = formatValue(template, value)
        }
    }
}