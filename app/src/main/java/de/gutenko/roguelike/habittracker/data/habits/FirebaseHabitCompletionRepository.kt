package de.gutenko.roguelike.habittracker.data.habits

import com.androidhuman.rxfirebase2.database.data
import com.androidhuman.rxfirebase2.database.dataChanges
import com.androidhuman.rxfirebase2.database.rxRemoveValue
import com.androidhuman.rxfirebase2.database.rxSetValue
import com.google.firebase.database.DatabaseReference
import de.gutenko.roguelike.habittracker.data.player.toPlayer
import de.gutenko.roguelike.habittracker.data.player.toPlayerUpdate
import de.gutenko.roguelike.habittracker.data.player.unUpdatePlayer
import de.gutenko.roguelike.habittracker.data.player.updatePlayer
import io.reactivex.Completable
import io.reactivex.Observable
import org.joda.time.LocalDate

class FirebaseHabitCompletionRepository(
    private val databaseReference: DatabaseReference
) : HabitCompletionRepository {
    private val users = databaseReference.child("users")

    override fun observeHabitCompletion(
        userId: String,
        habitId: String,
        localDate: LocalDate
    ): Observable<Optional<HabitCompletion>> {
        return users
            .child(userId)
            .child("completions")
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
            ).andThen(
                user.child("habits").child(habitId).child("playerUpdate").data()
                    .map { it.toPlayerUpdate() }
                    .flatMapCompletable { update ->
                        val playerRef = user.child("player")
                        playerRef
                            .data()
                            .map { it.toPlayer() }
                            .flatMapCompletable { player ->
                                val newPlayer = updatePlayer(player, update)

                                playerRef.rxSetValue(newPlayer)
                            }
                    })
    }

    override fun observeCompletionsForPlayer(
        userId: String,
        habitId: String
    ): Observable<List<HabitCompletion>> {
        return users
            .child(userId)
            .child(habitId)
            .dataChanges()
            .map {
                it.children.map {
                    val day = LocalDate.parse(it.valueExpected<String>("day"))

                    HabitCompletion(it.valueExpected("userId"), it.valueExpected("habitId"), day)
                }
            }
    }

    override fun removeCompletion(
        userId: String,
        habitId: String,
        localDate: LocalDate
    ): Completable {
        val user = users.child(userId)
        val completions = user.child("completions")
        val player = user.child("player")
        val habits = user.child("habits")

        return completions
            .child(habitId)
            .child(localDate.toString())
            .rxRemoveValue()
            .andThen(
                habits.child(habitId)
                    .child("playerUpdate")
                    .data()
                    .map { it.toPlayerUpdate() }
                    .flatMapCompletable { playerUpdate ->
                        player.data().map { it.toPlayer() }
                            .flatMapCompletable {
                                player.rxSetValue(unUpdatePlayer(it, playerUpdate))
                            }
                    }
            )
    }
}
