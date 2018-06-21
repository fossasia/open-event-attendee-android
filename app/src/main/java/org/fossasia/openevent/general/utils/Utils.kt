package org.fossasia.openevent.general.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import org.fossasia.openevent.general.R

object Utils {

    fun setUpCustomTab(context: Context, url: String) {
        var URL = url
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            URL = "http://$url"
        }

        CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setCloseButtonIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_arrow_back_white_cct_24dp))
                .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
                .build()
                .launchUrl(context, Uri.parse(URL))
    }
}
