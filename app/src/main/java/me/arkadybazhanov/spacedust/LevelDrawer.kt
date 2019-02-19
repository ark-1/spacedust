package me.arkadybazhanov.spacedust

import android.content.res.*
import android.graphics.*
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

    private val bitmapCache = mutableMapOf<Int, Bitmap>()

    private fun LevelSnapshot.CellSnapshot.toBitmap(): Bitmap {
        val id = if (characterType != null) when (characterType) {
            Player::class -> player
            else -> monster
        } else when (type) {
            AIR -> blue
            STONE -> white
        }
        return bitmapCache[id] ?: (
            BitmapFactory.decodeResource(resources, id) ?: error("Could not decode cell bitmap")
        ).also { bitmapCache[id] = it }
    }

    fun getCell(level: LevelSnapshot, canvasWidth: Int, x: Float, y: Float): Position {
        val cellWidth = canvasWidth / level.w
        return Position((x / cellWidth).toInt(), (y / cellWidth).toInt())
    }
}
