package de.gutenko.roguelike.habittracker.data.goals

import com.jakewharton.rxrelay2.BehaviorRelay
import de.gutenko.roguelike.habittracker.data.player.PlayerUpdate
import io.reactivex.Completable
import io.reactivex.Observable
import org.joda.time.LocalDateTime
import java.util.concurrent.TimeUnit

class MemoryGoalRepository : GoalRepository {
    private var nextGoalId: Int = 0
    private val goalRelay =
        BehaviorRelay.createDefault<Map<String, Goal>>(
            emptyMap()
        )

    override fun observeUserGoals(userId: String): Observable<Set<Goal>> {
        return goalRelay.map { it.filterValues { it.userId == userId }.values.toSet() }
    }

    override fun completeGoal(goalId: String, userId: String): Completable {
        return Completable.complete()
            .delay(5, TimeUnit.SECONDS)
            .andThen(Completable.defer {
                val goal = goalRelay.value[goalId]?.copy(completedOn = LocalDateTime.now())
                        ?: throw IllegalArgumentException("Goal $goalId does not exist")

                goalRelay.accept(goalRelay.value + (goalId to goal))

                Completable.complete()
            }).delay(500, TimeUnit.MILLISECONDS)
    }

    override fun uncompleteGoal(goalId: String, userId: String): Completable {
        return Completable.complete()
            .delay(5, TimeUnit.SECONDS)
            .andThen(Completable.defer {
                val goal = goalRelay.value[goalId]?.copy(completedOn = null)
                        ?: throw IllegalArgumentException("Goal $goalId does not exist")

                goalRelay.accept(goalRelay.value + (goalId to goal))

                Completable.complete()
            }).delay(500, TimeUnit.MILLISECONDS)
    }

    override fun addGoal(userId: String, name: String, playerUpdate: PlayerUpdate): Completable {
        return Completable.defer {
            val goalId = nextGoalId.toString()

            nextGoalId++

            goalRelay.accept(
                goalRelay.value + (goalId to Goal(
                    goalId,
                    userId,
                    LocalDateTime.now(),
                    name,
                    playerUpdate,
                    completedOn = null
                ))
            )

            Completable.complete()
        }
    }

    override fun removeGoal(goalId: String, userId: String): Completable {
        return Completable.complete().delay(5, TimeUnit.SECONDS).andThen(
            Completable.defer {
                goalRelay.accept(goalRelay.value - goalId)

                Completable.complete()
            })

    }
}