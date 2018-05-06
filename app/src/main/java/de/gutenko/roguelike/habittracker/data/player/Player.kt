package de.gutenko.roguelike.habittracker.data.player

import com.google.firebase.database.DataSnapshot
import de.gutenko.roguelike.habittracker.data.habits.valueExpected
import java.io.Serializable

data class Player(
    val userId: String,
    val userName: String
) : Serializable

data class GamePlayer(
    val attack: Int,
    val agility: Int,
    val endurance: Int,
    val intelligence: Int
) : Serializable

fun DataSnapshot.toPlayer(): Player = Player(
    valueExpected("userId"),
    valueExpected("userName")
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
