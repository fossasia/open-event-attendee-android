package org.fossasia.openevent.general.social

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.provider.CalendarContract.Instances.EVENT_ID
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.fragment_social_links.view.*
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.event.EventDetailsFragment
import org.fossasia.openevent.general.event.EventsRecyclerAdapter
import org.fossasia.openevent.general.event.EventsViewModel
import org.fossasia.openevent.general.event.RecyclerViewClickListener
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber

class SocialLinksFragment : Fragment() {
    private val socialLinksRecyclerAdapter: SocialLinksRecyclerAdapter = SocialLinksRecyclerAdapter()
    private val socialLinksViewModel by viewModel<SocialLinksViewModel>()
    private lateinit var rootView: View
    private var id: Long = -1
    private val EVENT_ID: String = "EVENT_ID"
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = this.arguments
        if (bundle != null) {
            id = bundle.getLong(EVENT_ID, -1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_social_links, container, false)

        rootView.progressBarSocial.isIndeterminate = true

        linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL
        rootView.socialLinksRecycler.layoutManager = linearLayoutManager

        rootView.socialLinksRecycler.adapter = socialLinksRecyclerAdapter
        rootView.socialLinksRecycler.isNestedScrollingEnabled = false

        linearLayoutManager = LinearLayoutManager(context)
        rootView.socialLinksRecycler.layoutManager = linearLayoutManager

        socialLinksViewModel.socialLinks.observe(this, Observer {
            it?.let {
                socialLinksRecyclerAdapter.addAll(it)
            }
            socialLinksRecyclerAdapter.notifyDataSetChanged()
            Timber.d("Fetched social-links of size %s", socialLinksRecyclerAdapter.itemCount)
        })

        socialLinksViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        socialLinksViewModel.progress.observe(this, Observer {
            it?.let { showProgressBar(it) }
        })

        socialLinksViewModel.loadSocialLinks(id)

        return rootView
    }

    private fun showProgressBar(show: Boolean) {
        rootView.progressBarSocial.isIndeterminate = show
        rootView.progressBarSocial.visibility = if (show) View.VISIBLE else View.GONE
    }
}