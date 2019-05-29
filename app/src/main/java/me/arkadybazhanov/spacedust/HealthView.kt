package me.arkadybazhanov.spacedust

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.*
import android.view.SurfaceHolder.Callback
import kotlinx.android.synthetic.main.activity_main.view.*

class HealthView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), Callback {
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        ViewUpdater.healthSurfaceHolder = null
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        ViewUpdater.healthSurfaceHolder = holder
        ViewUpdater.healthView = this
        println("$height $y ${(parent as ConstraintLayout).y}")
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        val view = (parent.parent as ConstraintLayout).gameView
        if (view != null && view.snapshot != null) {
            canvas.drawRect(
                0F,
                0F,
                canvas.width * (view.snapshot!!.playerSnapshot.hp.toFloat() / view.snapshot!!.playerSnapshot.maxHp),
                canvas.height.toFloat(),
                Paint().apply {
                    color = Color.RED
                }
            )
        }
    }

    init {
        holder.addCallback(this)
    }
}
