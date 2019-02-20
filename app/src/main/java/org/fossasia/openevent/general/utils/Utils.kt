package org.fossasia.openevent.general.utils

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import androidx.annotation.DrawableRes
import androidx.annotation.NonNull
import androidx.appcompat.content.res.AppCompatResources
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
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
                    R.drawable.ic_arrow_back_white_cct))
                .setStartAnimations(context, R.anim.slide_in_right, R.anim.slide_out_left)
                .setExitAnimations(context, R.anim.slide_in_left, R.anim.slide_out_right)
                .build()
                .launchUrl(context, Uri.parse(url))
    }

    fun showNoInternetDialog(context: Context?) {
        AlertDialog.Builder(context)
            .setMessage(context?.resources?.getString(R.string.no_internet_message))
            .setPositiveButton(context?.resources?.getString(R.string.ok)) { dialog, _ -> dialog.cancel() }
            .show()
    }

    fun isNetworkConnected(context: Context?): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        return connectivityManager?.activeNetworkInfo != null
    }

    fun progressDialog(context: Context?): ProgressDialog {
        val dialog = ProgressDialog(context)
        dialog.setCancelable(false)
        dialog.setMessage("Loading...")
        return dialog
    }

    fun ProgressDialog.show(show: Boolean) {
        if (show) this.show()
        else this.dismiss()
    }

    fun showSoftKeyboard(context: Context?, view: View) {
        view.requestFocus()
        val manager = context?.getSystemService(Context.INPUT_METHOD_SERVICE)
        if (manager is InputMethodManager) manager.toggleSoftInputFromWindow(view.windowToken,
            InputMethodManager.RESULT_UNCHANGED_SHOWN, InputMethodManager.RESULT_UNCHANGED_HIDDEN)
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

    fun getAnimFade(): NavOptions {
        val builder = NavOptions.Builder()
        builder.setEnterAnim(R.anim.fade_in)
        builder.setExitAnim(R.anim.fade_out)
        builder.setPopEnterAnim(R.anim.fade_in)
        builder.setPopExitAnim(R.anim.fade_out)
        return builder.build()
    }

    fun getAnimSlide(): NavOptions {
        val builder = NavOptions.Builder()
        builder.setEnterAnim(R.anim.slide_in_right)
        builder.setExitAnim(R.anim.slide_out_left)
        builder.setPopEnterAnim(R.anim.slide_in_left)
        builder.setPopExitAnim(R.anim.slide_out_right)
        return builder.build()
    }

    fun navAnimVisible(navigation: BottomNavigationView?, context: Context) {
        if (navigation?.visibility == View.GONE) {
            navigation.visibility = View.VISIBLE
            navigation.animation = AnimationUtils.loadAnimation(context, R.anim.slide_up)
        }
    }

    fun navAnimGone(navigation: BottomNavigationView?, context: Context) {
        if (navigation?.visibility == View.VISIBLE) {
            navigation.visibility = View.GONE
            navigation.animation = AnimationUtils.loadAnimation(context, R.anim.slide_down)
        }
    }
}
