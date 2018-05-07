package de.gutenko.roguelike.habittracker.data.habits

import com.androidhuman.rxfirebase2.database.data
import com.androidhuman.rxfirebase2.database.dataChanges
import com.androidhuman.rxfirebase2.database.rxRemoveValue
import com.androidhuman.rxfirebase2.database.rxSetValue
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import de.gutenko.roguelike.habittracker.androidLog
import de.gutenko.roguelike.habittracker.data.player.toPlayerUpdate
import de.gutenko.roguelike.habittracker.onErrorComplete
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

class FirebaseHabitRepository(private val firebaseDatabase: FirebaseDatabase) :
    HabitRepository {
    private val users = firebaseDatabase.reference.root.child("users")

    override fun observeUserHabits(userId: String): Observable<List<Habit>> {
        return users
            .child(userId)
            .child("habits")
            .dataChanges()
            .onErrorComplete()
            .androidLog("Habits")
            .map {
                it.children.map {
                    Habit(
                        it.valueExpected("id"),
                        it.valueExpected("userId"),
                        it.valueExpected("name"),
                        it.child("playerUpdate").toPlayerUpdate(),
                        it.child("timeOfDay").timeOfDay(),
                        it.valueExpected("createdTime")
                    )
                }
            }
    }

    private fun DataSnapshot.timeOfDay(): TimeOfDay {
        return TimeOfDay(
            valueExpected("hours"),
            valueExpected("minutes")
        )
    }

    override fun addHabit(userId: String, habitData: HabitData): Completable {
        val push = users
            .child(userId)
            .child("habits")
            .push()

        return push
            .rxSetValue(
                Habit(
                    push.key,
                    userId,
                    habitData.name,
                    habitData.playerUpdate,
                    habitData.timeOfDay,
                    System.currentTimeMillis()
                )
            )
    }

    override fun removeHabit(userId: String, habitId: String): Completable {
        return users.child(userId).child("habits").child(habitId).rxRemoveValue()
    }

    override fun getHabit(userId: String, habitId: String): Single<Habit> {
        return users.child(userId).child("habits").child(habitId).data().map {
            Habit(
                it.valueExpected("id"),
                it.valueExpected("userId"),
                it.valueExpected("name"),
                it.child("playerUpdate").toPlayerUpdate(),
                it.child("timeOfDay").timeOfDay(),
                it.valueExpected("createdTime")
            )
        }
    }
}