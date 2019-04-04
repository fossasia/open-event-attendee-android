package org.fossasia.openevent.general.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.data.Preference

const val SAVED_LOCATION = "LOCATION"

class SearchLocationViewModel(private val preference: Preference,
                              private val autoPlaceApi: AutoPlaceApi) : ViewModel() {

    val autoPlaceSuggestion = MutableLiveData<List<AutoCompletePlaceInfo>>()
    private val compositeDisposable = CompositeDisposable()

    fun saveSearch(query: String) {
        preference.putString(SAVED_LOCATION, query)
    }

    fun loadSuggestions(query: String){
        compositeDisposable.add(autoPlaceApi.getAutoCompletePlaceSuggestions(query)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    autoPlaceSuggestion.value = it.suggestions
                },
                {
                    autoPlaceSuggestion.value = emptyList()
                }
            )
        )
    }


}
