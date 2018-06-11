package org.fossasia.openevent.general

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel

class MainActivityViewModel : ViewModel() {
    val loadEventsAgain = MutableLiveData<Boolean>()

}

