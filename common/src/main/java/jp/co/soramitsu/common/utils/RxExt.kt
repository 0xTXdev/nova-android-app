package jp.co.soramitsu.common.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

typealias ErrorHandler = (Throwable) -> Unit

val DEFAULT_ERROR_HANDLER: ErrorHandler = Throwable::printStackTrace

fun <T> Single<T>.asLiveData(
    disposable: CompositeDisposable,
    errorHandler: ErrorHandler = DEFAULT_ERROR_HANDLER
): LiveData<T> {
    val liveData = MutableLiveData<T>()

    disposable.add(subscribe({
        liveData.value = it
    }, errorHandler))

    return liveData
}

fun <T> Single<T>.asMutableLiveData(
    disposable: CompositeDisposable,
    errorHandler: ErrorHandler = DEFAULT_ERROR_HANDLER
): MutableLiveData<T> {
    val liveData = MutableLiveData<T>()

    disposable.add(subscribe({
        liveData.value = it
    }, errorHandler))

    return liveData
}

fun <T> Observable<T>.asLiveData(
    disposable: CompositeDisposable,
    errorHandler: ErrorHandler = DEFAULT_ERROR_HANDLER
): LiveData<T> {
    val liveData = MutableLiveData<T>()

    disposable.add(subscribe({
        liveData.value = it
    }, errorHandler))

    return liveData
}

fun <T> Observable<T>.asMutableLiveData(
    disposable: CompositeDisposable,
    errorHandler: ErrorHandler = DEFAULT_ERROR_HANDLER
): MutableLiveData<T> {
    val liveData = MutableLiveData<T>()

    disposable.add(subscribe({
        liveData.value = it
    }, errorHandler))

    return liveData
}

operator fun CompositeDisposable.plusAssign(child: Disposable) {
    add(child)
}

fun Completable.subscribeToError(onError: (Throwable) -> Unit) = subscribe({ }, onError)

fun <T, R> Observable<List<T>>.mapList(mapper: (T) -> R): Observable<List<R>> {
    return map { list -> list.map(mapper) }
}

fun <T, R> Single<List<T>>.mapList(mapper: (T) -> R): Single<List<R>> {
    return map { list -> list.map(mapper) }
}

@Suppress("UNCHECKED_CAST") fun <R> List<Single<out R>>.zipSimilar() : Single<List<R>> = Single.zip(this) { values ->
    val casted = values as Array<out R>

    casted.toList()
}