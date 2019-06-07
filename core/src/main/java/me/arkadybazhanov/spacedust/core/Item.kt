package me.arkadybazhanov.spacedust.core

import kotlinx.serialization.Serializable
import me.arkadybazhanov.spacedust.core.WeaponType.*

interface Item : Savable {
    val name: String

    fun process(owner: Character) {
        owner.inventory += this
    }
}

enum class WeaponType {
    STICK, BRANCH, HAMMER
}

class Weapon(val game: Game, val damage: Int, val type: WeaponType, override val saveId: Int = game.getNextId()) : Item {
    override fun save() = SavedWeapon(saveId = saveId, game = game.saveId, damage = damage, type = type)

    override val name = when (type) {
        STICK -> "stick"
        BRANCH -> "branch"
        HAMMER -> "hammer"
    }

    override val refs = listOf<Savable>()

    @Serializable
    class SavedWeapon(
        override val saveId: Int,
        val game: Int,
        val damage: Int,
        val type: WeaponType
    ) : SavedStrong<Weapon> {
        override val refs = listOf<Int>()

        override fun initial(pool: Pool): Weapon = Weapon(pool.load(game), damage, type, saveId)
    }
}

class HealKit(val game: Game, val hp: Int, override val saveId: Int = game.getNextId()) : Item {
    override fun save() = SavedHealKit(saveId = saveId, game = game.saveId, hp = hp)
    override val name = "heal kit"

    override val refs = listOf<Savable>()

    @Serializable
    class SavedHealKit(
        override val saveId: Int,
        val game: Int,
        val hp: Int
    ) : SavedStrong<HealKit> {
        override val refs = listOf<Int>()

        override fun initial(pool: Pool): HealKit = HealKit(pool.load(game), hp, saveId)
    }

    override fun process(owner: Character) {
        owner.hp = minOf(owner.maxHp, owner.hp + hp)
    }
}
