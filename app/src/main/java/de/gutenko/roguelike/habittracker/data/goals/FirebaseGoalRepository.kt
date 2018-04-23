package de.gutenko.roguelike.habittracker.data.goals

import com.androidhuman.rxfirebase2.database.dataChanges
import com.androidhuman.rxfirebase2.database.rxRemoveValue
import com.androidhuman.rxfirebase2.database.rxSetValue
import com.androidhuman.rxfirebase2.database.rxUpdateChildren
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import de.gutenko.roguelike.habittracker.data.habits.valueExpected
import de.gutenko.roguelike.habittracker.data.habits.valueFor
import de.gutenko.roguelike.habittracker.data.player.PlayerUpdate
import io.reactivex.Completable
import io.reactivex.Observable
import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class FirebaseGoalRepository(private val firebaseDatabase: FirebaseDatabase) : GoalRepository {
    override fun observeUserGoals(userId: String): Observable<Set<Goal>> {
        return goalsReference
            .child(userId)
            .dataChanges()
            .filter { it.exists() }
            .map {
                it.children
                    .filter { it.exists() }
                    .map {
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
                            playerUpdate(it.child("playerUpdate")),
                            completedOn
                        )
                    }
            }.map { it.toSet() }
    }

    private fun playerUpdate(dataSnapshot: DataSnapshot): PlayerUpdate {
        return PlayerUpdate(
            dataSnapshot.valueExpected("attackUpdate"),
            dataSnapshot.valueExpected("agilityUpdate"),
            dataSnapshot.valueExpected("enduranceUpdate"),
            dataSnapshot.valueExpected("intelligenceUpdate")
        )
    }


    override fun completeGoal(goalId: String, userId: String): Completable {
        return goalsReference
            .child(userId)
            .child(goalId)
            .rxUpdateChildren(mapOf("completedOn" to DateTime.now(DateTimeZone.UTC).millis))
    }

    override fun uncompleteGoal(goalId: String, userId: String): Completable {
        return goalsReference
            .child(userId)
            .child(goalId)
            .rxUpdateChildren(mapOf("completedOn" to null))
    }

    override fun addGoal(userId: String, name: String, playerUpdate: PlayerUpdate): Completable {
        val push = goalsReference
            .child(userId)
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

    private val goalsReference = firebaseDatabase.reference.root.child("goals")

    override fun removeGoal(goalId: String, userId: String): Completable {
        return goalsReference.child(userId).child(goalId).rxRemoveValue()
    }
}