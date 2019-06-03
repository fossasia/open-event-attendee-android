package org.fossasia.openevent.general.speakercall

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerAvatar
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerNameLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerEmailLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerBioLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerOrganizationLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerCountryLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerPositionLayout
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerName
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerEmail
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerShortBio
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerOrganization
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerCountry
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerPosition
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerProgressBar
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerInfoContainer
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.expandSpeakerDetailButton
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerWebsite
import kotlinx.android.synthetic.main.fragment_speakers_call_proposal.view.speakerWebsiteLayout
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.ComplexBackPressFragment
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.speakers.Speaker
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.koin.androidx.viewmodel.ext.android.viewModel

class SpeakersCallProposalFragment : Fragment(), ComplexBackPressFragment {

    private lateinit var rootView: View
    private val speakersCallProposalViewModel by viewModel<SpeakersCallProposalViewModel>()
    private val safeArgs: SpeakersCallProposalFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_speakers_call_proposal, container, false)

        Utils.setToolbar(activity, getString(R.string.proposal))
        setHasOptionsMenu(true)

        speakersCallProposalViewModel.speaker
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                loadSpeakerUI(it)
            })

        speakersCallProposalViewModel.speakerProgress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.speakerProgressBar.isVisible = it
            })
        rootView.speakerInfoContainer.isVisible = speakersCallProposalViewModel.isSpeakerInfoShown

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
            rootView.speakerInfoContainer.isVisible = speakersCallProposalViewModel.isSpeakerInfoShown
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
            }
            .setPositiveButton(getString(R.string.continue_string)) { _, _ -> /*Do Nothing*/ }
            .create()
            .show()
    }

    private fun loadSpeakerUI(speaker: Speaker) {
        rootView.speakerInfoContainer.visibility = View.VISIBLE
        Picasso.get()
            .load(speaker.photoUrl)
            .placeholder(R.drawable.ic_account_circle_grey)
            .transform(CircleTransform())
            .into(rootView.speakerAvatar)

        when (speaker.name.isNullOrBlank()) {
            true -> rootView.speakerNameLayout.visibility = View.GONE
            false -> {
                rootView.speakerNameLayout.visibility = View.VISIBLE
                rootView.speakerName.text = speaker.name
            }
        }
        when (speaker.email.isNullOrBlank()) {
            true -> rootView.speakerEmailLayout.visibility = View.GONE
            false -> {
                rootView.speakerEmailLayout.visibility = View.VISIBLE
                rootView.speakerEmail.text = speaker.email
            }
        }
        when (speaker.organisation.isNullOrBlank()) {
            true -> rootView.speakerOrganizationLayout.visibility = View.GONE
            false -> {
                rootView.speakerOrganizationLayout.visibility = View.VISIBLE
                rootView.speakerOrganization.text = speaker.organisation
            }
        }
        when (speaker.position.isNullOrBlank()) {
            true -> rootView.speakerPositionLayout.visibility = View.GONE
            false -> {
                rootView.speakerPositionLayout.visibility = View.VISIBLE
                rootView.speakerPosition.text = speaker.position
            }
        }
        when (speaker.country.isNullOrBlank()) {
            true -> rootView.speakerCountryLayout.visibility = View.GONE
            false -> {
                rootView.speakerCountryLayout.visibility = View.VISIBLE
                rootView.speakerCountry.text = speaker.country
            }
        }
        val bio = speaker.longBiography ?: speaker.shortBiography
        when (bio.isNullOrBlank()) {
            true -> rootView.speakerBioLayout.visibility = View.GONE
            false -> {
                rootView.speakerBioLayout.visibility = View.VISIBLE
                rootView.speakerShortBio.text = bio
            }
        }
        when (speaker.website.isNullOrBlank()) {
            true -> rootView.speakerWebsiteLayout.visibility = View.GONE
            false -> {
                rootView.speakerWebsiteLayout.visibility = View.VISIBLE
                rootView.speakerWebsite.text = speaker.website
                rootView.speakerWebsiteLayout.setOnClickListener {
                    Utils.openUrl(requireContext(), speaker.website)
                }
            }
        }
    }
}
