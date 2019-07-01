package org.fossasia.openevent.general.utils.extensions

import android.os.Build
import androidx.transition.TransitionInflater
import androidx.fragment.app.Fragment

const val SUPPORTED_TRANSITION_VERSION = Build.VERSION_CODES.O

fun Fragment.setStartPostponedEnterTransition() {
    if (Build.VERSION.SDK_INT >= SUPPORTED_TRANSITION_VERSION) {
        this.startPostponedEnterTransition()
    }
}

fun Fragment.setSharedElementEnterTransition(transition: Int = android.R.transition.move) {
    if (Build.VERSION.SDK_INT >= SUPPORTED_TRANSITION_VERSION) {
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(transition)
    }
}

fun Fragment.setPostponeSharedElementTransition() {
    if (Build.VERSION.SDK_INT >= SUPPORTED_TRANSITION_VERSION) {
        postponeEnterTransition()
    }
}
