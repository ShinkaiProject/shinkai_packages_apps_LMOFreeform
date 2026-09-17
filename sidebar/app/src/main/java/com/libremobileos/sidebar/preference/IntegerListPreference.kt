package com.libremobileos.sidebar.preference

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference

class IntegerListPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ListPreference(context, attrs) {

    override fun persistString(value: String?): Boolean {
        return value?.toIntOrNull()?.let { persistInt(it) } ?: false
    }

    override fun getPersistedString(defaultReturnValue: String?): String {
        val defaultInt = defaultReturnValue?.toIntOrNull() ?: 0
        return getPersistedInt(defaultInt).toString()
    }
}
