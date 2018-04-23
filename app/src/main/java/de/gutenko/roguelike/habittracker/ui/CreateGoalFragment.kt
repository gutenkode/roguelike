package de.gutenko.roguelike.habittracker.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatSeekBar
import android.view.LayoutInflater
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap
import de.gutenko.roguelike.R
import de.gutenko.roguelike.habittracker.data.goals.GoalRepository
import de.gutenko.roguelike.habittracker.data.player.PlayerUpdate
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@Subcomponent
interface CreateGoalFragmentSubcomponent : AndroidInjector<CreateGoalFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<CreateGoalFragment>()
}

@Module(subcomponents = [CreateGoalFragmentSubcomponent::class])
abstract class CreateGoalFragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(CreateGoalFragment::class)
    abstract fun bindGoalFragmentInjectorFactory(builder: CreateGoalFragmentSubcomponent.Builder): AndroidInjector.Factory<out Fragment>
}


class CreateGoalFragment : DialogFragment() {
    @Inject
    lateinit var goalRepository: GoalRepository

    lateinit var userId: String

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        userId = arguments!!.getString(userIdKey)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.fragment_create_goal, null, false)

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setTitle(getString(R.string.create_goal))
            .setPositiveButton(getString(R.string.create)) { dialog, which ->
                val goalName = view.findViewById<TextInputEditText>(R.id.goalName).text.toString()
                val attackAdd = view.findViewById<AppCompatSeekBar>(R.id.attackAdd)
                val agilityAdd = view.findViewById<AppCompatSeekBar>(R.id.agilityAdd)
                val enduranceAdd = view.findViewById<AppCompatSeekBar>(R.id.enduranceAdd)
                val intelligenceAdd = view.findViewById<AppCompatSeekBar>(R.id.intelligenceAdd)

                compositeDisposable.add(
                    goalRepository.addGoal(
                        userId,
                        goalName,
                        PlayerUpdate(
                            attackUpdate = attackAdd.progress,
                            agilityUpdate = agilityAdd.progress,
                            enduranceUpdate = enduranceAdd.progress,
                            intelligenceUpdate = intelligenceAdd.progress
                        )
                    ).subscribe()
                )
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }

    companion object {
        const val tag = "CreateGoalFragmentTag"
        private val userIdKey = "userIdKey"

        fun newInstance(userId: String): CreateGoalFragment {
            return CreateGoalFragment().apply {
                arguments = Bundle().apply {
                    putString(userIdKey, userId)
                }
            }
        }
    }
}