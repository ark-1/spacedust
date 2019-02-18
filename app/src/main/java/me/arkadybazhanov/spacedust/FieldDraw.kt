package me.arkadybazhanov.spacedust

import android.content.res.*
import android.graphics.*
import me.arkadybazhanov.spacedust.R.drawable.*
import me.arkadybazhanov.spacedust.core.*
import me.arkadybazhanov.spacedust.core.Cell.*

class LevelDrawer(private val resources: Resources) {
    fun drawLevel(level: Level, canvas: Canvas) {
        val cellWidth = canvas.width / level.w

        for ((x, y, cell) in level.withCoordinates()) {
            val bm = cell.toBitmap()
            val src = Rect(0, 0, bm.width, bm.height)
            val dst = Rect(x * cellWidth, y * cellWidth, (x + 1) * cellWidth, (y + 1) * cellWidth)
            canvas.drawBitmap(bm, src, dst, null)
        }
    }

    private fun Cell.toBitmap() = (if (characters.isNotEmpty()) player else when (this) {
        is Air -> blue
        is Stone -> white
    }).let { BitmapFactory.decodeResource(resources, it) } ?: error("Could not decode cell bitmap")
}
