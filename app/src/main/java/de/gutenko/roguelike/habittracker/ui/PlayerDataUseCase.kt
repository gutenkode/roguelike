package de.gutenko.roguelike.habittracker.ui

import de.gutenko.roguelike.habittracker.data.goals.GoalRepository
import de.gutenko.roguelike.habittracker.data.habits.HabitCompletionRepository
import de.gutenko.roguelike.habittracker.data.habits.HabitRepository
import de.gutenko.roguelike.habittracker.data.player.PlayerUpdate
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class PlayerDataUseCase(
    private val habitCompletionRepository: HabitCompletionRepository,
    private val habitRepository: HabitRepository,
    private val goalRepository: GoalRepository
) {
    fun playerData(userId: String): Observable<PlayerUpdate> {
        return Observable.combineLatest(
            habitUpdate(userId),
            goalUpdate(userId),
            BiFunction { p, q ->
                plus(p, q)
            })
    }

    private fun habitUpdate(userId: String): Observable<PlayerUpdate> {
        return habitRepository.observeUserHabits(userId)
            .flatMapSingle { habits ->
                Observable.fromIterable(habits)
                    .flatMap { habit ->
                        habitCompletionRepository.observeCompletionsForPlayer(userId, habit.id)
                            .map { it.size }
                            .map { habit.playerUpdate * it }
                    }.reduce(PlayerUpdate(0, 0, 0, 0), this::plus)
            }
    }

    private fun goalUpdate(userId: String): Observable<PlayerUpdate> {
        return goalRepository.observeUserGoals(userId)
            .map { goals ->
                goals
                    .map { it.playerUpdate }
                    .fold(PlayerUpdate(0, 0, 0, 0), this::plus)
            }
    }

    private operator fun PlayerUpdate.times(scale: Int): PlayerUpdate {
        return PlayerUpdate(
            attackUpdate = attackUpdate * scale,
            agilityUpdate = agilityUpdate * scale,
            enduranceUpdate = agilityUpdate * scale,
            intelligenceUpdate = intelligenceUpdate * scale
        )
    }

    private fun plus(
        p: PlayerUpdate,
        q: PlayerUpdate
    ): PlayerUpdate {
        return PlayerUpdate(
            p.attackUpdate + q.attackUpdate,
            p.agilityUpdate + q.agilityUpdate,
            p.enduranceUpdate + q.enduranceUpdate,
            p.intelligenceUpdate + q.intelligenceUpdate
        )
    }
}