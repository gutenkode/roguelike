package de.gutenko.roguelike.habittracker.ui

import com.androidhuman.rxfirebase2.database.data
import com.androidhuman.rxfirebase2.database.dataChanges
import com.androidhuman.rxfirebase2.database.rxSetValue
import com.google.firebase.database.DatabaseReference
import de.gutenko.roguelike.habittracker.data.player.Player
import de.gutenko.roguelike.habittracker.data.player.toPlayer
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface PlayerRepository {
    fun hasPlayer(userId: String): Single<Boolean>
    fun observePlayer(userId: String): Observable<Player>
    fun addPlayer(player: Player): Completable
}

class FirebasePlayerRepository(private val users: DatabaseReference) : PlayerRepository {
    override fun hasPlayer(userId: String): Single<Boolean> =
        users
            .child(userId)
            .child("player")
            .data()
            .map { it.exists() }

    override fun observePlayer(userId: String): Observable<Player> =
        users
            .child(userId)
            .child("player")
            .dataChanges()
            .retry(0)
            .map {
                it.toPlayer()
            }

    override fun addPlayer(player: Player): Completable =
        users.child(player.userId).child("player").rxSetValue(player)
}