package org.fossasia.openevent.general.auth

import android.Manifest
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import org.fossasia.openevent.general.utils.ImageUtils.decodeBitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_edit_profile.view.updateButton
import kotlinx.android.synthetic.main.fragment_edit_profile.view.toolbar
import kotlinx.android.synthetic.main.fragment_edit_profile.view.firstName
import kotlinx.android.synthetic.main.fragment_edit_profile.view.details
import kotlinx.android.synthetic.main.fragment_edit_profile.view.facebook
import kotlinx.android.synthetic.main.fragment_edit_profile.view.twitter
import kotlinx.android.synthetic.main.fragment_edit_profile.view.instagram
import kotlinx.android.synthetic.main.fragment_edit_profile.view.phone
import com.squareup.picasso.MemoryPolicy
import kotlinx.android.synthetic.main.dialog_edit_profile_image.view.editImage
import kotlinx.android.synthetic.main.dialog_edit_profile_image.view.takeImage
import kotlinx.android.synthetic.main.dialog_edit_profile_image.view.replaceImage
import kotlinx.android.synthetic.main.dialog_edit_profile_image.view.removeImage
import kotlinx.android.synthetic.main.fragment_edit_profile.view.lastName
import kotlinx.android.synthetic.main.fragment_edit_profile.view.profilePhoto
import kotlinx.android.synthetic.main.fragment_edit_profile.view.progressBar
import kotlinx.android.synthetic.main.fragment_edit_profile.view.profilePhotoFab
import kotlinx.android.synthetic.main.fragment_edit_profile.view.firstNameLayout
import kotlinx.android.synthetic.main.fragment_edit_profile.view.lastNameLayout
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.MainActivity
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.RotateBitmap
import org.fossasia.openevent.general.ComplexBackPressFragment
import org.fossasia.openevent.general.utils.Utils.hideSoftKeyboard
import org.fossasia.openevent.general.utils.Utils.requireDrawable
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.nullToEmpty
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.FileNotFoundException
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.emptyToNull
import org.fossasia.openevent.general.utils.setRequired
import org.jetbrains.anko.design.snackbar

class EditProfileFragment : Fragment(), ComplexBackPressFragment {

    private val profileViewModel by viewModel<ProfileViewModel>()
    private val editProfileViewModel by viewModel<EditProfileViewModel>()
    private val safeArgs: EditProfileFragmentArgs by navArgs()
    private lateinit var rootView: View
    private var storagePermissionGranted = false
    private val PICK_IMAGE_REQUEST = 100
    private val READ_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    private val READ_STORAGE_REQUEST_CODE = 1

    private var cameraPermissionGranted = false
    private val TAKE_IMAGE_REQUEST = 101
    private val CAMERA_REQUEST = arrayOf(Manifest.permission.CAMERA)
    private val CAMERA_REQUEST_CODE = 2

    private lateinit var userFirstName: String
    private lateinit var userLastName: String
    private lateinit var userDetails: String
    private lateinit var userAvatar: String
    private lateinit var userPhone: String
    private lateinit var userFacebook: String
    private lateinit var userTwitter: String
    private lateinit var userInstagram: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.fragment_edit_profile, container, false)

        setToolbar(activity, show = false)
        rootView.toolbar.setNavigationOnClickListener {
            handleBackPress()
        }

        profileViewModel.user
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loadUserUI(it)
            })

        val currentUser = editProfileViewModel.user.value
        if (currentUser == null) profileViewModel.getProfile() else loadUserUI(currentUser)

        editProfileViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.progressBar.isVisible = it
            })

        editProfileViewModel.getUpdatedTempFile()
            .nonNull()
            .observe(viewLifecycleOwner, Observer { file ->
                // prevent picasso from storing tempAvatar cache,
                // if user select another image picasso will display tempAvatar instead of its own cache
                Picasso.get()
                    .load(file)
                    .placeholder(requireDrawable(requireContext(), R.drawable.ic_person_black))
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .transform(CircleTransform())
                    .into(rootView.profilePhoto)
            })

        storagePermissionGranted = (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        cameraPermissionGranted = (ContextCompat.checkSelfPermission(requireContext(),
            Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)

        rootView.updateButton.setOnClickListener {
            hideSoftKeyboard(context, rootView)
            if (isValidInput()) {
                updateUser()
            } else {
                rootView.snackbar(getString(R.string.fill_required_fields_message))
            }
        }

        editProfileViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(it)
                if (it == getString(R.string.user_update_success_message)) {
                    val thisActivity = activity
                    if (thisActivity is MainActivity) thisActivity.onSuperBackPressed()
                }
            })

        rootView.profilePhotoFab.setOnClickListener {
            showEditPhotoDialog()
        }

        rootView.firstNameLayout.setRequired()
        rootView.lastNameLayout.setRequired()

        return rootView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)
        if (resultCode != Activity.RESULT_OK) return

        if (requestCode == PICK_IMAGE_REQUEST && intentData?.data != null) {
            val imageUri = intentData.data ?: return

            try {
                val selectedImage = RotateBitmap().handleSamplingAndRotationBitmap(requireContext(), imageUri)
                editProfileViewModel.encodedImage = selectedImage?.let { encodeImage(it) }
                editProfileViewModel.avatarUpdated = true
            } catch (e: FileNotFoundException) {
                Timber.d(e, "File Not Found Exception")
            }
        } else if (requestCode == TAKE_IMAGE_REQUEST) {
            val imageBitmap = intentData?.extras?.get("data")
            if (imageBitmap is Bitmap) {
                editProfileViewModel.encodedImage = imageBitmap.let { encodeImage(it) }
                editProfileViewModel.avatarUpdated = true
            }
        }
    }

    private fun isValidInput(): Boolean {
        var valid = true
        if (rootView.firstName.text.isNullOrBlank()) {
            rootView.firstName.error = getString(R.string.empty_field_error_message)
            valid = false
        }
        if (rootView.lastName.text.isNullOrBlank()) {
            rootView.lastName.error = getString(R.string.empty_field_error_message)
            valid = false
        }
        if (!rootView.instagram.text.isNullOrEmpty() && !Patterns.WEB_URL.matcher(rootView.instagram.text).matches()) {
            rootView.instagram.error = getString(R.string.invalid_url_message)
            valid = false
        }
        if (!rootView.facebook.text.isNullOrEmpty() && !Patterns.WEB_URL.matcher(rootView.facebook.text).matches()) {
            rootView.facebook.error = getString(R.string.invalid_url_message)
            valid = false
        }
        if (!rootView.twitter.text.isNullOrEmpty() && !Patterns.WEB_URL.matcher(rootView.twitter.text).matches()) {
            rootView.twitter.error = getString(R.string.invalid_url_message)
            valid = false
        }
        return valid
    }

    private fun loadUserUI(user: User) {
        userFirstName = user.firstName.nullToEmpty()
        userLastName = user.lastName.nullToEmpty()
        userDetails = user.details.nullToEmpty()
        userAvatar = user.avatarUrl.nullToEmpty()
        userPhone = user.contact.nullToEmpty()
        userFacebook = user.facebookUrl.nullToEmpty()
        userTwitter = user.twitterUrl.nullToEmpty()
        userInstagram = user.instagramUrl.nullToEmpty()

        if (safeArgs.croppedImage.isEmpty()) {
            if (userAvatar.isNotEmpty() && !editProfileViewModel.avatarUpdated) {
                val drawable = requireDrawable(requireContext(), R.drawable.ic_account_circle_grey)
                Picasso.get()
                    .load(userAvatar)
                    .placeholder(drawable)
                    .transform(CircleTransform())
                    .into(rootView.profilePhoto)
            }
        } else {
            val croppedImage = decodeBitmap(safeArgs.croppedImage)
            editProfileViewModel.encodedImage = encodeImage(croppedImage)
            editProfileViewModel.avatarUpdated = true
        }
        setTextIfNull(rootView.firstName, userFirstName)
        setTextIfNull(rootView.lastName, userLastName)
        setTextIfNull(rootView.details, userDetails)
        setTextIfNull(rootView.phone, userPhone)
        setTextIfNull(rootView.facebook, userFacebook)
        setTextIfNull(rootView.twitter, userTwitter)
        setTextIfNull(rootView.instagram, userInstagram)
    }

    private fun setTextIfNull(input: TextInputEditText, text: String) {
        if (input.text.isNullOrBlank()) input.setText(text)
    }

    private fun showEditPhotoDialog() {
        val editImageView = layoutInflater.inflate(R.layout.dialog_edit_profile_image, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(editImageView)
            .create()

        editImageView.editImage.setOnClickListener {

            if (!userAvatar.isNullOrEmpty()) {
                if (this::userAvatar.isInitialized) {
                    findNavController(rootView).navigate(
                        EditProfileFragmentDirections.actionEditProfileToCropImage(userAvatar))
                } else {
                    rootView.snackbar(getString(R.string.error_editting_image_message))
                }
            } else {
                rootView.snackbar(getString(R.string.image_not_found))
            }

            dialog.cancel()
        }

        editImageView.removeImage.setOnClickListener {
            dialog.cancel()
            clearAvatar()
        }

        editImageView.takeImage.setOnClickListener {
            dialog.cancel()
            if (cameraPermissionGranted) {
                takeImage()
            } else {
                requestPermissions(CAMERA_REQUEST, CAMERA_REQUEST_CODE)
            }
        }

        editImageView.replaceImage.setOnClickListener {
            dialog.cancel()
            if (storagePermissionGranted) {
                showFileChooser()
            } else {
                requestPermissions(READ_STORAGE, READ_STORAGE_REQUEST_CODE)
            }
        }
        dialog.show()
    }

    private fun clearAvatar() {
        val drawable = requireDrawable(requireContext(), R.drawable.ic_account_circle_grey)
        Picasso.get()
            .load(R.drawable.ic_account_circle_grey)
            .placeholder(drawable)
            .transform(CircleTransform())
            .into(rootView.profilePhoto)
        editProfileViewModel.encodedImage = encodeImage(drawable.toBitmap(120, 120))
        editProfileViewModel.avatarUpdated = true
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val bytes = baos.toByteArray()

        // create temp file
        try {

            val tempAvatar = File(context?.cacheDir, "tempAvatar")
            if (tempAvatar.exists()) {
                tempAvatar.delete()
            }
            val fos = FileOutputStream(tempAvatar)
            fos.write(bytes)
            fos.flush()
            fos.close()

            editProfileViewModel.setUpdatedTempFile(tempAvatar)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun takeImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, TAKE_IMAGE_REQUEST)
    }

    private fun showFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), PICK_IMAGE_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == READ_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                storagePermissionGranted = true
                rootView.snackbar(getString(R.string.permission_granted_message, getString(R.string.external_storage)))
                showFileChooser()
            } else {
                rootView.snackbar(getString(R.string.permission_denied_message, getString(R.string.external_storage)))
            }
        } else if (requestCode == CAMERA_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraPermissionGranted = true
                rootView.snackbar(getString(R.string.permission_granted_message, getString(R.string.camera)))
                takeImage()
            } else {
                rootView.snackbar(getString(R.string.permission_denied_message, getString(R.string.camera)))
            }
        }
    }

    /**
     * Handles back press when up button or back button is pressed
     */
    override fun handleBackPress() {
        val thisActivity = activity
        if (noDataChanged()) {
            findNavController(rootView).popBackStack()
        } else {
            hideSoftKeyboard(context, rootView)
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setMessage(getString(R.string.changes_not_saved))
            dialog.setNegativeButton(getString(R.string.discard)) { _, _ ->
                if (thisActivity is MainActivity) thisActivity.onSuperBackPressed()
            }
            dialog.setPositiveButton(getString(R.string.save)) { _, _ ->
                if (isValidInput()) {
                    updateUser()
                } else {
                    rootView.snackbar(getString(R.string.fill_required_fields_message))
                } }
            dialog.create().show()
        }
    }

    private fun updateUser() {
        val newUser = User(
            id = editProfileViewModel.getId(),
            firstName = rootView.firstName.text.toString(),
            lastName = rootView.lastName.text.toString(),
            details = rootView.details.text.toString(),
            facebookUrl = rootView.facebook.text.toString().emptyToNull(),
            twitterUrl = rootView.twitter.text.toString().emptyToNull(),
            contact = rootView.phone.text.toString().emptyToNull()
        )
        editProfileViewModel.updateProfile(newUser)
    }

    private fun noDataChanged() = !editProfileViewModel.avatarUpdated &&
        rootView.lastName.text.toString() == userLastName &&
        rootView.firstName.text.toString() == userFirstName &&
        rootView.details.text.toString() == userDetails &&
        rootView.facebook.text.toString() == userFacebook &&
        rootView.twitter.text.toString() == userTwitter &&
        rootView.instagram.text.toString() == userInstagram &&
        rootView.phone.text.toString() == userPhone

    override fun onDestroyView() {
        val activity = activity as? AppCompatActivity
        activity?.supportActionBar?.setDisplayHomeAsUpEnabled(false)
        setHasOptionsMenu(false)
        super.onDestroyView()
    }
}
