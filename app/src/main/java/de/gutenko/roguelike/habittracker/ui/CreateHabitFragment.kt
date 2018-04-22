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
import androidx.core.widget.toast
import dagger.Binds
import dagger.Module
import dagger.Subcomponent
import dagger.android.AndroidInjector
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.FragmentKey
import dagger.multibindings.IntoMap
import de.gutenko.roguelike.R
import de.gutenko.roguelike.habittracker.data.HabitData
import de.gutenko.roguelike.habittracker.data.HabitRepository
import de.gutenko.roguelike.habittracker.data.PlayerUpdate
import de.gutenko.roguelike.habittracker.data.TimeOfDay
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
            .setPositiveButton("Create") { dialog, _ ->
                val habitName = view.findViewById<TextInputEditText>(R.id.habitName)
                val attackAdd = view.findViewById<AppCompatSeekBar>(R.id.attackAdd)
                val agilityAdd = view.findViewById<AppCompatSeekBar>(R.id.agilityAdd)
                val enduranceAdd = view.findViewById<AppCompatSeekBar>(R.id.enduranceAdd)
                val intelligenceAdd = view.findViewById<AppCompatSeekBar>(R.id.intelligenceAdd)
                val timePicker = view.findViewById<TimePicker>(R.id.habitTimePicker)

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
                        TimeOfDay(timePicker.currentHour, timePicker.currentMinute)
                    )
                )
            }
            .setNegativeButton("Cancel") { _, _ ->
                requireContext().toast("Canceled")
            }
            .setCancelable(true)
            .setTitle("Create Habit")
            .create()
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