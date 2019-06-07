package org.fossasia.openevent.general.utils

import android.text.Html
import android.util.Patterns
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.fossasia.openevent.general.R

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

fun TextInputEditText.checkEmpty(): Boolean {
    if (text.isNullOrBlank()) {
        error = resources.getString(R.string.empty_field_error_message)
        return false
    }
    return true
}

fun TextInputEditText.checkValidEmail(): Boolean {
    if (text.isNullOrBlank()) return false
    if (!Patterns.EMAIL_ADDRESS.matcher(text.toString()).matches()) {
        error = resources.getString(R.string.invalid_email_message)
        return false
    }
    return true
}

fun TextInputLayout.setRequired() {
    hint = "$hint *"
}
