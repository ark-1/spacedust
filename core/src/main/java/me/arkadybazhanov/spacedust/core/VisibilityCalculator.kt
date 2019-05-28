package me.arkadybazhanov.spacedust.core

import java.lang.Math.sqrt


class VisibilityCalculator(val character: Character, val radius: Int) {

    val w = character.level.w
    val h = character.level.h
    val visibilityMap = Array(w) { BooleanArray(h)}
    val startX = character.position.x
    val startY = character.position.y
    val level = character.level

    fun calculateVisibility(): Array<BooleanArray> {
        visibilityMap[character.position.x][character.position.y] = true
        for (d in Character.bishopDirections) {
            castLight(1, 1.0f, 0.0f, 0, d.x, d.y, 0)
            castLight(1, 1.0f, 0.0f, d.x, 0, 0, d.y)
        }
        return visibilityMap
    }

    private fun castLight(row: Int, start: Float, end: Float, xx: Int, xy: Int, yx: Int, yy: Int) {
        var start = start
        var newStart = 0.0f
        if (start < end) {
            return
        }
        var blocked = false
        var distance = row
        while (distance <= radius && !blocked) {
            val deltaY = -distance
            for (deltaX in -distance..0) {
                val currentX = startX + deltaX * xx + deltaY * xy
                val currentY = startY + deltaX * yx + deltaY * yy
                val leftSlope = (deltaX - 0.5f) / (deltaY + 0.5f)
                val rightSlope = (deltaX + 0.5f) / (deltaY - 0.5f)

                if (!(currentX >= 0 && currentY >= 0 && currentX < w && currentY < h) || start < rightSlope) {
                    continue
                } else if (end > leftSlope) {
                    break
                }
                if (sqrt((deltaX * deltaX + deltaY * deltaY).toDouble()) <= radius) {
                    visibilityMap[currentX][currentY] = true
                }

                if (blocked) {
                    if (level[currentX, currentY].type == CellType.STONE) {
                        newStart = rightSlope
                        continue
                    } else {
                        blocked = false
                        start = newStart
                    }
                } else {
                    if (level[currentX, currentY].type == CellType.STONE && distance < radius) {
                        blocked = true
                        castLight(distance + 1, start, leftSlope, xx, xy, yx, yy)
                        newStart = rightSlope
                    }
                }
            }
            distance++
        }

    }
}
