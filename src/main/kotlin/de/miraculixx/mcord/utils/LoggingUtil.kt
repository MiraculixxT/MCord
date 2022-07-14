package de.miraculixx.mcord.utils

import java.time.LocalDate
import java.time.LocalTime

fun String.log(color: Color = Color.WHITE) {
    printToConsole(
        this, "\u001B[${color.code}m"
    )
}

fun String.error() {
    printToConsole(this, "\u001b[${Color.RED.code}m")
}

private fun printToConsole(input: String, color: String) {
    val date = LocalDate.now()
    val time = LocalTime.now()
    println("$color[$date ${prettyNumber(time.hour)}:${prettyNumber(time.minute)}:${prettyNumber(time.second)}] $input\u001B[0m")
}

private fun prettyNumber(int: Int): String {
    return if (int <= 9) "0$int" else int.toString()
}

enum class Color(val code: Byte) {
    RED(31),
    GREEN(32),
    YELLOW(33),
    BLUE(34),
    MAGENTA(35),
    CYAN(36),
    GRAY(90),
    WHITE(97)
}