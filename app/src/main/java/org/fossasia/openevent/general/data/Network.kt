package org.fossasia.openevent.general.data

import android.content.Context
import android.net.ConnectivityManager
import org.fossasia.openevent.general.OpenEventGeneral

class Network {

    private val context by lazy {
        OpenEventGeneral.appContext!!
    }

    fun isNetworkConnected(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

        return connectivityManager?.activeNetworkInfo != null
    }
}