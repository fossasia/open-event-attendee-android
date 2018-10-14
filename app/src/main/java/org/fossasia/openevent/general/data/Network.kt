package org.fossasia.openevent.general.data

import android.content.Context
import android.net.ConnectivityManager
import org.fossasia.openevent.general.OpenEventGeneral

class Network {

    private val context by lazy {
        OpenEventGeneral.appContext
    }

    private val connectivityManager by lazy {
        context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    }

    fun isNetworkConnected(): Boolean {
        return connectivityManager?.activeNetworkInfo != null
    }
}
