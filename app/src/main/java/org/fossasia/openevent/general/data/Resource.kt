package org.fossasia.openevent.general.data

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import org.fossasia.openevent.general.OpenEventGeneral

class Resource {

    private val context by lazy {
        OpenEventGeneral.appContext
    }

    fun getString(@StringRes resId: Int) = context?.getString(resId)

    fun getString(@StringRes resId: Int, vararg args: Any?) = context?.getString(resId, args)

    fun getColor(@ColorRes resId: Int) = context?.resources?.getColor(resId)
}
