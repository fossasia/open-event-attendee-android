package org.fossasia.openevent.general.auth

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.isseiaoki.simplecropview.CropImageView
import com.isseiaoki.simplecropview.callback.CropCallback
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_crop_image.view.cropImageView
import kotlinx.android.synthetic.main.fragment_crop_image.view.tick
import kotlinx.android.synthetic.main.fragment_crop_image.view.toolbar
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.ImageUtils.encodeBitmap
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.jetbrains.anko.design.snackbar

class CropImageFragment : Fragment() {
    private lateinit var rootView: View
    private val safeArgs: CropImageFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_crop_image, container, false)
        setToolbar(activity, show = false)
        rootView.toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        Picasso.get()
            .load(safeArgs.imageToCrop)
            .into(rootView.cropImageView)
        rootView.cropImageView.setCropMode(CropImageView.CropMode.SQUARE)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootView.tick.setOnClickListener {
            val cropCallback: CropCallback = object : CropCallback {
                override fun onSuccess(cropped: Bitmap) {
                    val newEncodedImage = encodeBitmap(cropped)
                    findNavController(rootView)
                        .navigate(CropImageFragmentDirections.actionCropImageToEditProfilePop(newEncodedImage))
                }

                override fun onError(e: Throwable?) {
                    rootView.snackbar(getString(R.string.error_cropping_image_message))
                }
            }
            rootView.cropImageView.crop(null).execute(cropCallback)
        }
    }
}
