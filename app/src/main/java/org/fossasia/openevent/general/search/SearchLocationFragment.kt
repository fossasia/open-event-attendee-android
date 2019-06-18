package org.fossasia.openevent.general.search

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_search_location.eventLocationLv
import kotlinx.android.synthetic.main.fragment_search_location.locationSearchView
import kotlinx.android.synthetic.main.fragment_search_location.placeSuggestionsCard
import kotlinx.android.synthetic.main.fragment_search_location.titleTv
import kotlinx.android.synthetic.main.fragment_search_location.view.currentLocation
import kotlinx.android.synthetic.main.fragment_search_location.view.eventLocationLv
import kotlinx.android.synthetic.main.fragment_search_location.view.locationProgressBar
import kotlinx.android.synthetic.main.fragment_search_location.view.locationSearchView
import kotlinx.android.synthetic.main.fragment_search_location.view.rvAutoPlaces
import kotlinx.android.synthetic.main.fragment_search_location.view.toolbar
import kotlinx.android.synthetic.main.fragment_search_location.view.shimmerSearchEventTypes
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

const val LOCATION_PERMISSION_REQUEST = 1000

class SearchLocationFragment : Fragment() {
    private lateinit var rootView: View
    private val searchLocationViewModel by viewModel<SearchLocationViewModel>()
    private val geoLocationViewModel by viewModel<GeoLocationViewModel>()
    private val safeArgs: SearchLocationFragmentArgs by navArgs()
    private val eventLocationList: MutableList<String> = ArrayList()

    private val placeSuggestionsAdapter = PlaceSuggestionsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search_location, container, false)
        setToolbar(activity, show = false)
        searchLocationViewModel.loadEventsLocation()

        setupPopularLocations()

        setUpLocationSearchView()

        setupRecyclerPlaceSuggestions()

        geoLocationViewModel.currentLocationVisibility.observe(viewLifecycleOwner, Observer {
            rootView.currentLocation.isVisible = false
        })

        rootView.currentLocation.setOnClickListener {
            checkLocationPermission()
            geoLocationViewModel.configure()
            rootView.locationProgressBar.visibility = View.VISIBLE
        }

        geoLocationViewModel.location.observe(viewLifecycleOwner, Observer { location ->
            savePlaceAndRedirectToMain(location)
        })

        searchLocationViewModel.placeSuggestions.observe(viewLifecycleOwner, Observer {
            placeSuggestionsAdapter.submitList(it)
            // To handle the case : search result comes after query is empty
            toggleSuggestionVisibility(it.isNotEmpty() && locationSearchView.query.isNotEmpty())
        })

        rootView.toolbar.setNavigationOnClickListener {
            Utils.hideSoftKeyboard(context, rootView)
            activity?.onBackPressed()
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()
        Utils.showSoftKeyboard(context, rootView.locationSearchView)
    }

    private fun checkLocationPermission() {
        val permission =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST)
        }
    }

    private fun redirectToMain() {
        val fragmentId = when (safeArgs.fromFragmentName) {
            SEARCH_FRAGMENT -> SearchLocationFragmentDirections.actionSearchLocationToSearch()
            SEARCH_FILTER_FRAGMENT -> SearchLocationFragmentDirections.actionSearchLocationToSearchFilter()
            else -> SearchLocationFragmentDirections.actionSearchLocationToEvents()
        }
        Navigation.findNavController(rootView).navigate(fragmentId)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    geoLocationViewModel.configure()
                } else {
                    rootView.snackbar(R.string.cannot_fetch_location)
                    rootView.locationProgressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun setupRecyclerPlaceSuggestions() {
        rootView.rvAutoPlaces.layoutManager = LinearLayoutManager(context)
        rootView.rvAutoPlaces.adapter = placeSuggestionsAdapter

        placeSuggestionsAdapter.onSuggestionClick = {
            savePlaceAndRedirectToMain(it)
        }
    }
    private fun toggleSuggestionVisibility(state: Boolean) {
        placeSuggestionsCard.isVisible = state

        titleTv.isVisible = !state
        eventLocationLv.isVisible = !state
    }

    private fun savePlaceAndRedirectToMain(place: String) {
        searchLocationViewModel.saveSearch(place)
        redirectToMain()
    }

    private fun setUpLocationSearchView() {
        val subject = PublishSubject.create<String>()
        rootView.locationSearchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                savePlaceAndRedirectToMain(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                handleDisplayPlaceSuggestions(newText, subject)
                return false
            }
        })
        searchLocationViewModel.handlePlaceSuggestions(subject)
    }

    private fun handleDisplayPlaceSuggestions(query: String, subject: PublishSubject<String>) {
        if (query.isNotEmpty()) {
            subject.onNext(query)
        } else {
            toggleSuggestionVisibility(false)
        }
    }

    private fun setupPopularLocations() {

        val adapter = ArrayAdapter(context, android.R.layout.simple_list_item_1, eventLocationList)
        rootView.eventLocationLv.adapter = adapter

        searchLocationViewModel.showShimmer
            .nonNull()
            .observe(viewLifecycleOwner, Observer { shouldShowShimmer ->
                if (shouldShowShimmer) {
                    rootView.shimmerSearchEventTypes.startShimmer()
                    adapter.clear()
                } else {
                    rootView.shimmerSearchEventTypes.stopShimmer()
                }
                rootView.shimmerSearchEventTypes.isVisible = shouldShowShimmer
            })

        searchLocationViewModel.eventLocations
            .nonNull()
            .observe(this, Observer { list ->
                list.forEach {
                    eventLocationList.add(it.name)
                }
                adapter.notifyDataSetChanged()
            })

        rootView.eventLocationLv.setOnItemClickListener { parent, view, position, id ->
            savePlaceAndRedirectToMain(eventLocationList[position])
        }
    }
}
