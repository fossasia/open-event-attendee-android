package org.fossasia.openevent.general.data

import android.annotation.SuppressLint
import android.preference.PreferenceManager
import org.fossasia.openevent.general.OpenEventGeneral

@SuppressLint("CommitPrefEdits")
class Preference {

    private val sharedPreferences by lazy {
        PreferenceManager.getDefaultSharedPreferences(OpenEventGeneral.appContext)
    }
    private val editor by lazy {
        sharedPreferences.edit()
    }

    fun putInt(key: String, value: Int) {
        editor.putInt(key, value).apply()
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun putLong(key: String, value: Long) {
        editor.putLong(key, value).apply()
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    fun putString(key: String, value: String?) {
        editor.putString(key, value).apply()
    }

    fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        editor.putBoolean(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun remove(key: String) {
        editor.remove(key).apply()
    }
}
