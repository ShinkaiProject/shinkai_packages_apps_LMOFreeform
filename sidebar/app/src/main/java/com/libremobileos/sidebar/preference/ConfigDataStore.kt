package com.libremobileos.sidebar.preference

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceDataStore
import com.libremobileos.sidebar.app.SidebarApplication

class ConfigDataStore(context: Context) : PreferenceDataStore() {

    private val sp: SharedPreferences =
        context.getSharedPreferences(SidebarApplication.CONFIG, Context.MODE_PRIVATE)

    override fun putString(key: String?, value: String?) {
        sp.edit().putString(key, value).apply()
    }

    override fun getString(key: String?, defValue: String?): String? {
        return sp.getString(key, defValue)
    }

    override fun putBoolean(key: String?, value: Boolean) {
        sp.edit().putBoolean(key, value).apply()
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return sp.getBoolean(key, defValue)
    }

    override fun putInt(key: String?, value: Int) {
        sp.edit().putInt(key, value).apply()
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return sp.getInt(key, defValue)
    }

    override fun putFloat(key: String?, value: Float) {
        sp.edit().putFloat(key, value).apply()
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return sp.getFloat(key, defValue)
    }

    override fun putLong(key: String?, value: Long) {
        sp.edit().putLong(key, value).apply()
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return sp.getLong(key, defValue)
    }

    override fun putStringSet(key: String?, values: MutableSet<String>?) {
        sp.edit().putStringSet(key, values).apply()
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        return sp.getStringSet(key, defValues)
    }
}
