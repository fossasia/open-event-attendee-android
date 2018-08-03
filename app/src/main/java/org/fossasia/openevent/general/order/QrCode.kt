package org.fossasia.openevent.general.order

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.journeyapps.barcodescanner.BarcodeEncoder
import timber.log.Timber

class QrCode {

    fun generateQrBitmap(text: String?, width: Int, height: Int): Bitmap? {
        try {
            val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
            return BarcodeEncoder().createBitmap(bitMatrix)
        } catch (e: WriterException) {
            Timber.d(e, "Writer Exception")
        }
        return null
    }

}