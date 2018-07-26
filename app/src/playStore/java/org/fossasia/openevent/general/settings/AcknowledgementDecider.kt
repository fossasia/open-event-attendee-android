package org.fossasia.openevent.general.settings

import android.content.Context
import android.content.Intent
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity

object AcknowledgementDecider {

    fun decide(context: Context) {
        val intent = Intent(context, OssLicensesMenuActivity::class.java)
        context.startActivity(intent)
    }

    fun showAcknowledgement(): Boolean {
        return true
    }
}
