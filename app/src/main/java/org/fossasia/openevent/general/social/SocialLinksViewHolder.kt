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
        val drawableId = getSocialLinkDrawableId(socialLink.name.toLowerCase())
        val imageDrawable: Drawable? = ContextCompat.getDrawable(context, drawableId)
        imageDrawable?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(context, R.color.greyMore),
            PorterDuff.Mode.SRC_IN)
        itemView.imgSocialLink.setImageDrawable(imageDrawable)

        itemView.setOnClickListener {
            Utils.openUrl(context, socialLink.link)
        }
    }

    private fun getSocialLinkDrawableId(name: String): Int {
        return when {
            name.contains("github") -> R.drawable.ic_github
            name.contains("twitter") -> R.drawable.ic_twitter
            name.contains("facebook") -> R.drawable.ic_facebook
            name.contains("linkedin") -> R.drawable.ic_linkedin
            name.contains("youtube") -> R.drawable.ic_youtube
            name.contains("google") -> R.drawable.ic_google_plus
            name.contains("wiki") -> R.drawable.ic_wikipedia
            name.contains("flickr") -> R.drawable.ic_flickr
            name.contains("blog") -> R.drawable.ic_blogger
            else -> R.drawable.ic_link_black
        }
    }
}
