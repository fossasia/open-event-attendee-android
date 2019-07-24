package org.fossasia.openevent.general.speakercall

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_proposal_speaker.view.speakerName
import kotlinx.android.synthetic.main.fragment_proposal_speaker.view.speakerImage
import kotlinx.android.synthetic.main.fragment_proposal_speaker.view.speakerOrganization
import kotlinx.android.synthetic.main.fragment_proposal_speaker.view.speakerEmail
import kotlinx.android.synthetic.main.fragment_proposal_speaker.view.speakerPosition
import kotlinx.android.synthetic.main.fragment_proposal_speaker.view.speakerShortBio
import kotlinx.android.synthetic.main.fragment_proposal_speaker.view.speakerWebsite
import kotlinx.android.synthetic.main.fragment_proposal_speaker.view.speakerTwitter
import kotlinx.android.synthetic.main.fragment_proposal_speaker.view.submitButton
import kotlinx.android.synthetic.main.fragment_proposal_speaker.view.speakerEmailLayout
import kotlinx.android.synthetic.main.fragment_proposal_speaker.view.speakerNameLayout
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.ComplexBackPressFragment
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.RotateBitmap
import org.fossasia.openevent.general.auth.User
import org.fossasia.openevent.general.auth.UserId
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.speakers.Speaker
import org.fossasia.openevent.general.utils.Utils.progressDialog
import org.fossasia.openevent.general.utils.Utils.show
import org.fossasia.openevent.general.utils.Utils.hideSoftKeyboard
import org.fossasia.openevent.general.utils.Utils.requireDrawable
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.checkEmpty
import org.fossasia.openevent.general.utils.emptyToNull
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.nullToEmpty
import org.fossasia.openevent.general.utils.setRequired
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber
import java.io.FileNotFoundException
import java.io.File
import java.io.IOException
import java.io.FileOutputStream
import java.io.ByteArrayOutputStream

class EditSpeakerFragment : Fragment(), ComplexBackPressFragment {
    private lateinit var rootView: View
    private val editSpeakerViewModel by viewModel<EditSpeakerViewModel>()
    private val safeArgs: EditSpeakerFragmentArgs by navArgs()
    private var isCreatingNewSpeaker = true
    private var permissionGranted = false
    private val PICK_IMAGE_REQUEST = 100
    private val READ_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    private val REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCreatingNewSpeaker = (safeArgs.speakerId == -1L)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_proposal_speaker, container, false)

        setToolbar(activity, getString(R.string.proposal_speaker))
        setHasOptionsMenu(true)

        editSpeakerViewModel.user
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                autoFillByUserInformation(it)
            })

        val currentSpeaker = editSpeakerViewModel.speaker.value
        if (currentSpeaker == null) {
            if (isCreatingNewSpeaker) {
                editSpeakerViewModel.user.value?.let {
                    autoFillByUserInformation(it)
                } ?: editSpeakerViewModel.loadUser(editSpeakerViewModel.getId())
            } else {
                editSpeakerViewModel.loadSpeaker(safeArgs.speakerId)
            }
        } else loadSpeakerUI(currentSpeaker)

        editSpeakerViewModel.speaker
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loadSpeakerUI(it)
            })

        val progressDialog = progressDialog(context)
        editSpeakerViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        editSpeakerViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(it)
            })

        editSpeakerViewModel.submitSuccess
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it)
                    findNavController(rootView).popBackStack()
            })

        rootView.speakerNameLayout.setRequired()
        rootView.speakerEmailLayout.setRequired()
        rootView.submitButton.text = getString(if (isCreatingNewSpeaker)
            R.string.add_speaker else R.string.edit_speaker)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editSpeakerViewModel.getUpdatedTempFile()
            .nonNull()
            .observe(viewLifecycleOwner, Observer { file ->
                Picasso.get()
                    .load(file)
                    .placeholder(requireDrawable(requireContext(), R.drawable.ic_person_black))
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .transform(CircleTransform())
                    .into(rootView.speakerImage)
            })

        rootView.speakerImage.setOnClickListener {
            if (permissionGranted) {
                showFileChooser()
            } else {
                requestPermissions(READ_STORAGE, REQUEST_CODE)
            }
        }

        rootView.submitButton.setOnClickListener {
            if (!checkSpeakerSuccess()) return@setOnClickListener
            val speaker = Speaker(
                id = editSpeakerViewModel.speaker.value?.id ?: editSpeakerViewModel.getId(),
                name = rootView.speakerName.text.toString(),
                email = rootView.speakerEmail.text.toString(),
                organisation = rootView.speakerOrganization.text.toString(),
                position = rootView.speakerPosition.text.toString(),
                shortBiography = rootView.speakerShortBio.text.toString(),
                website = rootView.speakerWebsite.text.toString().emptyToNull(),
                twitter = rootView.speakerTwitter.text.toString().emptyToNull(),
                event = EventId(safeArgs.eventId),
                user = UserId(editSpeakerViewModel.getId())
            )
            if (isCreatingNewSpeaker)
                editSpeakerViewModel.submitSpeaker(speaker)
            else
                editSpeakerViewModel.editSpeaker(speaker)
        }
    }

    override fun handleBackPress() {
        hideSoftKeyboard(context, rootView)
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.changes_not_saved))
            .setNegativeButton(R.string.discard) { _, _ ->
                findNavController(rootView).popBackStack()
            }
            .setPositiveButton(getString(R.string.continue_string)) { _, _ -> /*Do Nothing*/ }
            .create()
            .show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && intentData?.data != null) {
            val imageUri = intentData.data ?: return

            try {
                val selectedImage = RotateBitmap().handleSamplingAndRotationBitmap(requireContext(), imageUri)
                editSpeakerViewModel.encodedImage = selectedImage?.let { encodeImage(it) }
            } catch (e: FileNotFoundException) {
                Timber.d(e, "File Not Found Exception")
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                permissionGranted = true
                rootView.snackbar(getString(R.string.permission_granted_message, getString(R.string.external_storage)))
                showFileChooser()
            } else {
                rootView.snackbar(getString(R.string.permission_denied_message, getString(R.string.external_storage)))
            }
        }
    }

    private fun showFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, getString(R.string.select_image)), PICK_IMAGE_REQUEST)
    }

    private fun autoFillByUserInformation(user: User) {
        rootView.speakerName.setText("${user.firstName.nullToEmpty()} ${user.lastName.nullToEmpty()}")
        rootView.speakerEmail.setText(user.email)
        rootView.speakerShortBio.setText(user.details)
        Picasso.get()
            .load(user.avatarUrl)
            .placeholder(R.drawable.ic_account_circle_grey)
            .transform(CircleTransform())
            .into(rootView.speakerImage)
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

            editSpeakerViewModel.setUpdatedTempFile(tempAvatar)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return "data:image/jpeg;base64," + Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun checkSpeakerSuccess(): Boolean =
        rootView.speakerEmail.checkEmpty() && rootView.speakerName.checkEmpty()

    private fun loadSpeakerUI(speaker: Speaker) {
        Picasso.get()
            .load(speaker.photoUrl)
            .placeholder(R.drawable.ic_account_circle_grey)
            .transform(CircleTransform())
            .into(rootView.speakerImage)
        rootView.speakerName.setText(speaker.name)
        rootView.speakerEmail.setText(speaker.email)
        rootView.speakerOrganization.setText(speaker.organisation)
        rootView.speakerPosition.setText(speaker.position)
        rootView.speakerShortBio.setText(speaker.shortBiography)
        rootView.speakerWebsite.setText(speaker.website)
        rootView.speakerTwitter.setText(speaker.twitter)
    }
}
