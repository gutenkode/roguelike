package de.gutenko.roguelike.habittracker.data.habits

import io.reactivex.Completable
import io.reactivex.Observable

interface HabitRepository {
    fun observeUserHabits(userId: String): Observable<List<Habit>>
    fun addHabit(userId: String, habitData: HabitData): Completable
}
