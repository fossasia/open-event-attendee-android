package org.fossasia.openevent.general.search

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.mapbox.api.geocoding.v5.models.CarmenFeature
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceAutocompleteFragment
import com.mapbox.mapboxsdk.plugins.places.autocomplete.ui.PlaceSelectionListener
import kotlinx.android.synthetic.main.fragment_search_location.view.currentLocationLinearLayout
import kotlinx.android.synthetic.main.fragment_search_location.view.locationProgressBar
import org.fossasia.openevent.general.BuildConfig
import org.fossasia.openevent.general.R
import org.koin.androidx.viewmodel.ext.android.viewModel

const val LOCATION_PERMISSION_REQUEST = 1000
const val AUTOCOMPLETE_FRAG_TAG = "AutoComplete_Frag"

class SearchLocationFragment : Fragment() {
    private lateinit var rootView: View
    private val searchLocationViewModel by viewModel<SearchLocationViewModel>()
    private val geoLocationViewModel by viewModel<GeoLocationViewModel>()
    private val safeArgs: SearchLocationFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_search_location, container, false)

        val thisActivity = activity
        if (thisActivity is AppCompatActivity) {
            thisActivity.supportActionBar?.hide()
        }
        setHasOptionsMenu(true)

        geoLocationViewModel.currentLocationVisibility.observe(viewLifecycleOwner, Observer {
            rootView.currentLocationLinearLayout.visibility = View.GONE
        })

        rootView.currentLocationLinearLayout.setOnClickListener {
            checkLocationPermission()
            geoLocationViewModel.configure()
            rootView.locationProgressBar.visibility = View.VISIBLE
        }

        geoLocationViewModel.location.observe(viewLifecycleOwner, Observer { location ->
            searchLocationViewModel.saveSearch(location)
            redirectToMain()
        })

        setupPlaceAutoCompleteFrag(savedInstanceState)

        return rootView
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
        val fragmentId = if (safeArgs.fromSearchFragment) R.id.searchFragment else R.id.eventsFragment
        Navigation.findNavController(rootView).popBackStack(fragmentId, false)
    }

    private fun setupPlaceAutoCompleteFrag(savedInstanceState: Bundle?) {

        val autocompleteFragment: PlaceAutocompleteFragment?
        if (savedInstanceState == null) {
            val placeOptions = PlaceOptions.builder().build(PlaceOptions.MODE_CARDS)
            autocompleteFragment = PlaceAutocompleteFragment.newInstance(
                BuildConfig.MAPBOX_KEY, placeOptions)
            val transaction = fragmentManager?.beginTransaction()
            transaction?.add(R.id.autocomplete_frag_container, autocompleteFragment,
                AUTOCOMPLETE_FRAG_TAG)
            transaction?.commit()
        } else {
            autocompleteFragment = fragmentManager?.findFragmentByTag(
                AUTOCOMPLETE_FRAG_TAG) as? PlaceAutocompleteFragment
        }

        autocompleteFragment?.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(carmenFeature: CarmenFeature) {
                val location = carmenFeature.placeName()?.split(",")?.first()
                location?.let {
                    searchLocationViewModel.saveSearch(it)
                    redirectToMain()
                }
            }

            override fun onCancel() {
                activity?.onBackPressed()
            }
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    geoLocationViewModel.configure()
                } else {
                    Snackbar.make(rootView, R.string.cannot_fetch_location, Snackbar.LENGTH_SHORT).show()
                    rootView.locationProgressBar.visibility = View.GONE
                }
            }
        }
    }
}
