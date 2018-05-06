package de.gutenko.roguelike.habittracker.di

import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import de.gutenko.roguelike.habittracker.data.goals.FirebaseGoalRepository
import de.gutenko.roguelike.habittracker.data.goals.GoalRepository
import de.gutenko.roguelike.habittracker.data.goals.MemoryGoalRepository
import de.gutenko.roguelike.habittracker.data.habits.*
import de.gutenko.roguelike.habittracker.ui.FirebasePlayerRepository
import de.gutenko.roguelike.habittracker.ui.PlayerRepository
import javax.inject.Singleton

interface AppModule {
    fun habitRepository(): HabitRepository
    fun habitCompletionRepository(): HabitCompletionRepository
    fun goalRepository(): GoalRepository
    fun playerRepository(): PlayerRepository
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

    @Singleton
    @Provides
    override fun playerRepository(): PlayerRepository {
        return TODO()
    }
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
    override fun habitCompletionRepository(): HabitCompletionRepository {
        val reference = FirebaseDatabase.getInstance().reference.root

        return FirebaseHabitCompletionRepository(reference.child("users"))
    }

    @Singleton
    @Provides
    override fun goalRepository(): GoalRepository {
        val root = FirebaseDatabase.getInstance().reference.root

        return FirebaseGoalRepository(root.root.child("users"))
    }

    @Singleton
    @Provides
    override fun playerRepository(): PlayerRepository =
        FirebasePlayerRepository(FirebaseDatabase.getInstance().reference.root.child("users"))
}