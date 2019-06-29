package org.fossasia.openevent.general.feedback

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
import kotlinx.android.synthetic.main.fragment_feedback.view.progressBar
import kotlinx.android.synthetic.main.fragment_feedback.view.feedbackEmptyView
import kotlinx.android.synthetic.main.fragment_feedback.view.feedbackRecyclerView
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class FeedbackFragment : Fragment() {
    private lateinit var rootView: View
    private val feedbackViewModel by viewModel<FeedbackViewModel>()
    private val safeArgs: FeedbackFragmentArgs by navArgs()
    private val feedbackAdapter = FeedbackRecyclerAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_feedback, container, false)

        setToolbar(activity, getString(R.string.feedback))
        setHasOptionsMenu(true)

        feedbackViewModel.progress
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.progressBar.isVisible = it
            })

        feedbackViewModel.message
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(it)
            })

        feedbackViewModel.feedback
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                feedbackAdapter.addAll(it)
                rootView.feedbackEmptyView.isVisible = it.isEmpty()
                rootView.feedbackRecyclerView.isVisible = it.isNotEmpty()
            })

        rootView.feedbackRecyclerView.layoutManager = LinearLayoutManager(activity)
        rootView.feedbackRecyclerView.adapter = feedbackAdapter

        feedbackViewModel.feedback.value?.let {
            feedbackAdapter.addAll(it)
            rootView.feedbackEmptyView.isVisible = it.isEmpty()
            rootView.feedbackRecyclerView.isVisible = it.isNotEmpty()
        } ?: feedbackViewModel.getAllFeedback(safeArgs.eventId)

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
