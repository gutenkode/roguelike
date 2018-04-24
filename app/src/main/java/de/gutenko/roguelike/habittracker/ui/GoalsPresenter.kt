package de.gutenko.roguelike.habittracker.ui

import com.jakewharton.rxrelay2.PublishRelay
import de.gutenko.roguelike.habittracker.data.goals.Goal
import de.gutenko.roguelike.habittracker.data.goals.GoalRepository
import io.reactivex.Observable

class GoalsPresenter(private val goalRepository: GoalRepository, private val userId: String) {
    sealed class Event {
        data class GoalMarkedDone(val goalId: String) : Event()
        data class GoalMarkedUndone(val goalId: String) : Event()
        data class GoalDelete(val goalId: String) : Event()
        data class GoalDeletePrompt(val goalId: String) : Event()
        data class GoalSelected(val goalId: String) : Event()
    }

    data class GoalViewState(
        val goalId: String,
        val name: String,
        val completed: Boolean,
        val completedOnString: String,
        val loading: Boolean
    )

    sealed class Effect {
        data class GoalConfirmUndone(val goalId: String) : Effect()
        data class GoalDeleteConfirm(val goalId: String) : Effect()
        data class GoalShow(val goalId: String) : Effect()
    }

    private val effectSubject = PublishRelay.create<Effect>()
    fun effects(): Observable<Effect> = effectSubject

    fun viewStates(events: Observable<Event>): Observable<List<GoalViewState>> {
        val goalsLoaded =
            goalRepository.observeUserGoals(userId)
                .map {
                    val (completed, notCompleted) = it.partition { it.completedOn != null }
                    val completedComparator = compareBy<Goal> { it.completedOn }.thenBy { it.name }
                    val notCompletedComparator = compareBy<Goal> { it.added }.thenBy { it.name }

                    notCompleted.sortedWith(notCompletedComparator) + completed.sortedWith(
                        completedComparator
                    )
                }
                .map { Result.GoalsLoaded(it) }

        val viewStates = events
            .flatMap {
                when (it) {
                    is Event.GoalMarkedDone -> {
                        goalRepository.completeGoal(it.goalId, userId)
                            .andThen(Observable.just<Result>(Result.GoalDone(it.goalId)))
                            .startWith(Result.GoalLoading(it.goalId))
                    }

                    is Event.GoalMarkedUndone -> {
                        goalRepository.uncompleteGoal(it.goalId, userId)
                            .andThen(Observable.just<Result>(Result.GoalUndone(it.goalId)))
                            .startWith(Result.GoalLoading(it.goalId))
                    }

                    is GoalsPresenter.Event.GoalDeletePrompt -> {
                        effectSubject.accept(Effect.GoalDeleteConfirm(it.goalId))
                        Observable.empty()
                    }

                    is GoalsPresenter.Event.GoalDelete -> {
                        goalRepository.removeGoal(it.goalId, userId)
                            .andThen(Observable.never())
                    }

                    is GoalsPresenter.Event.GoalSelected -> {
                        effectSubject.accept(Effect.GoalShow(it.goalId))
                        Observable.empty<Result>()
                    }
                }
            }.mergeWith(goalsLoaded)
            .scan(emptyMap<String, GoalViewState>(),
                { state, result ->
                    when (result) {
                        is Result.GoalDone -> {
                            // No-op
                            state
                        }

                        is Result.GoalUndone -> {
                            effectSubject.accept(Effect.GoalConfirmUndone(result.goalId))
                            state
                        }

                        is Result.GoalLoading -> {
                            state + (result.goalId to state[result.goalId]!!.copy(loading = true))
                        }

                        is Result.GoalsLoaded -> {
                            result.goals.map { it.id to goalViewState(it) }.toMap()
                        }
                    }
                })
            .map { it.values.toList() }

        return viewStates
    }

    private sealed class Result {
        data class GoalDone(val goalId: String) : Result()
        data class GoalUndone(val goalId: String) : Result()
        data class GoalLoading(val goalId: String) : Result()
        data class GoalsLoaded(val goals: List<Goal>) : Result()
    }

    private fun goalViewState(goal: Goal): GoalViewState {
        return GoalViewState(
            goal.id,
            goal.name,
            goal.completedOn != null,
            goal.completedOn?.let {
                "Completed on $it"
            } ?: "GoalDeleteConfirm not completed yet!",
            loading = false)
    }
}