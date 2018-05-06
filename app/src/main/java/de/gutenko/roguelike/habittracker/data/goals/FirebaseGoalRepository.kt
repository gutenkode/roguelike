package de.gutenko.roguelike.habittracker.data.goals

import com.androidhuman.rxfirebase2.database.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import de.gutenko.roguelike.habittracker.data.habits.valueExpected
import de.gutenko.roguelike.habittracker.data.habits.valueFor
import de.gutenko.roguelike.habittracker.data.player.PlayerUpdate
import de.gutenko.roguelike.habittracker.data.player.toPlayerUpdate
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class FirebaseGoalRepository(private val users: DatabaseReference) : GoalRepository {
    override fun observeUserGoals(userId: String): Observable<Set<Goal>> =
        users
            .child(userId)
            .child("goals")
            .dataChanges()
            .map { it.children.map { it.toGoal() } }
            .map { it.toSet() }

    override fun completeGoal(goalId: String, userId: String): Completable {
        val goal = users
            .child(userId)
            .child("goals")
            .child(goalId)

        return goal
            .rxUpdateChildren(mapOf("completedOn" to DateTime.now(DateTimeZone.UTC).millis))
    }

    override fun uncompleteGoal(goalId: String, userId: String): Completable {
        val goal = users
            .child(userId)
            .child("goals")
            .child(goalId)

        return goal.rxUpdateChildren(mapOf("completedOn" to null))
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

    override fun getGoal(userId: String, goalId: String): Single<Goal> =
        users
            .child(userId)
            .child("goals")
            .child(goalId)
            .data()
            .map { it.toGoal() }

    private fun DataSnapshot.toGoal(): Goal {
        val addedMillis = valueExpected<Long>("added")
        val added = DateTime(addedMillis, DateTimeZone.UTC).toLocalDateTime()

        val completedOn = valueFor<Long>("completedOn")?.let {
            DateTime(it, DateTimeZone.UTC).toLocalDateTime()
        }

        return Goal(
            valueExpected("id"),
            valueExpected("userId"),
            added,
            valueExpected("name"),
            child("playerUpdate").toPlayerUpdate(),
            completedOn
        )
    }
}