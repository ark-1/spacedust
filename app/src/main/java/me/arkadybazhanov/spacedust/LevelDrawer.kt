package me.arkadybazhanov.spacedust

import android.content.res.Resources
import android.graphics.*
import me.arkadybazhanov.spacedust.LevelSnapshot.CellSnapshot
import me.arkadybazhanov.spacedust.R.drawable.*
import me.arkadybazhanov.spacedust.core.CellType.*
import me.arkadybazhanov.spacedust.core.Position
import kotlin.math.*

class LevelDrawer(private val resources: Resources, width: Int) {
    var scaleFactor = 412.0178f / width

    val shiftX = AtomicFloat(Player.VISIBILITY_RANGE.cell)
    val shiftY = AtomicFloat(Player.VISIBILITY_RANGE.cell)

    private fun square(left: Float, top: Float, width: Float) = RectF(
        left,
        top,
        left + width,
        top + width
    )

    fun drawLevel(level: LevelSnapshot, canvas: Canvas) {
        canvas.save()
        canvas.scale(scaleFactor, scaleFactor)
        canvas.translate(shiftX.value, shiftY.value)

        for ((x, y, cell) in level.withCoordinates()) {
            if (cell.discovered) {
                val (bm, bm2) = cell.toBitmaps()
                val src = Rect(0, 0, bm.width, bm.height)
                val dst = square(x.cell, y.cell, 1.cell)
                canvas.drawBitmap(bm, src, dst, null)
                if (max(abs(x - level.playerPosition.x), abs(y - level.playerPosition.y)) <= Player.VISIBILITY_RANGE) {
                    bm2?.let { canvas.drawBitmap(it, src, dst, null) }
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

    private fun CellSnapshot.toBitmaps(): Pair<Bitmap, Bitmap?> {
        val cellId = when (type) {
            AIR -> air
            STONE -> stone
        }
        val id = when (characterType) {
            null -> null
            Player::class -> player
            else -> monster
        }
        return bitmaps[cellId] to id?.let { bitmaps[it] }
    }

    fun getCell(level: LevelSnapshot, x: Float, y: Float): Position? {
        val position = Position(
            x = ((x / scaleFactor - shiftX.value) / CELL_WIDTH).toInt(),
            y = ((y / scaleFactor - shiftY.value) / CELL_WIDTH).toInt()
        )
        return position.takeIf { it.isValid(level.w, level.h) }
    }

    companion object {
        const val CELL_WIDTH = 64f
    }
}

val Int.cell get() = this * LevelDrawer.CELL_WIDTH
