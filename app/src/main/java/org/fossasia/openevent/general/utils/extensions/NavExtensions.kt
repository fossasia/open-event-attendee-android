package org.fossasia.openevent.general.utils.extensions

import android.os.Bundle
import android.view.View
import androidx.navigation.NavOptions
import org.fossasia.openevent.general.utils.Utils.getAnimSlide
import androidx.navigation.Navigation.findNavController

fun Bundle?.navigateWithBundleTo(
    controllerView: View,
    destResId: Int,
    navOptions: NavOptions = getAnimSlide()
) {

    findNavController(controllerView).navigate(destResId, this, navOptions)
}

fun navigateTo(
    controllerView: View,
    destResId: Int,
    navOptions: NavOptions = getAnimSlide()
) {
    findNavController(controllerView).navigate(destResId, null, navOptions)
}
