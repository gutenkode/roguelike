package de.gutenko.roguelike.habittracker.ui

import de.gutenko.roguelike.habittracker.data.player.Player
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface PlayerRepository {
    fun hasPlayer(userId: String): Single<Boolean>
    fun observePlayer(userId: String): Observable<Player>
    fun addPlayer(player: Player): Completable
}