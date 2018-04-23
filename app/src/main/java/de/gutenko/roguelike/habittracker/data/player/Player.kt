package de.gutenko.roguelike.habittracker.data.player

data class Player(
    val userId: String,
    val attack: Attribute,
    val agility: Attribute,
    val endurance: Attribute,
    val intelligence: Attribute
)

data class PlayerUpdate(
    val attackUpdate: Int,
    val agilityUpdate: Int,
    val enduranceUpdate: Int,
    val intelligenceUpdate: Int
)