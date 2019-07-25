package org.fossasia.openevent.general.speakers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_speaker.view.*
import org.fossasia.openevent.general.CircleTransform
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils.openUrl
import org.fossasia.openevent.general.utils.Utils.requireDrawable
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.fossasia.openevent.general.utils.stripHtml
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class SpeakerFragment : Fragment() {
    private lateinit var rootView: View
    private val speakerViewModel by viewModel<SpeakerViewModel>()
    private val safeArgs: SpeakerFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = layoutInflater.inflate(R.layout.fragment_speaker, container, false)

        setToolbar(activity)
        setHasOptionsMenu(true)

        speakerViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(it)
            })

        speakerViewModel.speaker
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                initializeViews(it)
            })

        speakerViewModel.loadSpeaker(safeArgs.speakerId)

        return rootView
    }

    private fun initializeViews(speaker: Speaker) {
        when (speaker.name.isNullOrBlank()) {
            true -> rootView.name.isVisible = false
            false -> {
                rootView.name.text = speaker.name
                setToolbar(activity, speaker.name)
            }
        }

        when (speaker.position.isNullOrBlank()) {
            true -> rootView.position.isVisible = false
            false -> rootView.position.text = speaker.position
        }

        when (speaker.organisation.isNullOrBlank()) {
            true -> rootView.organization.isVisible = false
            false -> rootView.organization.text = speaker.organisation
        }

        when {
            speaker.longBiography.isNullOrBlank() && speaker.shortBiography.isNullOrBlank() ->
                rootView.aboutSpeakerContainer.isVisible = false
            speaker.longBiography.isNullOrBlank() -> rootView.bio.text = speaker.shortBiography.stripHtml()
            else -> rootView.bio.text = speaker.longBiography.stripHtml()
        }

        if (speaker.email.isNullOrBlank() && speaker.mobile.isNullOrBlank() &&
            speaker.city.isNullOrBlank() && speaker.country.isNullOrBlank() &&
            speaker.website.isNullOrBlank() && speaker.twitter.isNullOrBlank() &&
            speaker.facebook.isNullOrBlank() && speaker.linkedin.isNullOrBlank() &&
            speaker.github.isNullOrBlank()) rootView.speakerContactsContainer.isVisible = false
        else {
            when (speaker.email.isNullOrBlank()) {
                true -> rootView.email.isVisible = false
                false -> rootView.email.text = getString(R.string.email_name, speaker.email)
            }

            when (speaker.mobile.isNullOrBlank()) {
                true -> rootView.mobile.isVisible = false
                false -> rootView.mobile.text = getString(R.string.phone_number_name, speaker.mobile)
            }

            when {
                speaker.country.isNullOrBlank() && speaker.country.isNullOrBlank() -> rootView.from.isVisible = false
                speaker.country.isNullOrBlank() -> rootView.from.text = getString(R.string.from_place, speaker.city)
                speaker.city.isNullOrBlank() -> rootView.from.text = getString(R.string.from_place, speaker.country)
                else -> rootView.from.text = getString(R.string.from_places, speaker.city, speaker.country)
            }

            when (speaker.website.isNullOrBlank()) {
                true -> rootView.website.isVisible = false
                false -> rootView.website.setOnClickListener {
                    context?.let { openUrl(it, speaker.website) }
                }
            }

            when (speaker.twitter.isNullOrBlank()) {
                true -> rootView.twitter.isVisible = false
                false -> rootView.twitter.setOnClickListener {
                    context?.let { openUrl(it, speaker.twitter) }
                }
            }

            when (speaker.facebook.isNullOrBlank()) {
                true -> rootView.facebook.isVisible = false
                false -> rootView.facebook.setOnClickListener {
                    context?.let { openUrl(it, speaker.facebook) }
                }
            }

            when (speaker.linkedin.isNullOrBlank()) {
                true -> rootView.linkedin.isVisible = false
                false -> rootView.linkedin.setOnClickListener {
                    context?.let { openUrl(it, speaker.linkedin) }
                }
            }

            when (speaker.github.isNullOrBlank()) {
                true -> rootView.github.isVisible = false
                false -> rootView.github.setOnClickListener {
                    context?.let { openUrl(it, speaker.github) }
                }
            }
        }

        Picasso.get()
            .load(speaker.photoUrl)
            .placeholder(requireDrawable(requireContext(), R.drawable.ic_account_circle_grey))
            .transform(CircleTransform())
            .into(rootView.photo)
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
}
