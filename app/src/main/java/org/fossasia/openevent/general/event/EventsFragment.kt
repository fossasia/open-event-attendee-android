package org.fossasia.openevent.general.event

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.fragment_events.view.*
import org.fossasia.openevent.general.R
import org.koin.android.ext.android.inject
import timber.log.Timber

class EventsFragment : Fragment() {
    private val eventsRecyclerAdapter: EventsRecyclerAdapter = EventsRecyclerAdapter()
    private val linearLayoutManager: LinearLayoutManager by lazy {
        LinearLayoutManager(activity)
    }
    private val compositeDisposable = CompositeDisposable()
    private val eventApi: EventApi by inject()

    private lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_events, container, false)

        rootView.progressBar.isIndeterminate = true

        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rootView.eventsRecycler.layoutManager = linearLayoutManager

        rootView.eventsRecycler.adapter = eventsRecyclerAdapter
        rootView.eventsRecycler.isNestedScrollingEnabled = false

        compositeDisposable.add(eventApi.getEvents()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ eventList ->
                    Timber.d("Response Success")
                    eventsRecyclerAdapter.addAll(eventList)

                    progressBarHandle()
                    addAnim()
                    notifyItems()

                    Timber.d("Fetched events of size %s", eventsRecyclerAdapter.itemCount)
                }) { throwable ->
                    run {
                        progressBarHandle()
                        Timber.e(throwable, "Fetching Failed")
                    }
                })

        return rootView
    }

    private fun notifyItems() {
        val firstVisible = linearLayoutManager.findFirstVisibleItemPosition()
        val lastVisible = linearLayoutManager.findLastVisibleItemPosition()

        val itemsChanged = lastVisible - firstVisible + 1 // + 1 because we start count items from 0
        val start = if (firstVisible - itemsChanged > 0) firstVisible - itemsChanged else 0

        eventsRecyclerAdapter.notifyItemRangeChanged(start, itemsChanged + itemsChanged)
    }

    private fun addAnim() {
        //item animator
        val slideup = SlideInUpAnimator()
        slideup.addDuration = 500
        rootView.eventsRecycler.itemAnimator = slideup
    }

    private fun progressBarHandle() {
        rootView.progressBar.isIndeterminate = false
        rootView.progressBar.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}