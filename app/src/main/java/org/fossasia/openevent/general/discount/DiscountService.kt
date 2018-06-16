package org.fossasia.openevent.general.discount

import android.arch.persistence.room.EmptyResultSetException
import io.reactivex.Single
import org.fossasia.openevent.general.auth.DiscountCode
import timber.log.Timber

class DiscountService(private val discountApi: DiscountApi, private val discountDao: DiscountDao) {

    fun getDiscountCode(id: Long): Single<DiscountCode> {
        return discountDao.getDiscountCode(id)
                .onErrorResumeNext {
                    if (it is EmptyResultSetException) {
                        Timber.d(it, "DiscountCodes not found in Database for %d", id)
                        discountApi.getDiscountCodes(id)
                                .map {
                                    discountDao.insertDiscountCode(it)
                                    it
                                }
                    } else {
                        Single.error(it)
                    }
                }
    }
}