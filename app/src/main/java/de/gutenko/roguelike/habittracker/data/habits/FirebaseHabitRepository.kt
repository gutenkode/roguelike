package de.gutenko.roguelike.habittracker.data.habits

import com.androidhuman.rxfirebase2.database.dataChanges
import com.androidhuman.rxfirebase2.database.rxSetValue
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import de.gutenko.roguelike.habittracker.data.player.toPlayerUpdate
import io.reactivex.Completable
import io.reactivex.Observable

class FirebaseHabitRepository(private val firebaseDatabase: FirebaseDatabase) :
    HabitRepository {
    private val users = firebaseDatabase.reference.root.child("users")

    override fun observeUserHabits(userId: String): Observable<List<Habit>> {
        return users
            .child(userId)
            .child("habits")
            .dataChanges()
            .filter { it.exists() }
            .map {
                it.children.map {
                    Habit(
                        it.valueExpected("id"),
                        it.valueExpected("userId"),
                        it.valueExpected("name"),
                        it.child("playerUpdate").toPlayerUpdate(),
                        timeOfDay(it.child("timeOfDay")),
                        it.valueExpected("createdTime")
                    )
                }
            }
    }

    private fun timeOfDay(dataSnapshot: DataSnapshot): TimeOfDay {
        return TimeOfDay(
            dataSnapshot.valueExpected("hours"),
            dataSnapshot.valueExpected("minutes")
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
}