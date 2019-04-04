package org.fossasia.openevent.general.auth

import android.Manifest
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_edit_profile.view.editProfileCoordinatorLayout
import kotlinx.android.synthetic.main.fragment_edit_profile.view.updateButton
import kotlinx.android.synthetic.main.fragment_edit_profile.view.firstName
import kotlinx.android.synthetic.main.fragment_edit_profile.view.lastName
import kotlinx.android.synthetic.main.fragment_edit_profile.view.profilePhoto
import kotlinx.android.synthetic.main.fragment_edit_profile.view.progressBar
import kotlinx.android.synthetic.main.fragment_edit_profile.view.profilePhotoFab
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils.hideSoftKeyboard
import org.fossasia.openevent.general.utils.Utils.requireDrawable
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import org.fossasia.openevent.general.utils.Utils.setToolbar

class EditProfileFragment : Fragment() {

    private val profileViewModel by viewModel<ProfileViewModel>()
    private val editProfileViewModel by viewModel<EditProfileViewModel>()
    private lateinit var rootView: View
    private var permissionGranted = false
    private var encodedImage: String? = null
    private val PICK_IMAGE_REQUEST = 100
    private val READ_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    private val REQUEST_CODE = 1

    private lateinit var userFirstName: String
    private lateinit var userLastName: String
    private var avatarUpdated: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        profileViewModel.user
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                userFirstName = it.firstName.nullToEmpty()
                userLastName = it.lastName.nullToEmpty()
                val imageUrl = it.avatarUrl.nullToEmpty()
                if (rootView.firstName.text.isNullOrBlank()) {
                    rootView.firstName.setText(userFirstName)
                }
                if (rootView.lastName.text.isNullOrBlank()) {
                    rootView.lastName.setText(userLastName)
                }
                if (!imageUrl.isEmpty() && !avatarUpdated) {
                    val drawable = requireDrawable(requireContext(), R.drawable.ic_account_circle_grey)
                    Picasso.get()
                        .load(imageUrl)
                        .placeholder(drawable)
                        .transform(CircleTransform())
                        .into(rootView.profilePhoto)
                }
            })
        profileViewModel.avatarPicked.observe(this, Observer {
            if (it != null) {
                Picasso.get()
                    .load(Uri.parse(it))
                    .placeholder(requireDrawable(requireContext(), R.drawable.ic_account_circle_grey))
                    .transform(CircleTransform())
                    .into(rootView.profilePhoto)
                this.avatarUpdated = true
            }
        })
        profileViewModel.fetchProfile()

        editProfileViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.progressBar.isVisible = it
            })

        permissionGranted = (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)

        rootView.updateButton.setOnClickListener {
            hideSoftKeyboard(context, rootView)
            editProfileViewModel.updateProfile(encodedImage, rootView.firstName.text.toString(),
                rootView.lastName.text.toString())
        }

        editProfileViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                Snackbar.make(rootView.editProfileCoordinatorLayout, it, Snackbar.LENGTH_LONG).show()
                if (it == getString(R.string.user_update_success_message)) {
                    val thisActivity = activity
                    if (thisActivity is MainActivity) thisActivity.onSuperBackPressed()
                }
            })

        rootView.profilePhotoFab.setOnClickListener {
            if (permissionGranted) {
                showFileChooser()
            } else {
                requestPermissions(READ_STORAGE, REQUEST_CODE)
            }
        }

        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && intentData?.data != null) {
            val imageUri = intentData.data ?: return

            try {
                val imageStream = activity?.contentResolver?.openInputStream(imageUri)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                encodedImage = encodeImage(selectedImage)
            } catch (e: FileNotFoundException) {
                Timber.d(e, "File Not Found Exception")
            }

            Picasso.get()
                .load(imageUri)
                .placeholder(requireDrawable(requireContext(), R.drawable.ic_person_black))
                .transform(CircleTransform())
                .into(rootView.profilePhoto)
            avatarUpdated = true
            profileViewModel.avatarPicked.value = imageUri.toString()
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
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), PICK_IMAGE_REQUEST)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                handleBackPress()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        setToolbar(activity, "Edit Profile")
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
                Snackbar.make(
                    rootView.editProfileCoordinatorLayout, getString(R.string.storage_permission_granted_message),
                    Snackbar.LENGTH_SHORT).show()
                showFileChooser()
            } else {
                Snackbar.make(
                    rootView.editProfileCoordinatorLayout, getString(R.string.storage_permission_denied_message),
                    Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Handles back press when up button or back button is pressed
     */
    fun handleBackPress() {
        val thisActivity = activity
        if (!avatarUpdated && rootView.lastName.text.toString() == userLastName &&
            rootView.firstName.text.toString() == userFirstName) {
            if (thisActivity is MainActivity) thisActivity.onSuperBackPressed()
        } else {
            hideSoftKeyboard(context, rootView)
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setMessage(getString(R.string.changes_not_saved))
            dialog.setNegativeButton(getString(R.string.discard)) { _, _ ->
                if (thisActivity is MainActivity) thisActivity.onSuperBackPressed()
            }
            dialog.setPositiveButton(getString(R.string.save)) { _, _ ->
                editProfileViewModel.updateProfile(encodedImage, rootView.firstName.text.toString(),
                    rootView.lastName.text.toString())
            }
            dialog.create().show()
        }
    }

    override fun onDestroyView() {
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)
        super.onDestroyView()
    }
}
