package org.fossasia.openevent.general.event

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import org.fossasia.openevent.general.R
import org.jetbrains.anko.*
import org.jetbrains.anko.cardview.v7.cardView
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.design.floatingActionButton


class ItemCardEventsUi : AnkoComponent<ViewGroup> {

    companion object {
        val eventImage = 1
        val date = 2
        val month = 3
        val locationName = 4
        val eventName = 5
        val shareFab = 6
        val favFab = 7
    }


    @SuppressLint("InlinedApi", "NewApi")
    override fun createView(ui: AnkoContext<ViewGroup>): View = with(ui) {
        cardView {
            lparams(width = matchParent, height = wrapContent){
                margin = dimen(R.dimen.layout_margin_medium)
            }
            id = R.id.allEventsCard
            cardElevation = dimen(R.dimen.card_elevation).toFloat()
            backgroundColor = Color.WHITE
            radius = dimen(R.dimen.card_corner_radius).toFloat()
//            foreground = ColorDrawable(colorAttr(android.R.attr.selectableItemBackground))
            constraintLayout {
                imageView {
                    id = eventImage
                    scaleType = ImageView.ScaleType.CENTER_CROP
                    imageResource = R.drawable.placeholder
                }.lparams(width = dimen(R.dimen.layout_margin_none), height = dimen(R.dimen.layout_margin_none)) {
                    topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                    startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                    horizontalBias = 0.5f
                    dimensionRatio = "2"
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                }
                floatingActionButton {
                    id = shareFab
                    backgroundColor = Color.WHITE
                    scaleType = ImageView.ScaleType.CENTER
                    imageResource = R.drawable.ic_share_grey
                    backgroundTintList = resources.getColorStateList(android.R.color.white)
                    elevation = dimen(R.dimen.fab_elevation).toFloat()
                }.lparams(width = dimen(R.dimen.fab_width), height = dimen(R.dimen.fab_height)) {
                    marginEnd = dimen(R.dimen.fab_margin_80dp)
                    rightMargin = dimen(R.dimen.fab_margin_80dp)
                    bottomToBottom = eventImage
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    topToBottom = eventImage
                }
                floatingActionButton {
                    id = favFab
                    backgroundColor = Color.WHITE
                    scaleType = ImageView.ScaleType.CENTER
                    backgroundTintList = resources.getColorStateList(android.R.color.white)
                    elevation = dimen(R.dimen.fab_elevation).toFloat()
                    imageResource = R.drawable.ic_baseline_favorite_border
                }.lparams(width = dimen(R.dimen.fab_width), height = dimen(R.dimen.fab_height)) {
                    marginEnd = dimen(R.dimen.fab_margin_right)
                    rightMargin = dimen(R.dimen.fab_margin_right)
                    bottomToBottom = eventImage
                    endToEnd = eventImage
                    topToBottom = eventImage
                }
                textView("Jan") {
                    id = month
                    textAlignment = View.TEXT_ALIGNMENT_CENTER
                    allCaps = true
                    textColor = resources.getColor(R.color.colorPrimaryDark)
                }.lparams(width = dimen(R.dimen.text_size_very_large)) {
                    marginStart = dimen(R.dimen.layout_margin_small)
                    leftMargin = dimen(R.dimen.layout_margin_small)
                    topMargin = dimen(R.dimen.layout_margin_extra_small)
                    startToStart = eventImage
                    topToTop = eventName
                }
                textView("15") {
                    id = date
                    textColor = resources.getColor(R.color.black)
                }.lparams {
                    endToEnd = month
                    startToStart = month
                    topToBottom = month
                }
                textView("Open Source Meetup") {
                    id = eventName
                    textColor = resources.getColor(R.color.black)
                    textSize = 20f
                }.lparams(width = dimen(R.dimen.layout_margin_none)) {
                    marginStart = dimen(R.dimen.layout_margin_medium)
                    leftMargin = dimen(R.dimen.layout_margin_medium)
                    marginEnd = dimen(R.dimen.layout_margin_medium)
                    rightMargin = dimen(R.dimen.layout_margin_medium)
                    topToBottom = shareFab
                    startToEnd = month
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                }
                textView("Jaipur, Rajasthan, India") {
                    id = locationName
                    textSize = 12f
                }.lparams(width = dimen(R.dimen.layout_margin_none)) {
                    topMargin = dimen(R.dimen.layout_margin_small)
                    marginEnd = dimen(R.dimen.layout_margin_medium)
                    rightMargin = dimen(R.dimen.layout_margin_medium)
                    bottomMargin = dimen(R.dimen.layout_margin_extra_large)
                    bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                    endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                    startToStart = eventName
                    topToBottom = eventName

                }
            }
        }
    }
}

