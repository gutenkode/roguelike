package de.gutenko.roguelike.habittracker

import android.util.Log
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

fun <T> Observable<T>.androidLog(tag: String): Observable<T> {
    return doOnNext {
        Log.d(tag, "RxLog: (${Thread.currentThread().name}) onNext: $it")
    }.doOnDispose {
        Log.d(tag, "RxLog: (${Thread.currentThread().name}) onDispose")
    }.doOnError {
        Log.d(tag, "RxLog: (${Thread.currentThread().name}) onError: $it")
    }.doOnComplete {
        Log.d(tag, "RxLog: (${Thread.currentThread().name}) onComplete")
    }.doOnSubscribe {
        Log.d(tag, "RxLog: (${Thread.currentThread().name}) onSubscribe: $it")
    }
}

data class Changed<T>(val from: T, val to: T)
private data class Intermediate<T>(val mostRecent: T?, val leastRecent: T?)

fun <T> Observable<T>.changes(): Observable<Changed<T>> {
    return scan<Intermediate<T>>(
        Intermediate(mostRecent = null, leastRecent = null),
        BiFunction<Intermediate<T>, T, Intermediate<T>> { intermediate, t ->
            Intermediate(mostRecent = t, leastRecent = intermediate.mostRecent)
        }).filter { it.leastRecent != null && it.mostRecent != null }
        .map { Changed(it.leastRecent!!, it.mostRecent!!) }
}