package de.gutenko.roguelike.habittracker.data.goals

import de.gutenko.roguelike.habittracker.data.player.PlayerUpdate
import io.reactivex.Completable
import io.reactivex.Observable

interface GoalRepository {
    fun observeUserGoals(userId: String): Observable<Set<Goal>>
    fun completeGoal(goalId: String, userId: String): Completable
    fun uncompleteGoal(goalId: String, userId: String): Completable
    fun addGoal(userId: String, name: String, playerUpdate: PlayerUpdate): Completable
    fun removeGoal(goalId: String, userId: String): Completable
}

