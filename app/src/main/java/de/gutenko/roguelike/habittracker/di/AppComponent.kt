package de.gutenko.roguelike.habittracker.di

import dagger.Component
import dagger.android.AndroidInjectionModule
import de.gutenko.roguelike.habittracker.RoguelikeApplication
import de.gutenko.roguelike.habittracker.data.habits.HabitRepository
import de.gutenko.roguelike.habittracker.ui.*
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AndroidInjectionModule::class,
        FirebaseAppModule::class,
        HabitFragmentModule::class,
        CreateHabitFragmentModule::class,
        GoalsFragmentModule::class,
        CreateGoalFragmentModule::class,
        StatsActivityModule::class
    ]
)
interface AppComponent {
    fun habitRepository(): HabitRepository
    fun inject(application: RoguelikeApplication)
}
