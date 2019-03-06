package org.fossasia.openevent.general.order

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_qrcode.view.qrCodeView

private const val QR_BITMAP: String = "QR_BITMAP"

class QRCodeFragment : Fragment() {

    private lateinit var rootView: View
    private lateinit var bitmap: Bitmap
    private var currentBrightnessValue: Int = 50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = this.arguments
        if (bundle != null) {
            bitmap = bundle.getParcelable(QR_BITMAP)
        }

        currentBrightnessValue = android.provider.Settings.System.getInt(
            context?.contentResolver,
            android.provider.Settings.System.SCREEN_BRIGHTNESS)

        val layoutParams = activity?.window?.attributes
        layoutParams?.screenBrightness = 1f
        activity?.window?.attributes = layoutParams
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(org.fossasia.openevent.general.R.layout.fragment_qrcode, container, false)

        rootView.qrCodeView.setImageBitmap(bitmap)

        return rootView
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val layoutParams = activity?.window?.attributes
        layoutParams?.screenBrightness = currentBrightnessValue / 100f
        activity?.window?.attributes = layoutParams
    }
}
