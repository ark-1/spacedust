package me.arkadybazhanov.spacedust.core

import kotlinx.serialization.Serializable

interface Item : Savable {
    val name: String
}

class Weapon(val game: Game, val damage: Int, override val saveId: Int = game.getNextId()) : Item {
    override fun save() = SavedWeapon(saveId = saveId, game = game.saveId, damage = damage)
    override val name = "waepon"

    override val refs = listOf<Savable>()

    @Serializable
    class SavedWeapon(
        override val saveId: Int,
        val game: Int,
        val damage: Int
    ) : SavedStrong<Weapon> {
        override val refs = listOf<Int>()

        override fun initial(pool: Pool): Weapon = Weapon(pool.load(game), damage, saveId)
    }
}
