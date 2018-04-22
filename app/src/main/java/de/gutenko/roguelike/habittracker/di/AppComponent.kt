package de.gutenko.roguelike.habittracker.di

import dagger.Component
import dagger.android.AndroidInjectionModule
import de.gutenko.roguelike.habittracker.RoguelikeApplication
import de.gutenko.roguelike.habittracker.data.HabitRepository
import de.gutenko.roguelike.habittracker.ui.CreateHabitFragmentModule
import de.gutenko.roguelike.habittracker.ui.HabitFragmentModule
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, MemoryAppModule::class, HabitFragmentModule::class, CreateHabitFragmentModule::class])
interface AppComponent {
    fun habitRepository(): HabitRepository
    fun inject(application: RoguelikeApplication)
}
