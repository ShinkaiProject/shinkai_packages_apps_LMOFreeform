/*
 * SPDX-FileCopyrightText: 2026 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.sidebar.ui.sidebar

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceCategory
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import com.libremobileos.sidebar.R
import com.libremobileos.sidebar.bean.SidebarAppInfo
import com.libremobileos.sidebar.preference.ConfigDataStore
import com.libremobileos.sidebar.service.SidebarMonitorService
import kotlinx.coroutines.launch

class SidebarSettingsFragment : SettingsBasePreferenceFragment() {

    companion object {
        private const val KEY_MASTER = "sideline"
        private const val KEY_AUTO_ENABLE = "sidebar_auto_enable_selected_apps"
        private const val KEY_PER_APP = "sidebar_per_app_config"
        private const val KEY_PREDICTED = "sidebar_show_predicted_apps"
        private const val KEY_PINNED_CATEGORY = "pinned_apps_category"
    }

    private val viewModel: SidebarSettingsViewModel by viewModels { SidebarSettingsViewModel.Factory }

    private var masterSwitch: SwitchPreferenceCompat? = null
    private var autoEnableSwitch: SwitchPreferenceCompat? = null
    private var perAppPreference: androidx.preference.Preference? = null
    private var predictedPreference: SwitchPreferenceCompat? = null

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

        masterSwitch = findPreference(KEY_MASTER)?.apply {
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

        autoEnableSwitch = findPreference(KEY_AUTO_ENABLE)?.apply {
            isChecked = viewModel.isEnabled && viewModel.getAutoEnableSelectedAppsEnabled()
            isEnabled = viewModel.isEnabled
            setOnPreferenceChangeListener { _, newValue ->
                val enabled = newValue as Boolean
                viewModel.setAutoEnableSelectedAppsEnabled(enabled)
                updateMonitorService(context, enabled || viewModel.getSidebarEnabled())
                updateVisibility()
                true
            }
        }

        perAppPreference = findPreference(KEY_PER_APP)?.apply {
            setOnPreferenceClickListener {
                startActivity(Intent(context, SidebarPerAppConfigActivity::class.java))
                true
            }
        }

        predictedPreference = findPreference(KEY_PREDICTED)?.apply {
            isChecked = viewModel.isEnabled && viewModel.getPredictedAppsEnabled()
            isEnabled = viewModel.isEnabled
            setOnPreferenceChangeListener { _, newValue ->
                viewModel.setPredictedAppsEnabled(newValue as Boolean)
                true
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.appListFlow.collect { appList ->
                    rebuildPinnedApps(appList)
                }
            }
        }

        updateVisibility()
    }

    override fun onResume() {
        super.onResume()
        masterSwitch?.isChecked = viewModel.getSidebarEnabled()
        updateVisibility()
    }

    private fun rebuildPinnedApps(appList: List<SidebarAppInfo>) {
        val category = findPreference<PreferenceCategory>(KEY_PINNED_CATEGORY) ?: return
        category.removeAll()
        appList.forEach { appInfo ->
            val switch = SwitchPreferenceCompat(requireContext()).apply {
                key = "${appInfo.packageName}|${appInfo.activityName}|${appInfo.userId}"
                title = appInfo.label
                icon = appInfo.icon
                isChecked = appInfo.isSidebarApp
                isPersistent = false
                setOnPreferenceChangeListener { _, newValue ->
                    if (newValue as Boolean) {
                        viewModel.addSidebarApp(appInfo)
                    } else {
                        viewModel.deleteSidebarApp(appInfo)
                    }
                    true
                }
            }
            category.addPreference(switch)
        }
    }

    private fun updateVisibility() {
        val enabledUser = viewModel.isEnabled
        val master = enabledUser && viewModel.getSidebarEnabled()
        val autoEnable = enabledUser && viewModel.getAutoEnableSelectedAppsEnabled()
        perAppPreference?.isVisible = autoEnable
        predictedPreference?.isVisible = master
        findPreference<PreferenceCategory>(KEY_PINNED_CATEGORY)?.isVisible = master
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