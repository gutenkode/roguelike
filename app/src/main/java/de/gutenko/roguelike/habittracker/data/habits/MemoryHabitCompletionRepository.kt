package de.gutenko.roguelike.habittracker.data.habits

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.Observable
import org.joda.time.LocalDate
import java.util.concurrent.TimeUnit

class MemoryHabitCompletionRepository :
    HabitCompletionRepository {
    private val habitCompletionRelay =
        BehaviorRelay.createDefault(emptySet<HabitCompletion>())

    override fun observeHabitCompletion(
        userId: String,
        habitId: String,
        localDate: LocalDate
    ): Observable<Optional<HabitCompletion>> {
        val habitCompletion = HabitCompletion(
            userId,
            habitId,
            localDate
        )

        return habitCompletionRelay.map {
            if (habitCompletion in it)
                Optional.Some(habitCompletion)
            else
                Optional.None
        }
    }

    override fun addCompletion(userId: String, habitId: String, localDate: LocalDate): Completable {
        return Completable.complete().delay(2, TimeUnit.SECONDS).andThen(
            Completable.defer {
                habitCompletionRelay.accept(
                    habitCompletionRelay.value + HabitCompletion(
                        userId,
                        habitId,
                        localDate
                    )
                )

                Completable.complete()
            })
    }

    override fun removeCompletion(
        userId: String,
        habitId: String,
        localDate: LocalDate
    ): Completable {
        return Completable.complete().delay(2, TimeUnit.SECONDS).andThen(
            Completable.defer {
                habitCompletionRelay.accept(
                    habitCompletionRelay.value - HabitCompletion(
                        userId,
                        habitId,
                        localDate
                    )
                )

                Completable.complete()
            })
    }
}