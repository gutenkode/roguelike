package de.gutenko.roguelike.habittracker.data.habits

import io.reactivex.Completable
import io.reactivex.Observable
import org.joda.time.LocalDate

sealed class Optional<out T> {
    data class Some<T>(val t: T) : Optional<T>()
    object None : Optional<Nothing>()
}

interface HabitCompletionRepository {
    fun observeHabitCompletion(
        userId: String,
        habitId: String,
        localDate: LocalDate
    ): Observable<Optional<HabitCompletion>>

    fun observeCompletionsForPlayer(
        userId: String,
        habitId: String
    ): Observable<List<HabitCompletion>>

    fun addCompletion(userId: String, habitId: String, localDate: LocalDate): Completable

    fun removeCompletion(userId: String, habitId: String, localDate: LocalDate): Completable
}

