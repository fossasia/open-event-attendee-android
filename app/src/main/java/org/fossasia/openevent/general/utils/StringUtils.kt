package org.fossasia.openevent.general.utils

import android.content.Context
import android.content.res.Resources
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.util.Patterns
import android.view.View
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.fossasia.openevent.general.R

fun String?.nullToEmpty(): String {
    return this ?: ""
}

fun String?.emptyToNull(): String? {
    return if (this == "") null else this
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
    if (hint?.takeLast(1) != "*")
        hint = "$hint *"
}

fun TextInputEditText.checkValidURI(): Boolean {
    if (text.isNullOrBlank()) return false
    if (!Patterns.WEB_URL.matcher(text.toString()).matches()) {
        error = resources.getString(R.string.invalid_url_message)
        return false
    }

    return true
}

object StringUtils {
    fun getTermsAndPolicyText(context: Context, resources: Resources): SpannableStringBuilder {
        val paragraph = SpannableStringBuilder()
        val startText = resources.getString(R.string.start_text)
        val termsText = resources.getString(R.string.terms_text)
        val middleText = resources.getString(R.string.middle_text)
        val privacyText = resources.getString(R.string.privacy_text)

        paragraph.append(startText)
        paragraph.append(" $termsText")
        paragraph.append(" $middleText")
        paragraph.append(" $privacyText")

        val termsSpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }

            override fun onClick(widget: View) {
                Utils.openUrl(context, resources.getString(R.string.terms_of_service))
            }
        }

        val privacyPolicySpan = object : ClickableSpan() {
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }

            override fun onClick(widget: View) {
                Utils.openUrl(context, resources.getString(R.string.privacy_policy))
            }
        }

        paragraph.setSpan(termsSpan, startText.length, startText.length + termsText.length + 2,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        paragraph.setSpan(privacyPolicySpan, paragraph.length - privacyText.length, paragraph.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE) // -1 so that we don't include "." in the link
        return paragraph
    }
}
