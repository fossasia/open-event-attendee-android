package org.fossasia.openevent.general.social

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_social_link.view.imgSocialLink
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils

class SocialLinksViewHolder(itemView: View, private var context: Context) : RecyclerView.ViewHolder(itemView) {

    fun bind(socialLink: SocialLink) {
        val drawableId = getSocialLinkDrawableId(socialLink.name)
        val imageDrawable: Drawable? = ContextCompat.getDrawable(context, drawableId)
        imageDrawable?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.greyMore),
            PorterDuff.Mode.SRC_IN)
        itemView.imgSocialLink.setImageDrawable(imageDrawable)

        itemView.setOnClickListener {
            Utils.openUrl(context, socialLink.link)
        }
    }

    private fun getSocialLinkDrawableId(name: String): Int {
        return when (name.toLowerCase()) {
            "github url",
            "githuburl",
            "github" -> R.drawable.ic_github
            "twitter url",
            "twitterurl",
            "twitter" -> R.drawable.ic_twitter
            "facebook url",
            "facebookurl",
            "facebook" -> R.drawable.ic_facebook
            "linkedin url",
            "linkedinurl",
            "linkedin" -> R.drawable.ic_linkedin
            "youtube url",
            "youtubeurl",
            "youtube" -> R.drawable.ic_youtube
            "googleplus",
            "googleplus url",
            "googleplusurl",
            "google plus",
            "google plusurl",
            "google plus url",
            "google url",
            "googleurl",
            "google" -> R.drawable.ic_google_plus
            "wikipedia",
            "wikipedia url",
            "wikipediaurl" -> R.drawable.ic_wikipedia
            "flickr",
            "flickr url",
            "flickrurl" -> R.drawable.ic_flickr
            "blogger",
            "blogger url",
            "bloggerurl",
            "blog",
            "blog url",
            "blogurl" -> R.drawable.ic_blogger
            else -> R.drawable.ic_link_black
        }
    }
}
