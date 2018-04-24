package de.gutenko.roguelike.habittracker.data.habits

import org.joda.time.LocalDate

data class HabitCompletion(val userId: String, val habitId: String, val day: LocalDate)