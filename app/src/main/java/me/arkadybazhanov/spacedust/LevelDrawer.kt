package me.arkadybazhanov.spacedust

import android.content.res.Resources
import android.graphics.*
import me.arkadybazhanov.spacedust.LevelSnapshot.CellSnapshot
import me.arkadybazhanov.spacedust.R.drawable.*
import me.arkadybazhanov.spacedust.core.*
import me.arkadybazhanov.spacedust.core.CellType.*
import me.arkadybazhanov.spacedust.core.Monster.MonsterType.*
import me.arkadybazhanov.spacedust.core.WeaponType.*

class LevelDrawer(private val resources: Resources) {
    private fun square(left: Float, top: Float, width: Float) = RectF(
        left,
        top,
        left + width,
        top + width
    )

    fun drawLevel(level: LevelSnapshot, canvas: Canvas, camera: Camera) {
        canvas.save()
        canvas.scale(camera.scaleFactor, camera.scaleFactor)
        canvas.translate(camera.shiftX.value, camera.shiftY.value)

        for ((x, y, cell) in level.withCoordinates()) {
            if (cell.discovered) {
                val (bms, bm2) = cell.toBitmaps()
                val dst = square(x.cell, y.cell, 1.cell)
                for (bm in bms) {
                    val src = Rect(0, 0, bm.width, bm.height)
                    canvas.drawBitmap(bm, src, dst, null)
                }
                if (cell.visible) {
                    bm2?.let {
                        val final = if (cell.characterType != null
                                && cell.characterType == Player::class
                                && level.playerSnapshot.turn == Player.Turn.LEFT) {
                            val matrix = Matrix()
                            matrix.postScale(-1f, 1f)
                            Bitmap.createBitmap(bm2, 0, 0, bm2.width, bm2.height, matrix, true)
                        } else {
                            bm2
                        }
                        val src = Rect(0, 0, final.width, final.height)
                        canvas.drawBitmap(final, src, dst, null)
                    }
                } else {
                    canvas.drawRect(dst, Paint().apply { color = Color.argb(100, 0, 0, 0) })
                }
            }
        }


        val gridPaint = Paint().apply { alpha = 20 }

        for (x in 1 until level.w) {
            canvas.drawLine(x.cell, 0f, x.cell, level.h.cell, gridPaint)
        }

        for (y in 1 until level.h) {
            canvas.drawLine(0f, y.cell, level.h.cell, y.cell, gridPaint)
        }

        canvas.restore()
    }

    private val bitmaps = object {
        private val cache = mutableMapOf<Int, Bitmap>()

        operator fun get(id: Int) = cache[id] ?: (
                BitmapFactory.decodeResource(resources, id) ?: error("Could not decode cell bitmap")
                ).also { cache[id] = it }
    }

    private fun CellSnapshot.toBitmaps(): Pair<Iterable<Bitmap>, Bitmap?> {
        val cellId = when (type) {
            AIR -> air
            STONE -> stone
            DOWNSTAIRS -> downstairs
            UPSTAIRS -> upstairs
        }
        val backgroundId = when (type) {
            UPSTAIRS -> air
            else -> null
        }
        val id = when (characterType) {
            Player::class -> player
            Monster::class -> when (monsterType) {
                BASIC -> monster
                UPSET -> upset
                ROBOT -> robot
                else -> null
            }
            else -> null
        }
        val itemId = when (itemType) {
            Weapon::class -> when (weaponType) {
                STICK -> stick
                BRANCH -> branch
                HAMMER -> hammer
                else -> null
            }
            HealKit::class -> heal
            else -> null
        }
        return mutableListOf<Int>().apply {
            if (backgroundId != null) add(backgroundId)
            add(cellId)
        }.map { bitmaps[it] } to (id?.let { bitmaps[it] } ?: itemId?.let { bitmaps[it] })
    }

    fun getCell(level: LevelSnapshot, camera: Camera, x: Float, y: Float): Position? {
        val position = Position(
            x = ((x / camera.scaleFactor - camera.shiftX.value) / CELL_WIDTH).toInt(),
            y = ((y / camera.scaleFactor - camera.shiftY.value) / CELL_WIDTH).toInt()
        )
        return position.takeIf { it.isValid(level.w, level.h) }
    }

    companion object {
        const val CELL_WIDTH = 64f
    }
}

val Int.cell get() = this * LevelDrawer.CELL_WIDTH
