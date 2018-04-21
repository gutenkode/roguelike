package de.gutenko.roguelike.habittracker.data

data class Habit(val name: String, val playerUpdate: PlayerUpdate, val timeOfDay: TimeOfDay?)

data class TimeOfDay(val hours: Int, val minutes: Int)
