package de.gutenko.roguelike.habittracker.di

import dagger.Module
import dagger.Provides
import de.gutenko.roguelike.habittracker.data.HabitCompletionRepository
import de.gutenko.roguelike.habittracker.data.HabitRepository
import de.gutenko.roguelike.habittracker.data.MemoryHabitCompletionRepository
import de.gutenko.roguelike.habittracker.data.MemoryHabitRepository
import javax.inject.Singleton

@Singleton
@Module
object MemoryAppModule {
    @Singleton
    @Provides
    fun habitRepository(): HabitRepository {
        return MemoryHabitRepository()
    }

    @Singleton
    @Provides
    fun habitCompletionRepository(): HabitCompletionRepository {
        return MemoryHabitCompletionRepository()
    }
}
