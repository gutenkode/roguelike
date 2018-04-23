package de.gutenko.roguelike.habittracker.data.goals

import com.androidhuman.rxfirebase2.database.*
import com.google.firebase.database.DatabaseReference
import de.gutenko.roguelike.habittracker.data.habits.valueExpected
import de.gutenko.roguelike.habittracker.data.habits.valueFor
import de.gutenko.roguelike.habittracker.data.player.*
import io.reactivex.Completable
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class FirebaseGoalRepository(
    private val databaseReference: DatabaseReference
) : GoalRepository {
    private val users = databaseReference.root.child("users")

    override fun observeUserGoals(userId: String): Observable<Set<Goal>> {
        return users
            .child(userId)
            .child("goals")
            .dataChanges()
            .map {
                it.children.map {
                    val addedMillis = it.valueExpected<Long>("added")
                    val added = DateTime(addedMillis, DateTimeZone.UTC).toLocalDateTime()

                    val completedOn = it.valueFor<Long>("completedOn")?.let {
                        DateTime(it, DateTimeZone.UTC).toLocalDateTime()
                    }

                    Goal(
                        it.valueExpected("id"),
                        it.valueExpected("userId"),
                        added,
                        it.valueExpected("name"),
                        it.child("playerUpdate").toPlayerUpdate(),
                        completedOn
                    )
                }
            }.map { it.toSet() }
    }

    override fun completeGoal(goalId: String, userId: String): Completable {
        val goal = users
            .child(userId)
            .child("goals")
            .child(goalId)

        return goal
            .rxUpdateChildren(mapOf("completedOn" to DateTime.now(DateTimeZone.UTC).millis))
            .andThen(
                goal.child("playerUpdate")
                    .data()
                    .map { it.toPlayerUpdate() }
                    .flatMapCompletable { playerUpdate ->
                        users.child(userId).child("player").data().map { it.toPlayer() }
                            .flatMapCompletable { player ->
                                databaseReference
                                    .child(userId)
                                    .rxSetValue(updatePlayer(player, playerUpdate))
                            }
                    })
    }

    override fun uncompleteGoal(goalId: String, userId: String): Completable {
        val goal = users
            .child(userId)
            .child("goals")
            .child(goalId)

        val playerRef = users.child(userId).child("player")

        return goal
            .rxUpdateChildren(mapOf("completedOn" to null))
            .andThen(
                goal.child("playerUpdate")
                    .data()
                    .map { it.toPlayerUpdate() }
                    .flatMapCompletable { playerUpdate ->
                        playerRef.data().map { it.toPlayer() }
                            .flatMapCompletable { player ->
                                playerRef.rxSetValue(unUpdatePlayer(player, playerUpdate))
                            }
                    }
            )
    }

    override fun addGoal(
        userId: String,
        name: String,
        playerUpdate: PlayerUpdate
    ): Completable {
        val push = users.child(userId)
            .child("goals")
            .push()

        return push.rxSetValue(
            GoalData(
                push.key,
                userId,
                DateTime.now(DateTimeZone.UTC).millis,
                name,
                playerUpdate,
                null
            )
        )
    }

    data class GoalData(
        val id: String,
        val userId: String,
        val added: Long,
        val name: String,
        val playerUpdate: PlayerUpdate,
        val completedOn: Long?
    )

    override fun removeGoal(goalId: String, userId: String): Completable {
        return users.child(userId).child("goals").child(goalId).rxRemoveValue()
    }
}