package org.fossasia.openevent.general.paypal

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import org.fossasia.openevent.general.auth.AuthHolder
import org.fossasia.openevent.general.connectivity.MutableConnectionLiveData

class PaypalWebViewModel(
    private val authHolder: AuthHolder,
    private val mutableConnectionLiveData: MutableConnectionLiveData
) : ViewModel() {

    val connection: LiveData<Boolean> = mutableConnectionLiveData

    fun getToken() = authHolder.token
}
