package de.gutenko.roguelike.habittracker.data.habits

import com.google.firebase.database.DataSnapshot

inline fun <reified T> DataSnapshot.valueExpected(key: String): T {
    return valueFor<T>(key) ?: throw RuntimeException("Couldn't find $key in $this")
}

inline fun <reified T> DataSnapshot.valueFor(key: String): T? {
    return child(key).getValue(T::class.java)
}