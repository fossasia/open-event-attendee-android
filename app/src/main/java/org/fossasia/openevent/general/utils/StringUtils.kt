package org.fossasia.openevent.general.utils

import android.text.Html

fun String?.nullToEmpty(): String {
    return this ?: ""
}

fun String?.stripHtml(): String? {

    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY).toString()
    } else {
        Html.fromHtml(this)?.toString() ?: this
    }
}
