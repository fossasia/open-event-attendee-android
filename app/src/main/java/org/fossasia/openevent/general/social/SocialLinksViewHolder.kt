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
        if (drawableId != -1) {
            val imageDrawable: Drawable? = ContextCompat.getDrawable(context, drawableId)
            imageDrawable?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.greyMore),
                PorterDuff.Mode.SRC_IN)

            itemView.imgSocialLink.setImageDrawable(imageDrawable)
        }

        itemView.setOnClickListener {
            Utils.openUrl(context, socialLink.link)
        }
    }

    private fun getSocialLinkDrawableId(name: String): Int {
        return when (name) {
            "Github Url" -> R.drawable.ic_github_24dp
            "Twitter Url" -> R.drawable.ic_twitter_24dp
            "Facebook Url" -> R.drawable.ic_facebook_24dp
            "LinkedIn Url" -> R.drawable.ic_linkedin_24dp
            "Youtube Url" -> R.drawable.ic_youtube_24dp
            "Google Url" -> R.drawable.ic_google_plus_24dp
            else -> -1
        }
    }
}
