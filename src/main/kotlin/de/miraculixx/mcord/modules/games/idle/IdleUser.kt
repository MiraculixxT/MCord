package de.miraculixx.mcord.modules.games.idle

import de.miraculixx.mcord.modules.games.idle.data.BuildingData
import de.miraculixx.mcord.modules.games.idle.data.Upgrades

class IdleUser(val id: Long) {

    // A List of all Upgrades, the User already bought.
    // The number represent how many Upgrades already bought from this type.
    private val upgrades = HashMap<Upgrades, Int>()

    // A List of all Buildings, the User bought.
    // The number represents the Building Slot ID.
    private val buildings = HashMap<Int, BuildingData>()

    /**
     * @param amount How many upgrades should add to the User
     * @param upgrade What Upgrade is added
     */
    fun addUpgrade(upgrade: Upgrades, amount: Int) {
        val current = (upgrades[upgrade] ?: 0).plus(amount)
        upgrades[upgrade] = if (current > upgrade.maxAmount)
            upgrade.maxAmount else current
    }

    // Check how many Upgrades from this Type are left to upgrade
    fun upgradesLeft(upgrade: Upgrades): Int {
        return upgrade.maxAmount.minus(upgrades[upgrade] ?: 0)
    }
}