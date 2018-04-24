package de.gutenko.roguelike.habittracker.ui

fun <T> List<T>.replaceAt(index: Int, with: T): List<T> {
    val list = this.toMutableList()
    list[index] = with
    return list.toList()
}
