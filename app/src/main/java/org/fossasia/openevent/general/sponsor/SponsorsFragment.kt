package org.fossasia.openevent.general.sponsor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_sponsors.view.sponsorsDetailRecyclerView
import kotlinx.android.synthetic.main.fragment_sponsors.view.sponsorDetailProgressBar
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class SponsorsFragment : Fragment() {
    private lateinit var rootView: View
    private val sponsorsDetailViewModel by viewModel<SponsorsViewModel>()
    private val sponsorsAdapter = SponsorsDetailAdapter()
    private val safeArgs: SponsorsFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_sponsors, container, false)

        Utils.setToolbar(activity, getString(R.string.sponsors))
        setHasOptionsMenu(true)

        val sponsorURLClickListener = object : SponsorURLClickListener {
            override fun onClick(sponsorURL: String?) {
                if (sponsorURL.isNullOrBlank()) return
                context?.let { Utils.openUrl(it, sponsorURL) }
            }
        }
        sponsorsAdapter.apply {
            onURLClickListener = sponsorURLClickListener
        }

        val layoutManager = LinearLayoutManager(context)
        layoutManager.orientation = RecyclerView.VERTICAL
        rootView.sponsorsDetailRecyclerView.layoutManager = layoutManager
        rootView.sponsorsDetailRecyclerView.adapter = sponsorsAdapter

        sponsorsDetailViewModel.error
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(it)
            })

        sponsorsDetailViewModel.sponsors
            .nonNull()
            .observe(viewLifecycleOwner, Observer { sponsors ->
                sponsorsAdapter.addAll(sponsors)
            })

        sponsorsDetailViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer { isLoading ->
                rootView.sponsorDetailProgressBar.isVisible = isLoading
            })

        sponsorsDetailViewModel.loadSponsors(safeArgs.eventId)

        return rootView
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
