package de.gutenko.roguelike.habittracker.ui

import de.gutenko.roguelike.habittracker.data.habits.HabitCompletionRepository
import de.gutenko.roguelike.habittracker.data.habits.HabitRepository
import de.gutenko.roguelike.habittracker.data.habits.Optional
import io.reactivex.Observable
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import java.text.SimpleDateFormat
import java.util.Date

typealias HabitsViewState = List<HabitPresenter.HabitViewState>

class HabitPresenter(
    private val userId: String,
    private val habitRepository: HabitRepository,
    private val habitCompletionRepository: HabitCompletionRepository
) {
    data class HabitViewState(
        val habitId: String,
        val habitName: String,
        val habitDone: Boolean,
        val loading: Boolean,
        val habitTime: String
    )

    sealed class Event {
        data class HabitDone(val habitId: String) : Event()
        data class HabitUndone(val habitId: String) : Event()
    }

    fun viewStates(events: Observable<Event>): Observable<HabitsViewState> {
        val habitsLoaded = habitRepository.observeUserHabits(userId)
            .map { it.sortedBy { it.createdTime } }
            .flatMap { habits ->
                val completionsForToday = habits.map { habit ->
                    habitCompletionRepository.observeHabitCompletion(
                        userId,
                        habit.id,
                        // TODO: Make this rx
                        LocalDate.now()
                    )
                        .map { wasCompleted ->
                            val timeOfDay = habit.timeOfDay!!

                            val timeMillis =
                                LocalDateTime.now()
                                    .withHourOfDay(timeOfDay.hours)
                                    .withMinuteOfHour(timeOfDay.minutes)
                                    .toDateTime()
                                    .millis

                            val habitTimeString =
                                SimpleDateFormat.getTimeInstance(SimpleDateFormat.SHORT)
                                    .format(Date(timeMillis))

                            when (wasCompleted) {
                                is Optional.Some -> {
                                    HabitPresenter.HabitViewState(
                                        habit.id,
                                        habit.name,
                                        habitDone = true,
                                        loading = false,
                                        habitTime = habitTimeString
                                    )
                                }
                                is Optional.None -> HabitPresenter.HabitViewState(
                                    habit.id,
                                    habit.name,
                                    habitDone = false,
                                    loading = false,
                                    habitTime = habitTimeString
                                )
                            }
                        }
                }

                // Wait for all view states to come in before producing list
                Observable.combineLatest(completionsForToday) { viewStates ->
                    viewStates.map { it as HabitPresenter.HabitViewState }.toList()
                }
            }.map<Result> { Result.Loaded(it) }

        val addRemoveResults = events.flatMap<Result> {
            when (it) {
                is HabitPresenter.Event.HabitDone ->
                    habitCompletionRepository.addCompletion(
                        userId,
                        it.habitId,
                        LocalDate.now()
                    ).andThen(Observable.just<Result>(Result.HabitDone(it.habitId)))
                        .startWith(Result.HabitLoading(it.habitId))

                is HabitPresenter.Event.HabitUndone ->
                    habitCompletionRepository.removeCompletion(
                        userId,
                        it.habitId,
                        LocalDate.now()
                    ).andThen(Observable.just<Result>(Result.HabitUndone(it.habitId)))
                        .startWith(Result.HabitLoading(it.habitId))
            }
        }

        return habitsLoaded.mergeWith(addRemoveResults)
            .scan(emptyList(), { state, result ->
                when (result) {
                    is HabitPresenter.Result.Loaded -> result.viewStates

                    is HabitPresenter.Result.HabitLoading -> {
                        val habitIndex = state.indexOfFirst { it.habitId == result.habitId }
                        require(habitIndex != -1)

                        state.replaceAt(habitIndex, state[habitIndex].copy(loading = true))
                    }

                    is HabitPresenter.Result.HabitDone -> state
                    is HabitPresenter.Result.HabitUndone -> state
                }
            })
    }

    private sealed class Result {
        data class Loaded(val viewStates: List<HabitViewState>) : Result()
        data class HabitLoading(val habitId: String) : Result()
        data class HabitDone(val habitId: String) : Result()
        data class HabitUndone(val habitId: String) : Result()
    }
}
