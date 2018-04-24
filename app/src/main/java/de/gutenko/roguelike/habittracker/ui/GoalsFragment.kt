package de.gutenko.roguelike.habittracker.ui

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import de.gutenko.roguelike.databinding.GoalListItemBinding
import de.gutenko.roguelike.habittracker.androidLog
import de.gutenko.roguelike.habittracker.data.goals.GoalRepository
import de.gutenko.roguelike.habittracker.ui.GoalsPresenter.*
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_goals.addGoal
import javax.inject.Inject

@Subcomponent
interface GoalsFragmentSubcomponent : AndroidInjector<GoalsFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<GoalsFragment>()
}

@Module(subcomponents = [GoalsFragmentSubcomponent::class])
abstract class GoalsFragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(GoalsFragment::class)
    abstract fun bindGoalFragmentInjectorFactory(builder: GoalsFragmentSubcomponent.Builder): AndroidInjector.Factory<out Fragment>
}

typealias GoalAdapter<T, U> = BindingListAdapter<GoalListItemBinding, T, U>

class GoalsFragment : Fragment() {
    @Inject
    lateinit var goalRepository: GoalRepository

    private lateinit var userId: String

    private val compositeDisposable = CompositeDisposable()

    private lateinit var goalAdapter: GoalAdapter<GoalsPresenter.GoalViewState, GoalsPresenter.Event>

    private lateinit var presenter: GoalsPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        userId = arguments!!.getString(userIdKey)

        presenter = GoalsPresenter(goalRepository, userId)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_goals, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val context = requireContext()

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )

        goalAdapter = bindingListAdapter()

        recyclerView.adapter = goalAdapter

        return view
    }

    private fun bindingListAdapter(): GoalAdapter<GoalViewState, Event> {
        return BindingListAdapter.Builder<GoalListItemBinding, GoalsPresenter.GoalViewState, GoalsPresenter.Event>()
            .identical { first, second -> first.goalId == second.goalId }
            .same { first, second -> first == second }
            .eventsFor { binding, i ->
                val goal = binding.goal!!

                binding.checkbox
                    .clicks()
                    .map {
                        if (goal.completed)
                            Event.GoalMarkedUndone(goal.goalId)
                        else
                            Event.GoalMarkedDone(goal.goalId)
                    }.mergeWith(
                        binding.root
                            .longClicks()
                            .map { Event.GoalDeletePrompt(goal.goalId) }
                    ).mergeWith(
                        binding.root
                            .clicks()
                            .map { Event.GoalSelected(goal.goalId) }
                    )
            }
            .ids { it.goalId.hashCode().toLong() }
            .variable(BR.goal)
            .build(R.layout.goal_list_item)
    }

    override fun onStart() {
        super.onStart()

        addGoal.clicks().subscribe {
            CreateGoalFragment.newInstance(userId)
                .show(requireFragmentManager(), CreateGoalFragment.tag)
        }

        val eventRelay = PublishRelay.create<Event>()

        val viewStates = presenter.viewStates(goalAdapter.events.mergeWith(eventRelay))

        compositeDisposable.addAll(
            viewStates.observeOn(AndroidSchedulers.mainThread())
                .distinctUntilChanged()
                .androidLog("ViewStates")
                .subscribe {
                    goalAdapter.submitList(it)
                },

            presenter.effects()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    when (it) {
                        is Effect.GoalConfirmUndone -> postSnackbar(eventRelay, it.goalId)
                        is Effect.GoalDeleteConfirm -> AlertDialog.Builder(requireContext())
                            .setTitle(getString(R.string.delete_goal))
                            .setPositiveButton("Delete") { dialog, which ->
                                eventRelay.accept(Event.GoalDelete(it.goalId))
                            }.setNegativeButton("Cancel") { dialog, which ->
                                // No-op
                            }
                            .show()

                        is GoalsPresenter.Effect.GoalShow -> {
                            GoalDialogFragment.newInstance(userId, goalId = it.goalId)
                                .show(fragmentManager, "goal dialog tag")
                        }
                    }
                }
        )
    }

    private fun postSnackbar(
        goalCompleteRelay: PublishRelay<Event>,
        goalId: String
    ) {
        Snackbar.make(
            view!!,
            getString(R.string.goal_marked_undone),
            Snackbar.LENGTH_SHORT
        ).setAction(getString(R.string.undo)) {
            goalCompleteRelay.accept(
                Event.GoalMarkedDone(
                    goalId
                )
            )
        }
            .show()
    }

    companion object {
        const val userIdKey = "userIdKey"

        fun newInstance(userId: String): GoalsFragment {
            return GoalsFragment().apply {
                arguments = Bundle().apply {
                    putString(userIdKey, userId)
                }
            }
        }
    }

}