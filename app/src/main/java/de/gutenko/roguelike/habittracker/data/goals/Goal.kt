package de.gutenko.roguelike.habittracker.data.goals

import de.gutenko.roguelike.habittracker.data.player.PlayerUpdate
import org.joda.time.LocalDateTime

data class Goal(
    val id: String,
    val userId: String,
    val added: LocalDateTime,
    val name: String,
    val playerUpdate: PlayerUpdate,
    val completedOn: LocalDateTime?
)
