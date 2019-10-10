package org.fossasia.openevent.general.utils

import android.content.Context
import android.content.res.Resources
import android.text.Editable
import android.text.Html
import android.text.InputType
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextPaint
import android.text.TextWatcher
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

fun TextInputEditText.checkEmpty(layout: TextInputLayout): Boolean {
    if (text.isNullOrBlank()) {
        layout.error = resources.getString(R.string.empty_field_error_message)
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                layout.error = null
                layout.isErrorEnabled = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* Do Nothing */ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /* Do Nothing */ }
        })
        return false
    }
    return true
}

fun TextInputEditText.checkValidEmail(layout: TextInputLayout): Boolean {
    if (text.isNullOrBlank() && inputType != InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) return false
    if (!Patterns.EMAIL_ADDRESS.matcher(text.toString()).matches()) {
        layout.error = resources.getString(R.string.invalid_email_message)
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                layout.error = null
                layout.isErrorEnabled = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* Do Nothing */ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /* Do Nothing */ }
        })
        return false
    }
    return true
}

fun TextInputLayout.setRequired() {
    if (hint?.takeLast(1) != "*")
        hint = "$hint *"
}

fun TextInputEditText.checkValidURI(layout: TextInputLayout): Boolean {
    if (text.isNullOrBlank() && inputType != InputType.TYPE_TEXT_VARIATION_URI) return false
    if (!Patterns.WEB_URL.matcher(text.toString()).matches()) {
        layout.error = resources.getString(R.string.invalid_url_message)
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                layout.error = null
                layout.isErrorEnabled = false
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { /* Do Nothing */ }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /* Do Nothing */ }
        })
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

    fun isEmpty(str: CharSequence?): Boolean {
        return str.isNullOrEmpty()
    }
}
