package de.gutenko.roguelike.habittracker.data.habits

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface HabitRepository {
    fun observeUserHabits(userId: String): Observable<List<Habit>>
    fun addHabit(userId: String, habitData: HabitData): Completable
    fun removeHabit(userId: String, habitId: String): Completable
    fun getHabit(userId: String, habitId: String): Single<Habit>
}
