package org.fossasia.openevent.general.speakercall

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.comment
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.commentsLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.expandSpeakerDetailButton
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.language
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.languageLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.longAbstract
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.longAbstractLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.shortAbstract
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.shortAbstractLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerAvatar
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerBioLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerCountry
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerCountryLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerEmail
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerEmailLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerInfoContainer
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerName
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerNameLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerOrganization
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerOrganizationLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerPosition
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerPositionLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerProgressBar
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerShortBio
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerWebsite
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerWebsiteLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.subTitle
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.subTitleLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.submitProposalButton
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.title
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.titleLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.trackContainer
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.trackSelector
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.ComplexBackPressFragment
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.attendees.forms.CustomForm
import org.fossasia.openevent.general.event.EventId
import org.fossasia.openevent.general.sessions.Session
import org.fossasia.openevent.general.sessions.track.Track
import org.fossasia.openevent.general.speakercall.form.SessionIdentifier
import org.fossasia.openevent.general.speakers.Speaker
import org.fossasia.openevent.general.speakers.SpeakerId
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.show
import org.fossasia.openevent.general.utils.checkEmpty
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.nullToEmpty
import org.fossasia.openevent.general.utils.setRequired
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

private const val AUDIO_TYPE = 1
private const val SLIDES_TYPE = 2
private const val VIDEO_TYPE = 3

class SpeakersCallProposalFragment : Fragment(), ComplexBackPressFragment {

    private lateinit var rootView: View
    private val speakersCallProposalViewModel by viewModel<SpeakersCallProposalViewModel>()
    private val safeArgs: SpeakersCallProposalFragmentArgs by navArgs()
    private lateinit var tracksList: List<Track>
    private var isAddingNewSession = true
    private var permissionGranted = false
    private val PICK_AUDIO_REQUEST = 100
    private val PICK_SLIDES_REQUEST = 101
    private val PICK_VIDEO_REQUEST = 102
    private val READ_STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    private val REQUEST_CODE = 1
    private var currentRequestType = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isAddingNewSession = (safeArgs.sessionId == -1L)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_speakers_call_proposal, container, false)

        Utils.setToolbar(activity, getString(R.string.proposal))
        setHasOptionsMenu(true)

        speakersCallProposalViewModel.speaker
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loadSpeakerUI(it)
            })

        speakersCallProposalViewModel.session
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loadSessionUI(it)
            })
        if (!isAddingNewSession && speakersCallProposalViewModel.session.value == null)
            speakersCallProposalViewModel.loadSession(safeArgs.sessionId)

        val progressDialog = Utils.progressDialog(context, if (isAddingNewSession)
            getString(R.string.creating_session_message) else getString(R.string.updating_session_message))

        setupCustomForms()

        speakersCallProposalViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                progressDialog.show(it)
            })

        speakersCallProposalViewModel.submitSuccess
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                if (it) {
                    rootView.snackbar(getString(R.string.proposal_creaed_updated_message))
                    findNavController(rootView).popBackStack()
                }
            })

        speakersCallProposalViewModel.speakerProgress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.speakerProgressBar.isVisible = it
            })

        speakersCallProposalViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(it)
            })
        rootView.speakerInfoContainer.isExpanded = speakersCallProposalViewModel.isSpeakerInfoShown

        rootView.titleLayout.setRequired()
        setupTrack()
        rootView.submitProposalButton.text =
            if (isAddingNewSession) getString(R.string.create_session) else getString(R.string.update_session)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentSpeaker = speakersCallProposalViewModel.speaker.value
        if (currentSpeaker == null)
            speakersCallProposalViewModel.loadSpeaker(safeArgs.speakerId)
        else
            loadSpeakerUI(currentSpeaker)

        rootView.expandSpeakerDetailButton.setOnClickListener {
            speakersCallProposalViewModel.isSpeakerInfoShown = !speakersCallProposalViewModel.isSpeakerInfoShown
            rootView.speakerInfoContainer.toggle()
        }

        rootView.submitProposalButton.setOnClickListener {
            if (rootView.title.checkEmpty(rootView.titleLayout)) {
                val proposal = Proposal(
                    title = rootView.title.text.toString(),
                    subTitle = rootView.subTitle.text.toString(),
                    language = rootView.language.text.toString(),
                    shortAbstract = rootView.shortAbstract.text.toString(),
                    longAbstract = rootView.longAbstract.text.toString(),
                    comments = rootView.comment.text.toString(),
                    track = tracksList[speakersCallProposalViewModel.trackPosition],
                    event = EventId(safeArgs.eventId),
                    speakers = mutableListOf(SpeakerId(safeArgs.speakerId))
                )

                if (isAddingNewSession)
                    speakersCallProposalViewModel.submitProposal(proposal)
                else
                    speakersCallProposalViewModel.editProposal(safeArgs.sessionId, proposal.copy(
                        id = safeArgs.sessionId
                    ))
            } else {
                rootView.snackbar(getString(R.string.fill_all_fields_message))
            }
        }
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

    override fun handleBackPress() {
        Utils.hideSoftKeyboard(context, rootView)
        AlertDialog.Builder(requireContext())
            .setMessage(getString(R.string.changes_not_saved))
            .setNegativeButton(R.string.discard) { _, _ ->
                findNavController(rootView).popBackStack()
            }.setPositiveButton(getString(R.string.continue_string)) { _, _ -> /*Do Nothing*/ }
            .create()
            .show()
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
            } else {
                rootView.snackbar(getString(R.string.permission_denied_message, getString(R.string.external_storage)))
            }
        }
    }

    private fun loadSpeakerUI(speaker: Speaker) {
        rootView.speakerInfoContainer.isVisible = true
        Picasso.get()
            .load(speaker.photoUrl)
            .placeholder(R.drawable.ic_account_circle_grey)
            .transform(CircleTransform())
            .into(rootView.speakerAvatar)

        when (speaker.name.isNullOrBlank()) {
            true -> rootView.speakerNameLayout.isVisible = false
            false -> {
                rootView.speakerNameLayout.isVisible = true
                rootView.speakerName.text = speaker.name
            }
        }
        when (speaker.email.isNullOrBlank()) {
            true -> rootView.speakerEmailLayout.isVisible = false
            false -> {
                rootView.speakerEmailLayout.isVisible = true
                rootView.speakerEmail.text = speaker.email
            }
        }
        when (speaker.organisation.isNullOrBlank()) {
            true -> rootView.speakerOrganizationLayout.isVisible = false
            false -> {
                rootView.speakerOrganizationLayout.isVisible = true
                rootView.speakerOrganization.text = speaker.organisation
            }
        }
        when (speaker.position.isNullOrBlank()) {
            true -> rootView.speakerPositionLayout.isVisible = false
            false -> {
                rootView.speakerPositionLayout.isVisible = true
                rootView.speakerPosition.text = speaker.position
            }
        }
        when (speaker.country.isNullOrBlank()) {
            true -> rootView.speakerCountryLayout.isVisible = false
            false -> {
                rootView.speakerCountryLayout.isVisible = true
                rootView.speakerCountry.text = speaker.country
            }
        }
        val bio = speaker.longBiography ?: speaker.shortBiography
        when (bio.isNullOrBlank()) {
            true -> rootView.speakerBioLayout.isVisible = false
            false -> {
                rootView.speakerBioLayout.isVisible = true
                rootView.speakerShortBio.text = bio
            }
        }
        when (speaker.website.isNullOrBlank()) {
            true -> rootView.speakerWebsiteLayout.isVisible = false
            false -> {
                rootView.speakerWebsiteLayout.isVisible = true
                rootView.speakerWebsite.text = speaker.website
                rootView.speakerWebsiteLayout.setOnClickListener {
                    Utils.openUrl(requireContext(), speaker.website)
                }
            }
        }
    }

    private fun setupTrack() {
        speakersCallProposalViewModel.tracks
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                tracksList = it
                setupTracksSpinner(it)
            })
        val currentTracks = speakersCallProposalViewModel.tracks.value
        if (currentTracks == null) {
            speakersCallProposalViewModel.loadTracks(safeArgs.eventId)
        } else {
            setupTracksSpinner(currentTracks)
            tracksList = currentTracks
        }
    }

    private fun setupTracksSpinner(tracksList: List<Track>) {
        if (tracksList.isNullOrEmpty()) {
            rootView.trackContainer.isVisible = false
            return
        }
        rootView.trackContainer.isVisible = true
        rootView.trackSelector.adapter = ArrayAdapter(requireContext(),
            android.R.layout.simple_spinner_dropdown_item, tracksList.map { it.name })
        rootView.trackSelector.setSelection(speakersCallProposalViewModel.trackPosition)
        rootView.trackSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { /*Do Nothing*/ }
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                speakersCallProposalViewModel.trackPosition = position
            }
        }
    }

    private fun setupCustomForms() {
        speakersCallProposalViewModel.forms
            .nonNull()
            .observe(viewLifecycleOwner, Observer { forms ->
                forms.forEach {
                    setupFormWithSpeakerFields(it)
                }
            })

        val currentForms = speakersCallProposalViewModel.forms.value
        if (currentForms != null)
            currentForms.forEach {
                setupFormWithSpeakerFields(it)
            }
        else
            speakersCallProposalViewModel.getFormsForProposal(safeArgs.eventId)
    }

    private fun setupFormWithSpeakerFields(form: CustomForm) {
        when (form.fieldIdentifier) {
            SessionIdentifier.TITLE -> setupField(rootView.titleLayout, form.isRequired)
            SessionIdentifier.SUBTITLE -> setupField(rootView.subTitleLayout, form.isRequired)
            SessionIdentifier.COMMENTS -> setupField(rootView.commentsLayout, form.isRequired)
            SessionIdentifier.LANGUAGE -> setupField(rootView.languageLayout, form.isRequired)
            SessionIdentifier.SHORT_ABSTRACT -> setupField(rootView.shortAbstractLayout, form.isRequired)
            SessionIdentifier.LONG_ABSTRACT -> setupField(rootView.longAbstractLayout, form.isRequired)
            SessionIdentifier.TRACK -> rootView.trackContainer.isVisible = true
            else -> return
        }
    }

    private fun setupField(layout: TextInputLayout, isRequired: Boolean) {
        layout.isVisible = true
        if (isRequired) {
            layout.setRequired()
        }
    }

    private fun loadSessionUI(session: Session) {
        rootView.title.setText(session.title.nullToEmpty())
        rootView.language.setText(session.language.nullToEmpty())
        rootView.shortAbstract.setText(session.shortAbstract.nullToEmpty())
        rootView.comment.setText(session.comments.nullToEmpty())
        rootView.longAbstract.setText(session.longAbstract.nullToEmpty())

        if (this::tracksList.isInitialized) {
            session.track?.let {
                val trackPos = tracksList.indexOf(it)
                speakersCallProposalViewModel.trackPosition = trackPos
                rootView.trackSelector.setSelection(trackPos)
            }
        }
    }
}
