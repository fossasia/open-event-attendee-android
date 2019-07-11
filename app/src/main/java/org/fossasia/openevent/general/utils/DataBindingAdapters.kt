package org.fossasia.openevent.general.utils

import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PorterDuff
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.attendees.ORDER_STATUS_COMPLETED
import org.fossasia.openevent.general.attendees.ORDER_STATUS_PENDING
import org.fossasia.openevent.general.attendees.ORDER_STATUS_PLACED
import org.fossasia.openevent.general.data.Resource
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

@BindingAdapter("eventImage")
fun setEventImage(imageView: ImageView, url: String?) {
    Picasso.get()
        .load(url)
        .placeholder(R.drawable.header)
        .into(imageView)
}

@BindingAdapter("isFavorite")
fun setFavorite(fab: FloatingActionButton, isFavorite: Boolean) {
    fab.setImageResource(
        if (isFavorite) R.drawable.ic_baseline_favorite else R.drawable.ic_baseline_favorite_border)
}

@BindingAdapter("expiredTicket")
fun setExpired(imageView: ImageView, isExpired: Boolean) {
    if (isExpired) {
        val matrix = ColorMatrix()
        matrix.setSaturation(0F)
        imageView.colorFilter = ColorMatrixColorFilter(matrix)
    }
}

@BindingAdapter("orderStatus")
fun setOrderStatus(textView: TextView, orderStatus: String) {
    val resource = Resource()
    when (orderStatus) {
        ORDER_STATUS_PLACED -> {
            textView.isVisible = true
            textView.text = ORDER_STATUS_PLACED
            resource.getColor(R.color.orderStatusBlue)?.let {
                textView.background.setColorFilter(it, PorterDuff.Mode.SRC_ATOP)
            }
        }
        ORDER_STATUS_COMPLETED -> {
            textView.isVisible = true
            textView.text = ORDER_STATUS_COMPLETED
            resource.getColor(R.color.orderStatusGreen)?.let {
                textView.background.setColorFilter(it, PorterDuff.Mode.SRC_ATOP)
            }
        }
        ORDER_STATUS_PENDING -> {
            textView.isVisible = true
            textView.text = ORDER_STATUS_PENDING
            resource.getColor(R.color.orderStatusOrange)?.let {
                textView.background.setColorFilter(it, PorterDuff.Mode.SRC_ATOP)
            }
        }
        else -> {
            textView.isVisible = false
        }
    }
}
