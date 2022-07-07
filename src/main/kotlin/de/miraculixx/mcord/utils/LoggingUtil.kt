package de.miraculixx.mcord.utils

import java.time.LocalDate
import java.time.LocalTime

fun String.log() {
    val date = LocalDate.now()
    val time = LocalTime.now()
    println("[$date ${time.hour}:${time.minute}:${time.second}] $this")
}