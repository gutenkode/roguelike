package de.gutenko.roguelike.habittracker.data.player

import com.google.firebase.database.DataSnapshot
import de.gutenko.roguelike.habittracker.data.habits.valueExpected
import java.io.Serializable

data class Player(
    val userId: String,
    val userName: String,
    val attack: Int,
    val agility: Int,
    val endurance: Int,
    val intelligence: Int
) : Serializable

fun DataSnapshot.toPlayer(): Player = Player(
    valueExpected("userId"),
    valueExpected("userName"),
    valueExpected("attack"),
    valueExpected("agility"),
    valueExpected("endurance"),
    valueExpected("intelligence")
)


data class PlayerUpdate(
    val attackUpdate: Int,
    val agilityUpdate: Int,
    val enduranceUpdate: Int,
    val intelligenceUpdate: Int
)

fun DataSnapshot.toPlayerUpdate(): PlayerUpdate {
    return PlayerUpdate(
        valueExpected("attackUpdate"),
        valueExpected("agilityUpdate"),
        valueExpected("enduranceUpdate"),
        valueExpected("intelligenceUpdate")
    )
}


fun updatePlayer(
    player: Player,
    update: PlayerUpdate
): Player {
    val newPlayer = player.copy(
        attack = player.attack + update.attackUpdate,
        agility = player.agility + update.agilityUpdate,
        endurance = player.endurance + update.enduranceUpdate,
        intelligence = player.intelligence + update.intelligenceUpdate
    )
    return newPlayer
}

fun unUpdatePlayer(
    player: Player,
    update: PlayerUpdate
): Player {
    val newPlayer = player.copy(
        attack = player.attack - update.attackUpdate,
        agility = player.agility - update.agilityUpdate,
        endurance = player.endurance - update.enduranceUpdate,
        intelligence = player.intelligence - update.intelligenceUpdate
    )
    return newPlayer
}
