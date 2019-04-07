package org.fossasia.openevent.general.social

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.fossasia.openevent.general.R
import org.fossasia.openevent.general.common.SingleLiveEvent
import org.fossasia.openevent.general.data.Resource
import timber.log.Timber

class SocialLinksViewModel(
    private val socialLinksService: SocialLinksService,
    private val resource: Resource
) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val mutableProgress = MutableLiveData<Boolean>()
    val progress: LiveData<Boolean> = mutableProgress
    private val mutableSocialLinks = MutableLiveData<List<SocialLink>>()
    val socialLinks: LiveData<List<SocialLink>> = mutableSocialLinks
    private val mutableError = SingleLiveEvent<String>()
    val error: LiveData<String> = mutableError

    fun loadSocialLinks(id: Long) {
        compositeDisposable.add(socialLinksService.getSocialLinks(id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                mutableProgress.value = true
            }.subscribe({
                mutableSocialLinks.value = it
                mutableProgress.value = false
            }, {
                Timber.e(it, "Error fetching Social Links")
                mutableError.value = resource.getString(R.string.error_fetching_social_links_message)
                mutableProgress.value = false
            })
        )
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
