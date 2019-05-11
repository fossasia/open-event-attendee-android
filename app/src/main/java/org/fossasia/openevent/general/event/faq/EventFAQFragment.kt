package org.fossasia.openevent.general.event.faq

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_event_faq.view.faqEmptyView
import kotlinx.android.synthetic.main.fragment_event_faq.view.faqRv
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.about.AboutEventFragmentArgs
import org.fossasia.openevent.general.utils.Utils
import org.koin.androidx.viewmodel.ext.android.viewModel

class EventFAQFragment : Fragment() {
    private lateinit var rootView: View
    private val eventFAQViewModel by viewModel<EventFAQViewModel>()
    private val faqAdapter = FAQRecyclerAdapter()
    private val safeArgs: AboutEventFragmentArgs by navArgs()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = layoutInflater.inflate(R.layout.fragment_event_faq, container, false)

        rootView.faqRv.layoutManager = LinearLayoutManager(context)
        rootView.faqRv.adapter = faqAdapter

        Utils.setToolbar(activity, getString(R.string.frequently_asked_questions))
        setHasOptionsMenu(true)

        eventFAQViewModel.eventFAQ.observe(viewLifecycleOwner, Observer {
            faqAdapter.addAll(it)
            rootView.faqEmptyView.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
        })

        eventFAQViewModel.loadEventFaq(safeArgs.eventId)

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
