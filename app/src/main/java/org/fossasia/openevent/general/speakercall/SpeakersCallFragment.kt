package org.fossasia.openevent.general.speakercall

import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_speakers_call.view.speakersCallDescription
import kotlinx.android.synthetic.main.fragment_speakers_call.view.speakersCallEmptyView
import kotlinx.android.synthetic.main.fragment_speakers_call.view.timeStatus
import kotlinx.android.synthetic.main.fragment_speakers_call.view.speakersCallTimeDetail
import kotlinx.android.synthetic.main.fragment_speakers_call.view.speakersCallContainer
import kotlinx.android.synthetic.main.fragment_speakers_call.view.submitProposalButton
import kotlinx.android.synthetic.main.fragment_speakers_call.view.progressBar
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EventUtils.getEventDateTime
import org.fossasia.openevent.general.event.EventUtils.getFormattedDate
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.stripHtml
import org.jetbrains.anko.design.snackbar
import org.threeten.bp.DateTimeUtils
import java.util.Date

const val SPEAKERS_CALL_FRAGMENT = "speakersCallFragment"

class SpeakersCallFragment : Fragment() {

    private lateinit var rootView: View
    private val speakersCallViewModel by viewModel<SpeakersCallViewModel>()
    private val safeArgs: SpeakersCallFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_speakers_call, container, false)

        setToolbar(activity, getString(R.string.call_for_speakers))
        setHasOptionsMenu(true)

        speakersCallViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(it)
            })

        speakersCallViewModel.emptySpeakersCall
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                showEmptyView(it)
            })
        showEmptyView(speakersCallViewModel.emptySpeakersCall.value ?: true)

        speakersCallViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.progressBar.isVisible = it
            })

        speakersCallViewModel.speakersCall
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loadSpeakersCallSection(it)
                showEmptyView(false)
            })

        val currentSpeakersCall = speakersCallViewModel.speakersCall.value
        if (currentSpeakersCall == null) {
            speakersCallViewModel.loadSpeakersCall(safeArgs.eventId)
        } else {
            loadSpeakersCallSection(currentSpeakersCall)
            showEmptyView(false)
        }

        if (speakersCallViewModel.isLoggedIn()) {
            val currentUser = speakersCallViewModel.user.value
            if (currentUser == null) {
                speakersCallViewModel.loadMyUserAndSpeaker(safeArgs.eventId, safeArgs.eventIdentifier)
            } else {
                speakersCallViewModel.loadMySpeaker(currentUser, safeArgs.eventId, safeArgs.eventIdentifier)
            }
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rootView.submitProposalButton.setOnClickListener {
            if (speakersCallViewModel.isLoggedIn()) {
                val currentSpeaker = speakersCallViewModel.speaker.value
                if (currentSpeaker == null)
                    showCreateSpeakerDialog()
                else
                    showEditSpeakerOrCreateProposalDialog(currentSpeaker.id)
            } else {
                redirectToLogin()
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

    private fun showEditSpeakerOrCreateProposalDialog(speakerId: Long) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.create_edit_speaker))
            .setPositiveButton(getString(R.string.create_proposal)) { _, _ ->
                findNavController(rootView).navigate(SpeakersCallFragmentDirections
                    .actionSpeakersCallToProposal(safeArgs.eventId, speakerId))
            }
            .setNegativeButton(getString(R.string.edit_speaker)) { _, _ ->
                findNavController(rootView).navigate(SpeakersCallFragmentDirections
                    .actionSpeakersCallToAddSpeaker(safeArgs.eventId, speakerId))
            }
        speakersCallViewModel.sessions.value?.let { sessions ->
            if (sessions.isEmpty()) return@let

            dialogBuilder.setItems(sessions.map { getString(R.string.edit_session_name, it.title) }
                .toTypedArray()) { _, position ->
                findNavController(rootView).navigate(SpeakersCallFragmentDirections
                    .actionSpeakersCallToProposal(safeArgs.eventId, speakerId, sessions[position].id))
            }
        }
        dialogBuilder.create().show()
    }

    private fun showCreateSpeakerDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.create_speaker))
            .setMessage(getString(R.string.create_speaker_profile_message))
            .setPositiveButton(getString(R.string.create_speaker)) { _, _ ->
                findNavController(rootView)
                    .navigate(SpeakersCallFragmentDirections.actionSpeakersCallToAddSpeaker(safeArgs.eventId))
            }
            .create()
            .show()
    }

    private fun redirectToLogin() {
        rootView.snackbar(getString(R.string.log_in_first))
        Navigation.findNavController(rootView).navigate(
            SpeakersCallFragmentDirections
                .actionSpeakersCallToAuth(getString(R.string.log_in_first), SPEAKERS_CALL_FRAGMENT))
    }

    private fun showEmptyView(show: Boolean) {
        rootView.speakersCallContainer.isVisible = !show
        rootView.speakersCallEmptyView.isVisible = show
        if (show) rootView.submitProposalButton.isVisible = false
    }

    private fun loadSpeakersCallSection(speakersCall: SpeakersCall) {
        val startAt = getEventDateTime(speakersCall.startsAt, safeArgs.timeZone)
        val endAt = getEventDateTime(speakersCall.endsAt, safeArgs.timeZone)
        val startTime: Date = DateTimeUtils.toDate(startAt.toInstant())
        val endTime: Date = DateTimeUtils.toDate(endAt.toInstant())
        val currentTime = Date()

        rootView.speakersCallDescription.text = speakersCall.announcement.stripHtml()
        if (currentTime < startTime) {
            rootView.timeStatus.isVisible = false
            rootView.speakersCallTimeDetail.text = getString(R.string.speakers_call_open_at, getFormattedDate(startAt))
            rootView.submitProposalButton.isVisible = false
        } else if (startTime < currentTime && currentTime < endTime) {
            rootView.timeStatus.setImageDrawable(resources.getDrawable(R.drawable.ic_speakers_call_open))
            rootView.speakersCallTimeDetail.text = getString(R.string.speakers_call_open_until, getFormattedDate(endAt))
            rootView.submitProposalButton.isVisible = true
        } else {
            rootView.timeStatus.setImageDrawable(resources.getDrawable(R.drawable.ic_speakers_call_closed))
            rootView.speakersCallTimeDetail.text = getString(R.string.speakers_call_closed, getFormattedDate(endAt))
            rootView.submitProposalButton.isVisible = false
        }
    }
}
