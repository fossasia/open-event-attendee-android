package org.fossasia.openevent.general.utils.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import androidx.core.view.isVisible

fun View.hideWithFading(duration: Long = 200L) {
    alpha = 1f
    animate().alpha(0f)
        .setDuration(duration)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?, isReverse: Boolean) {
                isVisible = false
            }
            override fun onAnimationCancel(animation: Animator?) {
                isVisible = false
            }
        })
}

fun View.showWithFading(duration: Long = 200L) {
    alpha = 0f
    isVisible = true
    animate().alpha(1f).duration = duration
}
