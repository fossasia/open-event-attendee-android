package org.fossasia.openevent.general.utils

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.R
import timber.log.Timber

/**
 * Toggle Visibility of the view depending upn the value is null or not.
 */
@BindingAdapter("hideIfEmpty")
fun setHideIfEmpty(view: View, value: String?) {
    view.visibility = if (!value.isNullOrEmpty()) View.VISIBLE else View.GONE
}

/**
 * Set Html Text by chaging the value using String Extensions in Utils.
 */
@BindingAdapter("strippedHtml")
fun TextView.setStrippedHtml(value: String?) {
    this.text = value.nullToEmpty().stripHtml()?.trim()
}

@BindingAdapter("avatarUrl")
fun setAvatarUrl(imageView: ImageView, url: String?) {
    Picasso.get()
        .load(url)
        .placeholder(R.drawable.ic_person_black)
        .into(imageView, object : Callback {
            override fun onSuccess() {
                imageView.tag = "image_loading_success"
            }

            override fun onError(e: Exception?) {
                Timber.e(e)
            }
        })
}

@BindingAdapter("headerUrl")
fun setOriginalImageUrl(imageView: ImageView, url: String?) {
    Picasso.get()
        .load(url)
        .placeholder(R.drawable.header)
        .transform(CircleTransform())
        .into(imageView)
}
