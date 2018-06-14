package org.fossasia.openevent.general.event


import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PagedList
import android.util.Log


class BoundaryCallback(
        private val eventDao: EventDao,
        private val service: EventApi
) : PagedList.BoundaryCallback<Event>() {

    companion object {
        private const val NETWORK_PAGE_SIZE = 20
    }

    // keep the last requested page. When the request is successful, increment the page number.
    private var lastRequestedPage = 1

    private val viewNetworkErrors = MutableLiveData<String>()
    // LiveData of network errors.
    val networkErrors: LiveData<String>
        get() = viewNetworkErrors

    // avoid triggering multiple requests in the same time
    private var isRequestInProgress = false

    /**
     * Database returned 0 items. We should query the backend for more items.
     */
    override fun onZeroItemsLoaded() {
        Log.d("RepoBoundaryCallback", "onZeroItemsLoaded")
        requestAndSaveData()
    }

    /**
     * When all items in the database were loaded, we need to query the backend for more items.
     */
    override fun onItemAtEndLoaded(itemAtEnd: Event) {
        Log.d("RepoBoundaryCallback", "onItemAtEndLoaded")
        requestAndSaveData()
    }

    private fun requestAndSaveData() {
        if (isRequestInProgress) return

        isRequestInProgress = true
        service.getEvents(lastRequestedPage++).map {
            eventDao.insertEvents(it)
            it
        }.doOnSuccess {
            isRequestInProgress = false
        }

    }
}