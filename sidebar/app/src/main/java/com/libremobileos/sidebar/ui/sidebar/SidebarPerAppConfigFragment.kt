/*
 * SPDX-FileCopyrightText: DerpFest AOSP
 * SPDX-FileCopyrightText: 2026 kenway214
 * SPDX-License-Identifier: Apache-2.0
 */

package com.libremobileos.sidebar.ui.sidebar

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.SwitchPreferenceCompat
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import com.libremobileos.sidebar.R
import com.libremobileos.sidebar.app.SidebarApplication
import com.libremobileos.sidebar.preference.ConfigDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SidebarPerAppConfigFragment : SettingsBasePreferenceFragment() {

    companion object {
        const val PREF_AUTO_APPS = "sidebar_auto_apps"
        private const val KEY_INFO = "info"
        private const val KEY_RELOAD = "reloading"
    }

    private lateinit var sharedPrefs: SharedPreferences
    private val appPreferences = mutableListOf<SwitchPreferenceCompat>()

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_per_app, menu)
            val searchItem = menu.findItem(R.id.search)
            val searchView = searchItem.actionView as? SearchView
            searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String): Boolean {
                    filterApps(newText)
                    return true
                }

                override fun onQueryTextSubmit(query: String): Boolean {
                    return true
                }
            })
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.title = getString(R.string.sidebar_per_app_config)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = ConfigDataStore(requireContext())
        sharedPrefs = requireContext()
            .getSharedPreferences(SidebarApplication.CONFIG, Context.MODE_PRIVATE)
        setPreferencesFromResource(R.xml.per_app_settings, rootKey)
        populateApps()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun populateApps() {
        lifecycleScope.launch(Dispatchers.IO) {
            val apps = loadInstalledApps(requireContext())
            withContext(Dispatchers.Main) {
                addAppPreferences(apps)
            }
        }
    }

    private fun addAppPreferences(entries: List<AppEntry>) {
        val autoApps = sharedPrefs.getStringSet(PREF_AUTO_APPS, emptySet()) ?: emptySet()
        appPreferences.clear()
        entries.forEach { entry ->
            val pref = SwitchPreferenceCompat(requireContext()).apply {
                key = entry.packageName
                title = entry.label
                summary = entry.packageName
                icon = entry.icon
                isChecked = autoApps.contains(entry.packageName)
                isPersistent = false
                setOnPreferenceChangeListener { _, newValue ->
                    updateAutoApps(newValue as Boolean, entry.packageName)
                    true
                }
            }
            appPreferences.add(pref)
            preferenceScreen.addPreference(pref)
        }
    }

    private fun updateAutoApps(enabled: Boolean, packageName: String) {
        val current = sharedPrefs.getStringSet(PREF_AUTO_APPS, emptySet())
            ?.toMutableSet() ?: mutableSetOf()
        if (enabled) {
            current.add(packageName)
        } else {
            current.remove(packageName)
        }
        sharedPrefs.edit().putStringSet(PREF_AUTO_APPS, current).apply()
    }

    private fun filterApps(query: String) {
        val normalized = query.trim().lowercase()
        appPreferences.forEach { pref ->
            pref.isVisible = if (normalized.isEmpty()) {
                true
            } else {
                pref.title?.toString()?.lowercase()?.contains(normalized) == true ||
                    pref.summary?.toString()?.lowercase()?.contains(normalized) == true
            }
        }
    }

    private suspend fun loadInstalledApps(context: Context): List<AppEntry> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        apps.asSequence()
            .filter { app ->
                (app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 &&
                    app.packageName != context.packageName
            }
            .map { app ->
                AppEntry(
                    label = app.loadLabel(pm).toString(),
                    packageName = app.packageName,
                    icon = app.loadIcon(pm)
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    private data class AppEntry(
        val label: String,
        val packageName: String,
        val icon: Drawable
    )
}