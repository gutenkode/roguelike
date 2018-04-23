package de.gutenko.roguelike.habittracker.data.habits

import de.gutenko.roguelike.habittracker.data.player.PlayerUpdate

data class Habit(
    val id: String,
    val userId: String,
    val name: String,
    val playerUpdate: PlayerUpdate,
    val timeOfDay: TimeOfDay?,
    val createdTime: Long
)

data class HabitData(val name: String, val playerUpdate: PlayerUpdate, val timeOfDay: TimeOfDay?)

data class TimeOfDay(val hours: Int, val minutes: Int) {
    init {
        require(hours in 0..23)
        require(minutes in 0..59)
    }
}
