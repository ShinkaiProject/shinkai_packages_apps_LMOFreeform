/*
 * SPDX-FileCopyrightText: 2026 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.sidebar.ui.sidebar

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
import kotlinx.coroutines.launch

class PinnedAppsFragment : SettingsBasePreferenceFragment() {

    companion object {
        private const val KEY_PINNED_LIST = "pinned_apps_list"
    }

    private val viewModel: SidebarSettingsViewModel by viewModels { SidebarSettingsViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = getString(R.string.sidebar_app_setting_label)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = ConfigDataStore(requireContext())
        setPreferencesFromResource(R.xml.sidebar_pinned_apps, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.appListFlow.collect { appList ->
                    rebuildPinnedApps(appList)
                }
            }
        }
    }

    private fun rebuildPinnedApps(appList: List<SidebarAppInfo>) {
        val category = findPreference<PreferenceCategory>(KEY_PINNED_LIST) ?: return
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
}