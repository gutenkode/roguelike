package de.gutenko.roguelike.habittracker.data.habits

import com.androidhuman.rxfirebase2.database.dataChanges
import com.androidhuman.rxfirebase2.database.rxRemoveValue
import com.androidhuman.rxfirebase2.database.rxSetValue
import com.google.firebase.database.DatabaseReference
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

    fun addCompletion(userId: String, habitId: String, localDate: LocalDate): Completable

    fun removeCompletion(userId: String, habitId: String, localDate: LocalDate): Completable
}

class FirebaseHabitCompletionRepository(private val completionsReference: DatabaseReference) :
    HabitCompletionRepository {
    override fun observeHabitCompletion(
        userId: String,
        habitId: String,
        localDate: LocalDate
    ): Observable<Optional<HabitCompletion>> {
        return completionsReference
            .child(userId)
            .child(habitId)
            .child(localDate.toString())
            .dataChanges()
            .map {
                if (it.exists()) {
                    val day = LocalDate.parse(it.valueExpected<String>("day"))

                    Optional.Some(
                        HabitCompletion(
                            it.valueExpected("userId"),
                            it.valueExpected("habitId"),
                            day
                        )
                    )
                } else {
                    Optional.None
                }
            }
    }

    override fun addCompletion(userId: String, habitId: String, localDate: LocalDate): Completable {
        return completionsReference
            .child(userId)
            .child(habitId)
            .child(localDate.toString())
            .rxSetValue(
                mapOf(
                    "userId" to userId,
                    "habitId" to habitId,
                    "day" to localDate.toString()
                )
            )
    }

    override fun removeCompletion(
        userId: String,
        habitId: String,
        localDate: LocalDate
    ): Completable {
        return completionsReference
            .child(userId)
            .child(habitId)
            .rxRemoveValue()
    }
}

