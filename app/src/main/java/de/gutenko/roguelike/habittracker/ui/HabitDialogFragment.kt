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
import de.gutenko.roguelike.habittracker.data.habits.HabitRepository
import io.reactivex.disposables.CompositeDisposable
import java.text.SimpleDateFormat
import java.util.Date
import javax.inject.Inject

@Subcomponent
interface HabitDialogFragmentSubcomponent : AndroidInjector<HabitDialogFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<HabitDialogFragment>()
}

@Module(subcomponents = [HabitDialogFragmentSubcomponent::class])
abstract class HabitDialogFragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(HabitDialogFragment::class)
    abstract fun bindHabitDialogFragmentInjectorFactory(builder: HabitDialogFragmentSubcomponent.Builder): AndroidInjector.Factory<out Fragment>
}


class HabitDialogFragment : DialogFragment() {
    @Inject
    lateinit var habitRepository: HabitRepository

    private lateinit var userId: String
    private lateinit var habitId: String
    private lateinit var habitDialogView: View

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        userId = arguments!!.getString(userIdKey)
        habitId = arguments!!.getString(habitIdKey)
    }

    override fun onStart() {
        super.onStart()
        compositeDisposable.addAll(
            habitRepository.getHabit(userId, habitId)
                .subscribe { habit ->
                    dialog.setTitle(habit.name)
                    habitDialogView.apply {
                        val created = SimpleDateFormat.getDateInstance()
                            .format(Date(habit.createdTime))

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
                                habit.playerUpdate.attackUpdate

                        agilityBar.progress =
                                habit.playerUpdate.agilityUpdate

                        enduranceBar.progress =
                                habit.playerUpdate.enduranceUpdate

                        intelligenceBar.progress =
                                habit.playerUpdate.intelligenceUpdate
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
        private const val habitIdKey = "habitIdKey"

        fun newInstance(userId: String, habitId: String) = HabitDialogFragment().apply {
            arguments = Bundle().apply {
                putString(userIdKey, userId)
                putString(habitIdKey, habitId)
            }
        }
    }
}