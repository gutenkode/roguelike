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
import de.gutenko.roguelike.habittracker.data.habits.HabitCompletionRepository
import de.gutenko.roguelike.habittracker.data.habits.HabitRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
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
    private lateinit var presenter: HabitPresenter

    private val compositeDisposable = CompositeDisposable()

    private lateinit var habitAdapter: BindingListAdapter<HabitListItemBinding, HabitPresenter.HabitViewState, HabitPresenter.Event>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        userId = arguments?.getString(userIdKey)!!

        presenter = HabitPresenter(userId, habitRepository, habitCompletionRepository)
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

        compositeDisposable.add(
            presenter.viewStates(habitAdapter.events)
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    habitAdapter.submitList(it)
                }
        )

        return view
    }

    private fun bindingListAdapter(): BindingListAdapter<HabitListItemBinding, HabitPresenter.HabitViewState, HabitPresenter.Event> {
        return BindingListAdapter.Builder<HabitListItemBinding, HabitPresenter.HabitViewState, HabitPresenter.Event>()
            .same { firstHabit, secondHabit -> firstHabit == secondHabit }
            .identical { first, second -> first.habitId == second.habitId }
            .variable(BR.habit)
            .eventsFor { binding, i ->
                val habit = binding.habit!!

                binding
                    .checkbox
                    .clicks()
                    .map<HabitPresenter.Event> {
                        when {
                            habit.habitDone -> HabitPresenter.Event.HabitUndone(habit.habitId)
                            !habit.habitDone -> HabitPresenter.Event.HabitDone(habit.habitId)
                            else -> {
                                throw IllegalStateException()
                            }
                        }
                    }
            }
            .build(R.layout.habit_list_item)
    }


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

