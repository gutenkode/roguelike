package de.gutenko.roguelike.habittracker.ui

import android.app.AlertDialog
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.jakewharton.rxbinding2.view.clicks
import com.jakewharton.rxbinding2.view.longClicks
import com.jakewharton.rxrelay2.PublishRelay
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
import de.gutenko.roguelike.habittracker.ui.HabitPresenter.Event
import io.reactivex.Observable
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

        val emptyView = view.findViewById<LinearLayout>(R.id.empty_view)
        emptyView.visibility = View.INVISIBLE

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

        val eventRelay = PublishRelay.create<Event>()

        compositeDisposable.addAll(
            presenter.viewStates(habitAdapter.events.mergeWith(eventRelay))
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    if (it.isEmpty()) {
                        emptyView.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        emptyView.visibility = View.INVISIBLE
                        recyclerView.visibility = View.VISIBLE
                    }
                    habitAdapter.submitList(it)
                },

            presenter.effects().observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (it) {
                        is HabitPresenter.Effect.HabitDeleteSelected -> AlertDialog.Builder(
                            requireContext()
                        )
                            .setTitle(getString(R.string.delete_habit))
                            .setPositiveButton(android.R.string.yes) { dialog, which ->
                                eventRelay.accept(Event.HabitDeleted(it.habitId))
                            }.setNegativeButton(android.R.string.no) { dialog, which ->
                                // No-op
                            }
                            .create()
                            .show()

                        is HabitPresenter.Effect.HabitSelected -> {
                            HabitDialogFragment.newInstance(userId, it.habitId)
                                .show(fragmentManager, "tag")
                        }
                    }
                }
        )

        return view
    }

    private fun bindingListAdapter(): BindingListAdapter<HabitListItemBinding, HabitPresenter.HabitViewState, HabitPresenter.Event> {
        return BindingListAdapter.Builder<HabitListItemBinding, HabitPresenter.HabitViewState, HabitPresenter.Event>()
            .same { firstHabit, secondHabit -> firstHabit == secondHabit }
            .identical { first, second -> first.habitId == second.habitId }
            .variable(BR.habit)
            .eventsFor { binding ->
                val doneUndone = binding
                    .checkbox
                    .clicks()
                    .map<HabitPresenter.Event> {
                        val habit = binding.habit!!
                        when {
                            habit.habitDone -> Event.HabitUndone(habit.habitId)
                            !habit.habitDone -> Event.HabitDone(habit.habitId)
                            else -> {
                                throw IllegalStateException()
                            }
                        }
                    }

                val map: Observable<HabitPresenter.Event> = binding
                    .root
                    .longClicks()
                    .map<HabitPresenter.Event> {
                        val habit = binding.habit!!
                        Event.HabitDeleteSelected(habit.habitId)
                    }

                val selects = binding.root
                    .clicks()
                    .map {
                        val habit = binding.habit!!
                        Event.HabitSelected(habit.habitId)
                    }

                map.mergeWith(doneUndone).mergeWith(selects)
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

