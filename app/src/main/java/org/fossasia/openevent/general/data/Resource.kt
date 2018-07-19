package org.fossasia.openevent.general.data

import android.content.Context
import android.net.ConnectivityManager
import android.support.annotation.StringRes
import org.fossasia.openevent.general.OpenEventGeneral

class Resource {

    private val context by lazy {
        OpenEventGeneral.appContext!!
    }

    fun getString(@StringRes resId: Int) = context.getString(resId)

    fun getString(@StringRes resId: Int, vararg args: Any?) = context.getString(resId, args)

    fun isNetworkConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        return connectivityManager?.activeNetworkInfo != null
    }
}