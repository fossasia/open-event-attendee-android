package org.fossasia.openevent.general.auth

import android.Manifest
import android.app.Activity
import android.arch.lifecycle.Observer
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.content.res.AppCompatResources
import android.util.Base64
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_edit_profile.view.*
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.hideSoftKeyboard
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class EditProfileFragment : Fragment() {

    private val profileViewModel by viewModel<ProfileViewModel>()
    private val editProfileViewModel by viewModel<EditProfileViewModel>()
    private lateinit var rootView: View
    private var permissionGranted = false
    private val PICK_IMAGE_REQUEST = 100
    private val READ_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    private var encodedImage: String? = null
    private val REQUEST_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        profileViewModel.user.observe(this, Observer {
            it?.let {
                val userFirstName = it.firstName.nullToEmpty()
                val userLastName = it.lastName.nullToEmpty()
                val imageUrl = it.avatarUrl.nullToEmpty()
                rootView.firstName.setText(userFirstName)
                rootView.lastName.setText(userLastName)
                if (!imageUrl.isEmpty()) { // picasso requires the imageUrl to be non empty
                    context?.let { ctx ->
                        val drawable = AppCompatResources.getDrawable(ctx, R.drawable.ic_account_circle_grey_24dp)
                        drawable?.let { icon ->
                            Picasso.get()
                                    .load(imageUrl)
                                    .placeholder(icon)
                                    .transform(CircleTransform())
                                    .into(rootView.profilePhoto)
                        }
                    }
                }
            }
        })
        profileViewModel.fetchProfile()

        editProfileViewModel.progress.observe(this, Observer {
            it?.let {
                Utils.showProgressBar(rootView.progressBar, it)
            }
        })

        rootView.profilePhoto.setOnClickListener { v ->
            if (permissionGranted) {
                showFileChooser()
            } else {
                requestPermissions(READ_STORAGE, REQUEST_CODE)
            }
        }

        rootView.buttonUpdate.setOnClickListener {
            hideSoftKeyboard(context, rootView)
            editProfileViewModel.updateProfile(encodedImage, rootView.firstName.text.toString(), rootView.lastName.text.toString())
        }

        editProfileViewModel.message.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            if (it.equals(USER_UPDATED)) {
                activity?.onBackPressed()
            }
        })

        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && intentData?.data != null) {
            val imageUri = intentData.data
            var imageStream: InputStream? = null
            try {
                imageStream = activity?.contentResolver?.openInputStream(imageUri)
            } catch (e: FileNotFoundException) {
                Timber.d(e, "File Not Found Exception")
            }

            val selectedImage = BitmapFactory.decodeStream(imageStream)
            encodedImage = encodeImage(selectedImage)

            Picasso.get()
                    .load(imageUri)
                    .placeholder(AppCompatResources.getDrawable(context!!, R.drawable.ic_person_black_24dp)!!) // TODO: Make null safe
                    .transform(CircleTransform())
                    .into(rootView.profilePhoto)
        }
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bytes = baos.toByteArray()

        return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun showFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Image"), PICK_IMAGE_REQUEST)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        activity?.supportActionBar?.title = "Edit Profile"
        setHasOptionsMenu(true)
        super.onResume()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGranted = true
                Toast.makeText(context, "Permission to Access External Storage Granted !", Toast.LENGTH_SHORT).show()
                showFileChooser()
            } else {
                Toast.makeText(context, "Permission to Access External Storage Denied :(", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)
        super.onDestroyView()
    }
}
