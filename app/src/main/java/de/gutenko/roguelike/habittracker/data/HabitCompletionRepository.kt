package de.gutenko.roguelike.habittracker.data

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.CompletableSource
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

    fun addCompletion(userId: String, habitId: String, localDate: LocalDate): Completable

    fun removeCompletion(userId: String, habitId: String, localDate: LocalDate): Completable
}

class MemoryHabitCompletionRepository : HabitCompletionRepository {
    private val habitCompletionRelay = BehaviorRelay.createDefault(emptySet<HabitCompletion>())

    override fun observeHabitCompletion(
        userId: String,
        habitId: String,
        localDate: LocalDate
    ): Observable<Optional<HabitCompletion>> {
        val habitCompletion = HabitCompletion(userId, habitId, localDate)

        return habitCompletionRelay.map {
            if (habitCompletion in it)
                Optional.Some(habitCompletion)
            else
                Optional.None
        }
    }

    override fun addCompletion(userId: String, habitId: String, localDate: LocalDate): Completable {
        return Completable.defer {
            habitCompletionRelay.accept(
                habitCompletionRelay.value + HabitCompletion(
                    userId,
                    habitId,
                    localDate
                )
            )

            Completable.complete()
        }
    }

    override fun removeCompletion(
        userId: String,
        habitId: String,
        localDate: LocalDate
    ): Completable {
        return Completable.defer {
            habitCompletionRelay.accept(
                habitCompletionRelay.value - HabitCompletion(userId, habitId, localDate)
            )

            Completable.complete()
        }
    }
}