package me.arkadybazhanov.spacedust

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.*
import android.view.SurfaceHolder.Callback

class HealthView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), Callback {
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        ViewUpdater.healthSurfaceHolder = null
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        ViewUpdater.healthSurfaceHolder = holder
        println("$height $y ${(parent as ConstraintLayout).y}")
    }

    init {
        holder.addCallback(this)
    }
}