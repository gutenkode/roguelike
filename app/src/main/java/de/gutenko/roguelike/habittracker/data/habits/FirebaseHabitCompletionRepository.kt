package de.gutenko.roguelike.habittracker.data.habits

import com.androidhuman.rxfirebase2.database.dataChanges
import com.androidhuman.rxfirebase2.database.rxRemoveValue
import com.androidhuman.rxfirebase2.database.rxSetValue
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import io.reactivex.Completable
import io.reactivex.Observable
import org.joda.time.LocalDate

class FirebaseHabitCompletionRepository(private val users: DatabaseReference) :
    HabitCompletionRepository {
    override fun observeHabitCompletion(
        userId: String,
        habitId: String,
        localDate: LocalDate
    ): Observable<Optional<HabitCompletion>> =
        users
            .child(userId)
            .child("completions")
            .child(habitId)
            .child(localDate.toString())
            .dataChanges()
            .map {
                when {
                    it.exists() -> Optional.Some(it.toCompletion())
                    else -> Optional.None
                }
            }

    override fun addCompletion(userId: String, habitId: String, localDate: LocalDate): Completable {
        val user = users.child(userId)

        return user
            .child("completions")
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

    override fun observeCompletionsForPlayer(
        userId: String,
        habitId: String
    ): Observable<List<HabitCompletion>> =
        users
            .child(userId)
            .child(habitId)
            .dataChanges()
            .map {
                it.children.map { it.toCompletion() }
            }

    override fun removeCompletion(
        userId: String,
        habitId: String,
        localDate: LocalDate
    ): Completable {
        val user = users.child(userId)
        val completions = user.child("completions")

        return completions
            .child(habitId)
            .child(localDate.toString())
            .rxRemoveValue()
    }

    private fun DataSnapshot.toCompletion(): HabitCompletion {
        return HabitCompletion(
            valueExpected("userId"),
            valueExpected("habitId"),
            LocalDate.parse(valueExpected("day"))
        )
    }
}
