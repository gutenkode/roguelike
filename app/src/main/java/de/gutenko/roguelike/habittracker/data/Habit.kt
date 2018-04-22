package de.gutenko.roguelike.habittracker.data

import org.joda.time.LocalDate

data class Habit(
    val id: String,
    val userId: String,
    val name: String,
    val playerUpdate: PlayerUpdate,
    val timeOfDay: TimeOfDay?,
    val createdTime: Long
)

data class HabitData(val name: String, val playerUpdate: PlayerUpdate, val timeOfDay: TimeOfDay?)

data class TimeOfDay(val hours: Int, val minutes: Int)

data class HabitCompletion(val userId: String, val habitId: String, val day: LocalDate)