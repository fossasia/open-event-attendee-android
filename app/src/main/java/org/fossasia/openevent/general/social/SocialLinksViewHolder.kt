package org.fossasia.openevent.general.social

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.support.customtabs.CustomTabsIntent
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import kotlinx.android.synthetic.main.item_social_link.view.*
import org.fossasia.openevent.general.R
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter

class SocialLinksViewHolder(itemView: View, private var context: Context) : RecyclerView.ViewHolder(itemView) {

    fun bind(event: SocialLink) {
        val imageDrawable :Drawable? = ContextCompat.getDrawable(context, getSocialLinkDrawableId(event.name))
        imageDrawable?.colorFilter = PorterDuffColorFilter(context.resources.getColor(R.color.greyMore), PorterDuff.Mode.SRC_IN)

        itemView.img_social_link.setImageDrawable(imageDrawable)

        itemView.setOnClickListener{
            setUpCustomTab(context, event.link)
        }
    }

    private fun getSocialLinkDrawableId(name: String): Int {
        var id = 1
        when (name) {
            "Github Url" -> id = R.drawable.ic_github_24dp
            "Twitter Url" -> id = R.drawable.ic_twitter_24dp
            "Facebook Url" -> id = R.drawable.ic_facebook_24dp
            "LinkedIn Url" -> id = R.drawable.ic_linkedin_24dp
            "Youtube Url" -> id = R.drawable.ic_youtube_24dp
            "Google Url" -> id = R.drawable.ic_google_plus_24dp
            else -> {
            }
        }
        return id
    }

    private fun setUpCustomTab(context: Context, url: String) {

        var URL = url
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            URL = "http://$url"
        }

        val customTabsBuilder = CustomTabsIntent.Builder()
        customTabsBuilder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
        customTabsBuilder.setCloseButtonIcon(BitmapFactory.decodeResource(context.resources, R.drawable.ic_arrow_back_white_cct_24dp))
        customTabsBuilder.setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
        customTabsBuilder.setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
        val customTabsIntent = customTabsBuilder.build()
        customTabsIntent.launchUrl(context, Uri.parse(URL))
    }
}