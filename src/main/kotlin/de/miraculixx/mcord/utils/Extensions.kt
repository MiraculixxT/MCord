package de.miraculixx.mcord.utils

fun String.toError(): String {
    return "```diff\n- $this```"
}

fun String.toSuccess(): String {
    return "```diff\n+ $this```"
}