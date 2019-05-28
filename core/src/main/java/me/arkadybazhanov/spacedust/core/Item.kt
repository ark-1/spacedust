package me.arkadybazhanov.spacedust.core

import kotlinx.serialization.Serializable

interface Item : Savable {
}

class Weapon(val damage: Int,
             override val saveId: Int = Game.getNextId()
) : Item {
    override fun save() = SavedWeapon(saveId, damage)

    override val refs = listOf<Savable>()

    @Serializable
    class SavedWeapon(override val saveId: Int,
                      val damage: Int
    ) : SavedStrong<Weapon> {
        override val refs = listOf<Int>()

        override fun initial(pool: Pool): Weapon = Weapon(damage, saveId)

    }
}
