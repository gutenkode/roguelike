package de.gutenko.roguelike.habittracker.data

data class Player(
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