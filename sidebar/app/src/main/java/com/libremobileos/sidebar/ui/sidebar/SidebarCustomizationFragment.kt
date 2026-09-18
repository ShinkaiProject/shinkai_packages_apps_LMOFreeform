/*
 * SPDX-FileCopyrightText: 2026 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.sidebar.ui.sidebar

import android.os.Bundle
import android.view.View
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import com.android.settingslib.widget.SliderPreference
import com.libremobileos.sidebar.R
import com.libremobileos.sidebar.preference.ConfigDataStore
import com.libremobileos.sidebar.preference.FloatSliderPreference
import com.libremobileos.sidebar.preference.IntegerListPreference
import kotlin.math.roundToInt

class SidebarCustomizationFragment : SettingsBasePreferenceFragment() {

    companion object {
        private const val KEY_RESET = "sidebar_reset_defaults"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = getString(R.string.sidebar_customization_title)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = ConfigDataStore(requireContext())
        setPreferencesFromResource(R.xml.sidebar_customization, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSliders()
        findPreference<Preference>(KEY_RESET)?.setOnPreferenceClickListener {
            resetDefaults()
            true
        }
    }

    private val configStore: ConfigDataStore
        get() = preferenceManager.preferenceDataStore as ConfigDataStore

    private fun setupSliders() {
        setupFloatSlider("slider_transparency", R.string.sidebar_transparency_value, 10, 0.8f)
        setupFloatSlider("sidebar_corner_radius", R.string.sidebar_corner_radius_value, 1, 24f)
        setupFloatSlider("sidebar_background_transparency", R.string.sidebar_bg_transparency_value, 10, 0.8f)

        setupIntSlider("slider_length", R.string.sidebar_length_value)
        setupIntSlider("slider_width", R.string.sidebar_width_value)
        setupIntSlider("sidebar_columns", R.string.sidebar_columns_value)
        setupIntSlider("sidebar_icon_size", R.string.sidebar_icon_size_value)
        setupIntSlider("sidebar_icon_padding", R.string.sidebar_icon_padding_value)
        setupIntSlider("sidebar_column_spacing", R.string.sidebar_column_spacing_value)
        setupIntSlider("sidebar_auto_hide_timeout", R.string.sidebar_auto_hide_timeout_value)
    }

    private fun formatFloatTitle(resId: Int, value: Float): String =
        FloatSliderPreference.formatValue(getString(resId), value)

    private fun setupFloatSlider(key: String, titleRes: Int, scale: Int, defaultFloat: Float) {
        findPreference<SliderPreference>(key)?.apply {
            val storedFloat = configStore.getFloat(key, defaultFloat)
            value = (storedFloat * scale).roundToInt()
            title = formatFloatTitle(titleRes, storedFloat)
            setOnPreferenceChangeListener { pref, newValue ->
                val floatValue = (newValue as Int) / scale.toFloat()
                configStore.putFloat(key, floatValue)
                pref.title = formatFloatTitle(titleRes, floatValue)
                true
            }
        }
    }

    private fun setupIntSlider(key: String, titleRes: Int) {
        findPreference<SliderPreference>(key)?.apply {
            title = getString(titleRes, value)
            setOnPreferenceChangeListener { pref, newValue ->
                pref.title = getString(titleRes, newValue as Int)
                true
            }
        }
    }

    private fun refreshSliderTitles() {
        refreshFloatSliderTitle("slider_transparency", R.string.sidebar_transparency_value, 10)
        refreshFloatSliderTitle("sidebar_corner_radius", R.string.sidebar_corner_radius_value, 1)
        refreshFloatSliderTitle("sidebar_background_transparency", R.string.sidebar_bg_transparency_value, 10)

        refreshIntSliderTitle("slider_length", R.string.sidebar_length_value)
        refreshIntSliderTitle("slider_width", R.string.sidebar_width_value)
        refreshIntSliderTitle("sidebar_columns", R.string.sidebar_columns_value)
        refreshIntSliderTitle("sidebar_icon_size", R.string.sidebar_icon_size_value)
        refreshIntSliderTitle("sidebar_icon_padding", R.string.sidebar_icon_padding_value)
        refreshIntSliderTitle("sidebar_column_spacing", R.string.sidebar_column_spacing_value)
        refreshIntSliderTitle("sidebar_auto_hide_timeout", R.string.sidebar_auto_hide_timeout_value)
    }

    private fun refreshIntSliderTitle(key: String, titleRes: Int) {
        findPreference<SliderPreference>(key)?.let { pref ->
            pref.title = getString(titleRes, pref.value)
        }
    }

    private fun refreshFloatSliderTitle(key: String, titleRes: Int, scale: Int) {
        findPreference<SliderPreference>(key)?.let { pref ->
            pref.title = formatFloatTitle(titleRes, pref.value / scale.toFloat())
        }
    }

    private fun resetDefaults() {
        findPreference<SliderPreference>("slider_transparency")?.apply {
            setValue(8)
            configStore.putFloat("slider_transparency", 0.8f)
        }
        findPreference<SliderPreference>("sidebar_corner_radius")?.apply {
            setValue(24)
            configStore.putFloat("sidebar_corner_radius", 24f)
        }
        findPreference<SliderPreference>("sidebar_background_transparency")?.apply {
            setValue(8)
            configStore.putFloat("sidebar_background_transparency", 0.8f)
        }

        findPreference<SliderPreference>("slider_length")?.setValue(200)
        findPreference<SliderPreference>("slider_width")?.setValue(100)
        findPreference<SliderPreference>("sidebar_columns")?.setValue(1)
        findPreference<SliderPreference>("sidebar_icon_size")?.setValue(40)
        findPreference<SliderPreference>("sidebar_icon_padding")?.setValue(7)
        findPreference<SliderPreference>("sidebar_column_spacing")?.setValue(4)
        findPreference<SliderPreference>("sidebar_auto_hide_timeout")?.setValue(5)

        findPreference<IntegerListPreference>("sideline_position_x")?.value = "1"

        findPreference<SwitchPreferenceCompat>("sidebar_show_shadow")?.isChecked = true
        findPreference<SwitchPreferenceCompat>("sidebar_tap_to_open")?.isChecked = false
        findPreference<SwitchPreferenceCompat>("sidebar_swipe_to_open")?.isChecked = true
        findPreference<SwitchPreferenceCompat>("sidebar_hide_on_gamespace")?.isChecked = false
        findPreference<SwitchPreferenceCompat>("sidebar_auto_hide_enabled")?.isChecked = false

        refreshSliderTitles()
    }
}