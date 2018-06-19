package org.fossasia.openevent.general.event

import android.arch.lifecycle.Observer
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import kotlinx.android.synthetic.main.fragment_events.view.*
import org.fossasia.openevent.general.R
import org.koin.android.architecture.ext.viewModel
import timber.log.Timber
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.view.inputmethod.InputMethodManager
import android.net.ConnectivityManager




class EventsFragment : Fragment() {
    private val eventsRecyclerAdapter: EventsRecyclerAdapter = EventsRecyclerAdapter()
    private val eventsViewModel by viewModel<EventsViewModel>()
    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_events, container, false)

        rootView.progressBar.isIndeterminate = true

        rootView.eventsRecycler.layoutManager = LinearLayoutManager(activity)

        rootView.eventsRecycler.adapter = eventsRecyclerAdapter
        rootView.eventsRecycler.isNestedScrollingEnabled = false

        val recyclerViewClickListener = object : RecyclerViewClickListener {
            override fun onClick(eventID: Long) {
                val fragment = EventDetailsFragment()
                val bundle = Bundle()
                bundle.putLong(fragment.EVENT_ID, eventID)
                fragment.arguments = bundle
                activity?.supportFragmentManager?.beginTransaction()?.add(R.id.frameContainer, fragment)?.addToBackStack(null)?.commit()
            }
        }
        eventsRecyclerAdapter.setListener(recyclerViewClickListener)
        eventsViewModel.events.observe(this, Observer {
            it?.let {
                eventsRecyclerAdapter.addAll(it)
                eventsRecyclerAdapter.notifyDataSetChanged()
            }
            Timber.d("Fetched events of size %s", eventsRecyclerAdapter.itemCount)
        })

        eventsViewModel.error.observe(this, Observer {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        })

        eventsViewModel.progress.observe(this, Observer {
            it?.let { showProgressBar(it) }
        })

        rootView.locationEdittext.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH && !TextUtils.isEmpty(rootView.locationEdittext.text)) {
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(rootView.locationEdittext.windowToken, 0)

                eventsViewModel.locationName = rootView.locationEdittext.text.toString()
                eventsViewModel.loadLocationEvents()
                return@OnEditorActionListener true
            }
            false
        })

        showNoInternetScreen(isNetworkConnected())

        rootView.retry.setOnClickListener {
            showNoInternetScreen(isNetworkConnected())
        }

        return rootView
    }

    private fun showNoInternetScreen(show: Boolean) {
        rootView.homeScreenLL.visibility = if (show) View.VISIBLE else View.GONE
        rootView.noInternetCard.visibility = if (!show) View.VISIBLE else View.GONE
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        return connectivityManager.activeNetworkInfo != null
    }

    private fun showProgressBar(show: Boolean) {
        rootView.progressBar.isIndeterminate = show
        rootView.progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }

}