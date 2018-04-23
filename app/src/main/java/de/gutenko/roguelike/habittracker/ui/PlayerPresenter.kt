package de.gutenko.roguelike.habittracker.ui

import de.gutenko.roguelike.habittracker.data.player.Attribute
import io.reactivex.Observable

class PlayerPresenter(
    private val playerRepository: PlayerRepository,
    private val userId: String
) {
    data class PlayerViewState(
        val playerName: String,
        val attack: Attribute,
        val agility: Attribute,
        val endurance: Attribute,
        val intelligence: Attribute
    )

    fun viewStates(): Observable<PlayerViewState> {
        return playerRepository.observePlayer(userId).map {
            PlayerViewState(
                it.userName,
                Attribute.of(it.attack),
                Attribute.of(it.agility),
                Attribute.of(it.endurance),
                Attribute.of(it.intelligence)
            )
        }
    }
}