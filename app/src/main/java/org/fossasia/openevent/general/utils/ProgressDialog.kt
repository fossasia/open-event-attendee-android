package org.fossasia.openevent.general.utils

import android.app.Dialog
import android.content.Context
import android.view.Window
import org.fossasia.openevent.general.R

class ProgressDialog(context: Context?) {
    private val dialog = Dialog(context)
    init {
        dialog.setCancelable(false)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_progress)
    }

    fun showOrDismiss(show: Boolean) {
        if (show && !dialog.isShowing) dialog.show()
        else if (!show && dialog.isShowing) dialog.dismiss()
    }
}
