package org.fossasia.openevent.general.utils

import android.app.AlertDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import org.fossasia.openevent.general.R
import timber.log.Timber

object Utils {

    fun openUrl(context: Context, link: String) {
        var url = link
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "https://$url"
        }

        CustomTabsIntent.Builder()
                .setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                .setCloseButtonIcon(BitmapFactory.decodeResource(context.resources,
                    R.drawable.ic_arrow_back_white_cct_24dp))
                .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
                .build()
                .launchUrl(context, Uri.parse(url))
    }

    fun showProgressBar(progressBar: ProgressBar, show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

    fun showNoInternetDialog(context: Context?) {
        AlertDialog.Builder(context)
            .setMessage(context?.resources?.getString(R.string.no_internet_message))
            .setPositiveButton(context?.resources?.getString(R.string.ok)) { dialog, _ -> dialog.cancel() }
            .show()
    }

    fun hideSoftKeyboard(context: Context?, view: View) {
        val inputManager: InputMethodManager = context?.getSystemService(Context.INPUT_METHOD_SERVICE)
            as InputMethodManager
        inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.SHOW_FORCED)
    }

    fun checkAndLoadFragment(
        fragmentManager: FragmentManager,
        fragment: Fragment,
        frameLayout: Int,
        addToBackStack: Boolean = true
    ) {
        val savedFragment = fragmentManager.findFragmentByTag(fragment::class.java.name)
        if (savedFragment != null) {
            loadFragment(fragmentManager, savedFragment, frameLayout, addToBackStack)
            Timber.d("Loading fragment from stack ${fragment::class.java}")
        } else {
            loadFragment(fragmentManager, fragment, frameLayout, addToBackStack)
        }
    }

    fun loadFragment(
        fragmentManager: FragmentManager,
        fragment: Fragment,
        frameLayout: Int,
        addToBackStack: Boolean = true
    ) {
        val fragmentTransaction = fragmentManager.beginTransaction()
            .replace(frameLayout, fragment, fragment::class.java.name)
        if (addToBackStack) fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    fun requireDrawable(@NonNull context: Context, @DrawableRes resId: Int) = AppCompatResources
        .getDrawable(context, resId) ?: throw IllegalStateException("Drawable should not be null")
}
