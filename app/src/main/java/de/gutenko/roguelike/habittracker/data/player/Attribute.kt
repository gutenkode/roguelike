package de.gutenko.roguelike.habittracker.data.player

data class Attribute(val level: Int, val progress: Int) {
    init {
        require(progress in 0..99)
        require(level >= 0)
    }

    companion object {
        fun of(value: Int): Attribute {
            return Attribute(value / 100, value % 100)
        }
    }
}