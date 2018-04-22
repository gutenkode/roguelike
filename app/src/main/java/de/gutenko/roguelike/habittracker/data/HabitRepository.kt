package de.gutenko.roguelike.habittracker.data

import com.jakewharton.rxrelay2.BehaviorRelay
import io.reactivex.Completable
import io.reactivex.Observable

interface HabitRepository {
    fun observeUserHabits(userId: String): Observable<List<Habit>>
    fun addHabit(userId: String, habitData: HabitData): Completable
}

class MemoryHabitRepository : HabitRepository {
    private var nextHabitId = 0
    private val habitsRelay = BehaviorRelay.createDefault(emptyList<Habit>())

    override fun observeUserHabits(userId: String): Observable<List<Habit>> {
        return habitsRelay.map { it.filter { it.userId == userId } }
    }

    override fun addHabit(userId: String, habitData: HabitData): Completable {
        val habit = Habit(
            nextHabitId.toString(),
            userId,
            habitData.name,
            habitData.playerUpdate,
            habitData.timeOfDay,
            0
        )

        habitsRelay.accept(habitsRelay.value + habit)

        nextHabitId++

        return Completable.complete()
    }
}