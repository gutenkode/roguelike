package de.gutenko.roguelike.habittracker.ui

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.widget.checkedChanges
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap
import de.gutenko.roguelike.BR
import de.gutenko.roguelike.R
import de.gutenko.roguelike.databinding.HabitListItemBinding
import de.gutenko.roguelike.habittracker.changes
import de.gutenko.roguelike.habittracker.data.HabitCompletionRepository
import de.gutenko.roguelike.habittracker.data.HabitRepository
import de.gutenko.roguelike.habittracker.data.Optional
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import org.joda.time.LocalDate
import javax.inject.Inject

@Subcomponent
interface HabitFragmentSubcomponent : AndroidInjector<HabitFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<HabitFragment>()
}

@Module(subcomponents = [HabitFragmentSubcomponent::class])
abstract class HabitFragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(HabitFragment::class)
    abstract fun bindHabitFragmentInjectorFactory(builder: HabitFragmentSubcomponent.Builder): AndroidInjector.Factory<out Fragment>
}

class HabitFragment : Fragment() {
    @Inject
    lateinit var habitRepository: HabitRepository
    @Inject
    lateinit var habitCompletionRepository: HabitCompletionRepository

    private lateinit var userId: String

    private val compositeDisposable = CompositeDisposable()

    private lateinit var habitAdapter: BindingListAdapter<HabitListItemBinding, HabitViewState, Event>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        userId = arguments?.getString(userIdKey)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_habits, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val addHabit = view.findViewById<FloatingActionButton>(R.id.addHabit)
        val context = requireContext()

        addHabit.clicks().subscribe {
            CreateHabitFragment.newInstance(userId)
                .show(requireFragmentManager(), CreateHabitFragment.tag)
        }

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        habitAdapter = bindingListAdapter()

        recyclerView.adapter = habitAdapter

        compositeDisposable.addAll(
            habitRepository.observeUserHabits(userId)
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
                                when (wasCompleted) {
                                    is Optional.Some -> HabitViewState(habit.id, habit.name, true)
                                    is Optional.None -> HabitViewState(habit.id, habit.name, false)
                                }
                            }
                    }

                    // Wait for all view states to come in before producing list
                    Observable.combineLatest(completionsForToday) { viewStates ->
                        viewStates.map { it as HabitViewState }.toList()
                    }
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    habitAdapter.submitList(it)
                },

            habitAdapter.events.flatMapCompletable {
                when (it) {
                    is Event.HabitDone -> {
                        habitCompletionRepository.addCompletion(userId, it.habitId, LocalDate.now())
                    }

                    is Event.HabitUndone -> {
                        habitCompletionRepository.addCompletion(userId, it.habitId, LocalDate.now())
                    }
                }
            }.subscribe()
        )

        return view
    }

    private fun bindingListAdapter(): BindingListAdapter<HabitListItemBinding, HabitViewState, Event> {
        return BindingListAdapter.Builder<HabitListItemBinding, HabitViewState, Event>()
            .same { firstHabit, secondHabit -> firstHabit == secondHabit }
            .identical { first, second -> first.habitId == second.habitId }
            .variable(BR.habit)
            .eventsFor { binding, i ->
                val habit = binding.habit!!

                binding
                    .checkbox
                    .checkedChanges()
                    .changes()
                    .map<Event> {
                        when {
                            it.from == false && it.to == true -> Event.HabitDone(habit.habitId)
                            it.from == true && it.to == false -> Event.HabitUndone(habit.habitId)
                            else -> throw IllegalStateException("Self change found")
                        }
                    }
            }
            .build(R.layout.habit_list_item)
    }

    private sealed class Event {
        data class HabitDone(val habitId: String) : Event()
        data class HabitUndone(val habitId: String) : Event()
    }

    data class HabitViewState(val habitId: String, val habitName: String, val habitDone: Boolean)

    companion object {
        private val userIdKey = "userIdKey"

        fun newInstance(userId: String): HabitFragment {
            return HabitFragment().apply {
                arguments = Bundle().apply {
                    putString(userIdKey, userId)
                }
            }
        }
    }
}

