package de.gutenko.roguelike.habittracker.ui

import android.support.annotation.DrawableRes
import de.gutenko.roguelike.habittracker.data.player.Attribute
import de.gutenko.roguelike.habittracker.data.player.Player
import de.gutenko.roguelike.habittracker.data.player.PlayerUpdate
import io.reactivex.Observable
import io.reactivex.functions.BiFunction

class PlayerPresenter(
    private val playerRepository: PlayerRepository,
    private val playerDataUseCase: PlayerDataUseCase,
    private val userId: String
) {
    data class PlayerViewState(
        val playerName: String,
        val attributes: List<Attribute>
    )

    data class AttributeViewState(
        val attribute: Attribute,
        val name: String,
        @DrawableRes val icon: Int
    )

    fun viewStates(): Observable<PlayerViewState> = Observable.combineLatest(
        playerRepository.observePlayer(userId),
        playerDataUseCase.playerData(userId),
        BiFunction<Player, PlayerUpdate, PlayerViewState> { player, data ->
            PlayerViewState(
                player.userName,
                listOf(
                    Attribute.of(data.attackUpdate),
                    Attribute.of(data.agilityUpdate),
                    Attribute.of(data.enduranceUpdate),
                    Attribute.of(data.intelligenceUpdate)
                )
            )
        }
    )
}