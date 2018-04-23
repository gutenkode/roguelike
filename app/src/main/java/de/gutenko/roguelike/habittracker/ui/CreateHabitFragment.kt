package de.gutenko.roguelike.habittracker.ui

import android.app.Dialog
import android.os.Bundle
import android.support.design.widget.TextInputEditText
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.support.v7.app.AlertDialog
import android.support.v7.widget.AppCompatSeekBar
import android.view.LayoutInflater
import android.widget.TimePicker
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap
import de.gutenko.roguelike.R
import de.gutenko.roguelike.habittracker.data.habits.HabitData
import de.gutenko.roguelike.habittracker.data.habits.HabitRepository
import de.gutenko.roguelike.habittracker.data.habits.TimeOfDay
import de.gutenko.roguelike.habittracker.data.player.PlayerUpdate
import io.reactivex.disposables.CompositeDisposable
import javax.inject.Inject

@Subcomponent
interface CreateHabitFragmentSubcomponent : AndroidInjector<CreateHabitFragment> {
    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<CreateHabitFragment>()
}

@Module(subcomponents = [CreateHabitFragmentSubcomponent::class])
abstract class CreateHabitFragmentModule {
    @Binds
    @IntoMap
    @FragmentKey(CreateHabitFragment::class)
    abstract fun bindCreateHabitFragmentInjectorFactory(builder: CreateHabitFragmentSubcomponent.Builder): AndroidInjector.Factory<out Fragment>
}

class CreateHabitFragment : DialogFragment() {
    @Inject
    lateinit var habitRepository: HabitRepository

    private lateinit var userId: String

    private val compositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)

        userId = arguments!!.getString(userIdKey)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.fragment_create_habit, null, false)

        return AlertDialog.Builder(requireContext())
            .setView(view)
            .setPositiveButton(getString(R.string.create)) { dialog, _ ->
                val habitName = view.findViewById<TextInputEditText>(R.id.habitName)
                val attackAdd = view.findViewById<AppCompatSeekBar>(R.id.attackAdd)
                val agilityAdd = view.findViewById<AppCompatSeekBar>(R.id.agilityAdd)
                val enduranceAdd = view.findViewById<AppCompatSeekBar>(R.id.enduranceAdd)
                val intelligenceAdd = view.findViewById<AppCompatSeekBar>(R.id.intelligenceAdd)
                val timePicker = view.findViewById<TimePicker>(R.id.habitTimePicker)

                compositeDisposable.add(
                    habitRepository.addHabit(
                        userId,
                        HabitData(
                            habitName.text.toString(),
                            PlayerUpdate(
                                attackUpdate = attackAdd.progress,
                                agilityUpdate = agilityAdd.progress,
                                enduranceUpdate = enduranceAdd.progress,
                                intelligenceUpdate = intelligenceAdd.progress
                            ),
                            TimeOfDay(
                                timePicker.currentHour,
                                timePicker.currentMinute
                            )
                        )
                    ).subscribe()
                )
            }
            .setNegativeButton(getString(R.string.cancel)) { _, _ -> /*no-op */ }
            .setCancelable(true)
            .setTitle(getString(R.string.create_habit))
            .create()
    }

    override fun onDestroy() {
        super.onDestroy()

        compositeDisposable.clear()
    }

    companion object {
        const val tag = "CreateHabitFragmentSubcomponent"
        private val userIdKey = "userIdKey"

        fun newInstance(userId: String): CreateHabitFragment {
            return CreateHabitFragment().apply {
                arguments = Bundle().apply {
                    putString(userIdKey, userId)
                }
            }
        }
    }
}