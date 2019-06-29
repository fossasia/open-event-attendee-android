package org.fossasia.openevent.general.order.invoice

import android.app.DownloadManager
import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.net.Uri

const val DOWNLOAD_PATH = "downloadPath"
const val DESTINATION_PATH = "destinationPath"
const val TOKEN = "token"
const val FILENAME = "fileName"

class DownloadInvoiceService : IntentService("DownloadInvoiceService") {
    companion object {
        fun getDownloadService(
            context: Context,
            downloadPath: String,
            destinationPath: String,
            fileName: String,
            token: String
        ): Intent =
            Intent(context, DownloadInvoiceService::class.java)
                .putExtra(DOWNLOAD_PATH, downloadPath)
                .putExtra(DESTINATION_PATH, destinationPath)
                .putExtra(TOKEN, token)
                .putExtra(FILENAME, fileName)
    }
    override fun onHandleIntent(intent: Intent) {
        val downloadPath = intent.getStringExtra(DOWNLOAD_PATH)
        val destinationPath = intent.getStringExtra(DESTINATION_PATH)
        val token = intent.getStringExtra(TOKEN)
        val filename = intent.getStringExtra(FILENAME)
        startDownload(downloadPath, destinationPath, filename, token)
    }

    private fun startDownload(downloadPath: String, destinationPath: String, filename: String, token: String) {
        val uri = Uri.parse(downloadPath)
        val request = DownloadManager.Request(uri)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or
            DownloadManager.Request.NETWORK_WIFI)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setTitle(filename)
        request.setVisibleInDownloadsUi(true)
        request.setDestinationInExternalPublicDir(destinationPath, "$filename.pdf")
        request.addRequestHeader("Authorization", token)
        val manager = getSystemService(Context.DOWNLOAD_SERVICE)
        if (manager is DownloadManager) manager.enqueue(request)
    }
}
