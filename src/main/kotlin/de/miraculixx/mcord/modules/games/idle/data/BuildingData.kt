package de.miraculixx.mcord.modules.games.idle.data

data class BuildingData(
    val type: Buildings,
    var amount: Int,
    val maxAmount: Int,
    val baseValuePerBuilding: Double
)