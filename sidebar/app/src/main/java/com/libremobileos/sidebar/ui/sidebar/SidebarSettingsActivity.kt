package com.libremobileos.sidebar.ui.sidebar

import android.os.Bundle
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity

/**
 * @author KindBrave
 * @since 2023/10/21
 */
class SidebarSettingsActivity : CollapsingToolbarBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(com.android.settingslib.collapsingtoolbar.R.id.content_frame, SidebarSettingsFragment())
                .commit()
        }
    }
}