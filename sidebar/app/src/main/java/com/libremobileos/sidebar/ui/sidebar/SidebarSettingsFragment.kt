/*
 * SPDX-FileCopyrightText: 2026 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.sidebar.ui.sidebar

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.widget.MainSwitchPreference
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import com.libremobileos.sidebar.R
import com.libremobileos.sidebar.preference.ConfigDataStore
import com.libremobileos.sidebar.service.SidebarMonitorService

class SidebarSettingsFragment : SettingsBasePreferenceFragment() {

    companion object {
        private const val KEY_MASTER = "sideline"
        private const val KEY_AUTO_ENABLE = "sidebar_auto_enable_selected_apps"
        private const val KEY_PER_APP = "sidebar_per_app_config"
        private const val KEY_PREDICTED = "sidebar_show_predicted_apps"
        private const val KEY_PINNED_CATEGORY = "pinned_apps_category"
    }

    private val viewModel: SidebarSettingsViewModel by viewModels { SidebarSettingsViewModel.Factory }

    private var masterSwitch: MainSwitchPreference? = null
    private var autoEnableSwitch: SwitchPreferenceCompat? = null
    private var perAppPreference: androidx.preference.Preference? = null
    private var predictedPreference: SwitchPreferenceCompat? = null
    private var pinnedAppsPreference: androidx.preference.Preference? = null
    private var customizationPreference: androidx.preference.Preference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = getString(R.string.sidebar_label)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = ConfigDataStore(requireContext())
        setPreferencesFromResource(R.xml.sidebar_settings, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = requireContext()

        masterSwitch = findPreference<MainSwitchPreference>(KEY_MASTER)?.apply {
            isChecked = viewModel.getSidebarEnabled()
            isEnabled = viewModel.isEnabled
            setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                viewModel.setSidebarEnabled(enabled)
                updateMonitorService(context, enabled || viewModel.getAutoEnableSelectedAppsEnabled())
                updateVisibility()
                true
            }
        }

        autoEnableSwitch = findPreference<SwitchPreferenceCompat>(KEY_AUTO_ENABLE)?.apply {
            isChecked = viewModel.isEnabled && viewModel.getAutoEnableSelectedAppsEnabled()
            setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                viewModel.setAutoEnableSelectedAppsEnabled(enabled)
                updateMonitorService(context, enabled || viewModel.getSidebarEnabled())
                updateVisibility()
                true
            }
        }

        customizationPreference = findPreference<androidx.preference.Preference>("sidebar_customization")?.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(context, SidebarCustomizationActivity::class.java))
                true
            }
        }

        pinnedAppsPreference = findPreference<androidx.preference.Preference>(KEY_PINNED_CATEGORY)?.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(context, PinnedAppsActivity::class.java))
                true
            }
        }

        perAppPreference = findPreference<androidx.preference.Preference>(KEY_PER_APP)?.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(context, SidebarPerAppConfigActivity::class.java))
                true
            }
        }

        predictedPreference = findPreference<SwitchPreferenceCompat>(KEY_PREDICTED)?.apply {
            isChecked = viewModel.isEnabled && viewModel.getPredictedAppsEnabled()
            setOnPreferenceChangeListener { _, newValue ->
                viewModel.setPredictedAppsEnabled(newValue as Boolean)
                true
            }
        }

        updateVisibility()
    }

    override fun onResume() {
        super.onResume()
        masterSwitch?.isChecked = viewModel.getSidebarEnabled()
        updateVisibility()
    }

    private fun updateVisibility() {
        val enabledUser = viewModel.isEnabled
        val master = enabledUser && viewModel.getSidebarEnabled()
        val autoEnable = enabledUser && viewModel.getAutoEnableSelectedAppsEnabled()

        autoEnableSwitch?.isEnabled = enabledUser
        perAppPreference?.isEnabled = autoEnable
        predictedPreference?.isEnabled = master
        pinnedAppsPreference?.isEnabled = master
        customizationPreference?.isEnabled = master
    }

    private fun updateMonitorService(context: android.content.Context, enabled: Boolean) {
        val intent = Intent(context, SidebarMonitorService::class.java)
        if (enabled) {
            context.startService(intent)
        } else {
            context.stopService(intent)
        }
    }
}