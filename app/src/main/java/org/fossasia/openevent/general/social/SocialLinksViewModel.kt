package org.fossasia.openevent.general.social

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class SocialLinksViewModel(private val socialLinksService: SocialLinksService) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val progress = MutableLiveData<Boolean>()
    val socialLinks = MutableLiveData<List<SocialLink>>()
    val error = MutableLiveData<String>()

    fun loadSocialLinks(id: Long) {
        compositeDisposable.add(socialLinksService.getSocialLinks(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe({
                    progress.value = true
                }).doFinally({
                    progress.value = false
                }).subscribe({
                    socialLinks.value = it
                }, {
                    Timber.e(it, "Error fetching Social Links")
                    error.value = "Error Social Links"
                }))
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

}