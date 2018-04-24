package de.gutenko.roguelike.habittracker.ui

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.widget.AppCompatSeekBar
import android.support.v7.widget.AppCompatTextView
import android.view.LayoutInflater
import android.view.View
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap
import de.gutenko.roguelike.R
import de.gutenko.roguelike.habittracker.data.goals.GoalRepository
import io.reactivex.disposables.CompositeDisposable
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@Subcomponent
interface GoalDialogFragmentSubcomponent : AndroidInjector<GoalDialogFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<GoalDialogFragment>()
}

@Module(subcomponents = [GoalDialogFragmentSubcomponent::class])
abstract class GoalDialogFragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(GoalDialogFragment::class)
    abstract fun bindGoalDialogFragmentInjectorFactory(builder: GoalDialogFragmentSubcomponent.Builder): AndroidInjector.Factory<out Fragment>
}


class GoalDialogFragment : DialogFragment() {
    @Inject
    lateinit var goalRepository: GoalRepository

    private lateinit var userId: String
    private lateinit var goalId: String
    private lateinit var habitDialogView: View

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        userId = arguments!!.getString(userIdKey)
        goalId = arguments!!.getString(goalIdKey)
    }

    override fun onStart() {
        super.onStart()
        compositeDisposable.addAll(
            goalRepository.getGoal(userId, goalId)
                .subscribe { goal ->
                    dialog.setTitle(goal.name)
                    habitDialogView.apply {
                        val created = SimpleDateFormat.getDateInstance()
                            .format(Date(goal.added.toDateTime().millis))

                        findViewById<AppCompatTextView>(R.id.habitAdded).text = "Added at $created"

                        val attackBar = findViewById<AppCompatSeekBar>(R.id.attackAdd).apply {
                            isEnabled = false
                        }

                        val agilityBar = findViewById<AppCompatSeekBar>(R.id.agilityAdd).apply {
                            isEnabled = false
                        }

                        val enduranceBar =
                            findViewById<AppCompatSeekBar>(R.id.enduranceAdd).apply {
                                isEnabled = false
                            }

                        val intelligenceBar =
                            findViewById<AppCompatSeekBar>(R.id.intelligenceAdd).apply {
                                isEnabled = false
                            }

                        attackBar.progress =
                                goal.playerUpdate.attackUpdate

                        agilityBar.progress =
                                goal.playerUpdate.agilityUpdate

                        enduranceBar.progress =
                                goal.playerUpdate.enduranceUpdate

                        intelligenceBar.progress =
                                goal.playerUpdate.intelligenceUpdate
                    }
                }
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        habitDialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_habit_detail, null, false)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.habit))
            .setView(habitDialogView)
            .create()

        return dialog
    }

    companion object {
        private const val userIdKey = "userIdKey"
        private const val goalIdKey = "habitIdKey"

        fun newInstance(userId: String, goalId: String) = GoalDialogFragment().apply {
            arguments = Bundle().apply {
                putString(userIdKey, userId)
                putString(goalIdKey, goalId)
            }
        }
    }
}