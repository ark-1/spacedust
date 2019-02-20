package me.arkadybazhanov.spacedust

import android.content.res.*
import android.graphics.*
import me.arkadybazhanov.spacedust.LevelSnapshot.CellSnapshot
import me.arkadybazhanov.spacedust.R.drawable.*
import me.arkadybazhanov.spacedust.core.*
import me.arkadybazhanov.spacedust.core.CellType.*

class LevelDrawer(private val resources: Resources) {
    fun drawLevel(level: LevelSnapshot, canvas: Canvas) {
        val cellWidth = canvas.width / level.w

        for ((x, y, cell) in level.withCoordinates()) {
            val bm = cell.toBitmap()
            val src = Rect(0, 0, bm.width, bm.height)
            val dst = Rect(x * cellWidth, y * cellWidth, (x + 1) * cellWidth, (y + 1) * cellWidth)
            canvas.drawBitmap(bm, src, dst, null)
        }
    }

    private val bitmaps = object {
        private val cache = mutableMapOf<Int, Bitmap>()

        operator fun get(id: Int) = cache[id] ?: (
            BitmapFactory.decodeResource(resources, id) ?: error("Could not decode cell bitmap")
        ).also { cache[id] = it }
    }

    private fun CellSnapshot.toBitmap(): Bitmap {
        val id = when (characterType) {

            null -> when (type) {
                AIR -> blue
                STONE -> white
            }

            Player::class -> player
            else -> monster
        }
        return bitmaps[id]
    }

    fun getCell(level: LevelSnapshot, canvasWidth: Int, x: Float, y: Float): Position {
        val cellWidth = canvasWidth / level.w
        return Position((x / cellWidth).toInt(), (y / cellWidth).toInt())
    }
}
