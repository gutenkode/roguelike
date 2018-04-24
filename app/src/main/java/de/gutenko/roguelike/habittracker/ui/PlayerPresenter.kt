package de.gutenko.roguelike.habittracker.ui

import de.gutenko.roguelike.habittracker.data.player.Attribute
import io.reactivex.Observable

class PlayerPresenter(
    private val playerRepository: PlayerRepository,
    private val userId: String
) {
    data class PlayerViewState(
        val playerName: String,
        val attributes: List<Attribute>
    )

    data class AttributeViewState(val attribute: Attribute, val name: String)

    fun viewStates(): Observable<PlayerViewState> {
        return playerRepository.observePlayer(userId).map {
            PlayerViewState(
                it.userName,
                listOf(
                    Attribute.of(it.attack),
                    Attribute.of(it.agility),
                    Attribute.of(it.endurance),
                    Attribute.of(it.intelligence)
                )
            )
        }
    }
}