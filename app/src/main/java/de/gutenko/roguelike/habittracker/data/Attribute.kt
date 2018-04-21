package de.gutenko.roguelike.habittracker.data

data class Attribute(val level: Int, val progress: Int) {
    init {
        require(progress in 0..99)
        require(level >= 0)
    }
}