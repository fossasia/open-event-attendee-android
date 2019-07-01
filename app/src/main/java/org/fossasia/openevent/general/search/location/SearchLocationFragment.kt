package org.fossasia.openevent.general.search.location

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_search_location.view.scrollView
import kotlinx.android.synthetic.main.fragment_search_location.view.toolbarTitle
import kotlinx.android.synthetic.main.fragment_search_location.view.toolbarLayout
import kotlinx.android.synthetic.main.fragment_search_location.view.placeSuggestionsCard
import kotlinx.android.synthetic.main.fragment_search_location.view.recentSearchLayout
import kotlinx.android.synthetic.main.fragment_search_location.view.recentSearchRv
import kotlinx.android.synthetic.main.fragment_search_location.view.popularLocationsLayout
import kotlinx.android.synthetic.main.fragment_search_location.view.currentLocation
import kotlinx.android.synthetic.main.fragment_search_location.view.popularLocationsRv
import kotlinx.android.synthetic.main.fragment_search_location.view.locationProgressBar
import kotlinx.android.synthetic.main.fragment_search_location.view.locationSearchView
import kotlinx.android.synthetic.main.fragment_search_location.view.rvAutoPlaces
import kotlinx.android.synthetic.main.fragment_search_location.view.toolbar
import kotlinx.android.synthetic.main.fragment_search_location.view.shimmerSearchEventTypes
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.search.SEARCH_FILTER_FRAGMENT
import org.fossasia.openevent.general.search.SEARCH_FRAGMENT
import org.fossasia.openevent.general.utils.Utils
import org.fossasia.openevent.general.utils.Utils.isLocationEnabled
import org.fossasia.openevent.general.utils.Utils.setToolbar
import org.fossasia.openevent.general.utils.Utils.showSoftKeyboard
import org.fossasia.openevent.general.utils.extensions.nonNull
import org.jetbrains.anko.design.snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

const val LOCATION_PERMISSION_REQUEST = 1000

class SearchLocationFragment : Fragment() {
    private lateinit var rootView: View
    private val searchLocationViewModel by viewModel<SearchLocationViewModel>()
    private val geoLocationViewModel by viewModel<GeoLocationViewModel>()
    private val safeArgs: SearchLocationFragmentArgs by navArgs()
    private val popularLocationAdapter = LocationsAdapter()
    private val recentLocationAdapter = LocationsAdapter()

    private val placeSuggestionsAdapter = PlaceSuggestionsAdapter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search_location, container, false)
        setToolbar(activity, show = false)

        setupPopularLocations()

        setUpLocationSearchView()

        setupRecyclerPlaceSuggestions()

        setupRecentSearchLocations()

        rootView.currentLocation.setOnClickListener {
            checkLocationPermission()
            if (isLocationEnabled(requireContext())) {
                geoLocationViewModel.configure()
                rootView.locationProgressBar.visibility = View.VISIBLE
            }
        }

        geoLocationViewModel.location.observe(viewLifecycleOwner, Observer { location ->
            savePlaceAndRedirectToMain(location)
        })

        geoLocationViewModel.errorMessage
            .nonNull()
            .observe(viewLifecycleOwner, Observer {
                rootView.snackbar(it)
                rootView.locationProgressBar.visibility = View.GONE
            })

        searchLocationViewModel.placeSuggestions.observe(viewLifecycleOwner, Observer {
            placeSuggestionsAdapter.submitList(it)
            // To handle the case : search result comes after query is empty
            toggleSuggestionVisibility(it.isNotEmpty() && rootView.locationSearchView.query.isNotEmpty())
        })

        rootView.toolbar.setNavigationOnClickListener {
            Utils.hideSoftKeyboard(context, rootView)
            activity?.onBackPressed()
        }

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listener = object : TextClickListener {
            override fun onTextClick(location: String) {
                savePlaceAndRedirectToMain(location)
            }
        }
        popularLocationAdapter.setListener(listener)
        recentLocationAdapter.setListener(listener)

        rootView.toolbarTitle.setOnClickListener {
            rootView.scrollView.scrollTo(0, 0)
            rootView.locationSearchView.isFocusable = true
            showSoftKeyboard(context, rootView)
        }

        rootView.scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, scrollY: Int, _: Int, _: Int ->
            if (scrollY > rootView.locationSearchView.y) {
                rootView.toolbarLayout.elevation = resources.getDimension(R.dimen.custom_toolbar_elevation)
                rootView.toolbarTitle.text = getString(R.string.location_hint)
            } else {
                rootView.toolbarLayout.elevation = 0F
                rootView.toolbarTitle.text = ""
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showSoftKeyboard(context, rootView.locationSearchView)
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
        rootView.placeSuggestionsCard.isVisible = state

        rootView.popularLocationsLayout.isVisible = !state && popularLocationAdapter.itemCount != 0
        rootView.recentSearchLayout.isVisible = !state && recentLocationAdapter.itemCount != 0
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
        rootView.locationSearchView.locationSearchView.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK)
                rootView.locationSearchView.clearFocus()
            true
        }
        searchLocationViewModel.handlePlaceSuggestions(subject)
    }

    private fun handleDisplayPlaceSuggestions(query: String, subject: PublishSubject<String>) {
        if (query.isNotEmpty()) {
            subject.onNext(query)
        } else {
            toggleSuggestionVisibility(false)
        }
    }

    private fun setupRecentSearchLocations() {
        val recentSearch = searchLocationViewModel.getRecentLocationList()
        if (recentSearch.isEmpty()) {
            rootView.recentSearchLayout.isVisible = false
        } else {
            rootView.recentSearchLayout.isVisible = true
            recentLocationAdapter.addAll(recentSearch)
            rootView.recentSearchRv.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            rootView.recentSearchRv.adapter = recentLocationAdapter
        }
    }

    private fun setupPopularLocations() {
        rootView.popularLocationsRv.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rootView.popularLocationsRv.adapter = popularLocationAdapter

        searchLocationViewModel.showShimmer
            .nonNull()
            .observe(viewLifecycleOwner, Observer { shouldShowShimmer ->
                if (shouldShowShimmer) {
                    rootView.shimmerSearchEventTypes.startShimmer()
                } else {
                    rootView.shimmerSearchEventTypes.stopShimmer()
                }
                rootView.shimmerSearchEventTypes.isVisible = shouldShowShimmer
            })

        searchLocationViewModel.eventLocations
            .nonNull()
            .observe(this, Observer { list ->
                popularLocationAdapter.addAll(list.map { it.name })
                rootView.popularLocationsLayout.isVisible = list.isNotEmpty()
            })

        searchLocationViewModel.loadEventsLocation()
    }
}
