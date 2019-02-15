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
        if (name.contains("github")) return R.drawable.ic_github
        else if (name.contains("twitter")) return R.drawable.ic_twitter
        else if (name.contains("facebook")) return R.drawable.ic_facebook
        else if (name.contains("linkedin")) return R.drawable.ic_linkedin
        else if (name.contains("youtube")) return R.drawable.ic_youtube
        else if (name.contains("google")) return R.drawable.ic_google_plus
        else if (name.contains("wiki")) return R.drawable.ic_wikipedia
        else if (name.contains("flickr")) return R.drawable.ic_flickr
        else if (name.contains("blog")) return R.drawable.ic_blogger
        else return R.drawable.ic_link_black
    }
}
