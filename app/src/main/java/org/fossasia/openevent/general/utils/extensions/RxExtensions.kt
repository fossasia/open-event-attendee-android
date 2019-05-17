package org.fossasia.openevent.general.utils.extensions

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

fun <T> Single<T>.withDefaultSchedulers():
    Single<T> = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun <T> Flowable<T>.withDefaultSchedulers():
    Flowable<T> = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

fun Completable.withDefaultSchedulers():
    Completable = subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
