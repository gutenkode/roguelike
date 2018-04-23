package de.gutenko.roguelike.habittracker.data.habits

import com.androidhuman.rxfirebase2.database.dataChanges
import com.androidhuman.rxfirebase2.database.rxSetValue
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import de.gutenko.roguelike.habittracker.data.player.PlayerUpdate
import io.reactivex.Completable
import io.reactivex.Observable

class FirebaseHabitRepository(private val firebaseDatabase: FirebaseDatabase) :
    HabitRepository {
    private val habitsReference = firebaseDatabase.reference.root.child("habits")

    override fun observeUserHabits(userId: String): Observable<List<Habit>> {
        return habitsReference
            .child(userId)
            .dataChanges()
            .filter { it.exists() }
            .map {
                it.children.map {
                    Habit(
                        it.valueExpected("id"),
                        it.valueExpected("userId"),
                        it.valueExpected("name"),
                        playerUpdate(it.child("playerUpdate")),
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

    private fun playerUpdate(dataSnapshot: DataSnapshot): PlayerUpdate {
        return PlayerUpdate(
            dataSnapshot.valueExpected("attackUpdate"),
            dataSnapshot.valueExpected("agilityUpdate"),
            dataSnapshot.valueExpected("enduranceUpdate"),
            dataSnapshot.valueExpected("intelligenceUpdate")
        )
    }

    override fun addHabit(userId: String, habitData: HabitData): Completable {
        val push = habitsReference
            .child(userId)
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