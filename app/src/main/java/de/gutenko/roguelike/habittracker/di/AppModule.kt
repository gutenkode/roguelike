package de.gutenko.roguelike.habittracker.di

import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import de.gutenko.roguelike.habittracker.data.goals.FirebaseGoalRepository
import de.gutenko.roguelike.habittracker.data.goals.GoalRepository
import de.gutenko.roguelike.habittracker.data.goals.MemoryGoalRepository
import de.gutenko.roguelike.habittracker.data.habits.*
import javax.inject.Singleton

interface AppModule {
    fun habitRepository(): HabitRepository

    fun habitCompletionRepository(): HabitCompletionRepository

    fun goalRepository(): GoalRepository
}

@Singleton
@Module
object MemoryAppModule : AppModule {
    @Singleton
    @Provides
    override fun habitRepository(): HabitRepository {
        return MemoryHabitRepository()
    }

    @Singleton
    @Provides
    override fun habitCompletionRepository(): HabitCompletionRepository {
        return MemoryHabitCompletionRepository()
    }

    @Singleton
    @Provides
    override fun goalRepository(): GoalRepository = MemoryGoalRepository()
}

@Singleton
@Module
object FirebaseAppModule : AppModule {
    @Singleton
    @Provides
    override fun habitRepository(): HabitRepository =
        FirebaseHabitRepository(
            FirebaseDatabase.getInstance()
        )

    @Singleton
    @Provides
    override fun habitCompletionRepository(): HabitCompletionRepository =
        FirebaseHabitCompletionRepository(FirebaseDatabase.getInstance().reference.root.child("completions"))

    @Singleton
    @Provides
    override fun goalRepository(): GoalRepository =
        FirebaseGoalRepository(FirebaseDatabase.getInstance())
}