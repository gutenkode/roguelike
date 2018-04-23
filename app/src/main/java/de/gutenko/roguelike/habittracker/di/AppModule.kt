package de.gutenko.roguelike.habittracker.di

import com.androidhuman.rxfirebase2.database.data
import com.androidhuman.rxfirebase2.database.dataChanges
import com.androidhuman.rxfirebase2.database.rxSetValue
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import de.gutenko.roguelike.habittracker.data.goals.FirebaseGoalRepository
import de.gutenko.roguelike.habittracker.data.goals.GoalRepository
import de.gutenko.roguelike.habittracker.data.goals.MemoryGoalRepository
import de.gutenko.roguelike.habittracker.data.habits.*
import de.gutenko.roguelike.habittracker.data.player.Player
import de.gutenko.roguelike.habittracker.data.player.toPlayer
import de.gutenko.roguelike.habittracker.ui.PlayerRepository
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
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

        return FirebaseHabitCompletionRepository(reference)
    }

    @Singleton
    @Provides
    override fun goalRepository(): GoalRepository {
        val root = FirebaseDatabase.getInstance().reference.root

        return FirebaseGoalRepository(root)
    }

    @Singleton
    @Provides
    override fun playerRepository(): PlayerRepository {
        return object : PlayerRepository {
            override fun hasPlayer(userId: String): Single<Boolean> {
                return FirebaseDatabase.getInstance().reference
                    .child("users")
                    .child(userId)
                    .child("player")
                    .data().map {
                        it.exists()
                    }
            }

            override fun observePlayer(userId: String): Observable<Player> {
                return FirebaseDatabase.getInstance().reference.root
                    .child("users")
                    .child(userId)
                    .child("player")
                    .dataChanges()
                    .map { it.toPlayer() }
            }

            override fun addPlayer(player: Player): Completable {
                return FirebaseDatabase.getInstance().reference.child("users").child(player.userId)
                    .child("player")
                    .rxSetValue(player)
            }
        }
    }
}