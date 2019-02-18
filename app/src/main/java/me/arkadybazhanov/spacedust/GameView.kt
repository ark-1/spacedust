package me.arkadybazhanov.spacedust

import android.content.*
import android.graphics.*
import android.hardware.Camera
import android.util.*
import android.view.*
import me.arkadybazhanov.spacedust.core.*
import android.widget.ZoomControls

class GameView(context: Context, attributes: AttributeSet) : SurfaceView(context, attributes), SurfaceHolder.Callback {
    lateinit var camera: Camera
    private var inPreview = false
/*    private lateinit var sgd: ScaleGestureDetector
    private var isSingleTouch: Boolean = false
    private var _width: Float = 0f
    private var _height = 0f
    private var scale = 1f
    private val minScale = 1f
    private val maxScale = 5f
    private var _left: Int = 0
    private var _top: Int = 0
    private var _right: Int = 0
    private var _bottom: Int = 0*/

    init {
        holder.addCallback(this)
/*
        var dX: Float = 0.toFloat()
        var dY: Float = 0.toFloat()

        setOnTouchListener { _, event ->
            sgd.onTouchEvent(event)
            if (event.pointerCount > 1) {
                isSingleTouch = false
            } else {
                if (event.action == ACTION_UP) {
                    isSingleTouch = true
                }
            }
            when (event.action) {
                ACTION_DOWN -> {
                    dX = x - event.rawX
                    dY = y - event.rawY
                }

                ACTION_MOVE -> if (isSingleTouch) {
                    animate()
                        .x(event.rawX + dX)
                        .y(event.rawY + dY)
                        .setDuration(0)
                        .start()
                    checkDimension()
                }
            }
            true
        }

        sgd = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                Log.e("onGlobalLayout: ", "$scale $_width $_height")
                scale *= detector.scaleFactor
                scale = Math.max(minScale, Math.min(scale, maxScale))

                val params = ConstraintLayout.LayoutParams((_width * scale).toInt(), (_height * scale).toInt())
                this@GameView.layoutParams = params
                checkDimension()
                return true
            }
        })

        viewTreeObserver.addOnGlobalLayoutListener {}
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (_width == 0f && _height == 0f) {
            _width = width.toFloat()
            _height = height.toFloat()
            _left = left
            _right = right
            _top = top
            _bottom = bottom
        }
    }

    private fun checkDimension() {
        if (x > _left) {
            animate()
                .x(_left.toFloat())
                .y(y)
                .setDuration(0)
                .start()
        }

        if (width + x < _right) {
            animate()
                .x((_right - width).toFloat())
                .y(y)
                .setDuration(0)
                .start()
        }

        if (y > _top) {
            animate()
                .x(x)
                .y(_top.toFloat())
                .setDuration(0)
                .start()
        }

        if (height + y < _bottom) {
            animate()
                .x(x)
                .y((_bottom - height).toFloat())
                .setDuration(0)
                .start()
        }*/
    }

    private val thread = GameThread(holder, this)
    private val drawer = FieldDrawer(resources)
    private val field = Field(Array(40) { x ->
        Array(40) { y ->
            if (x % (y + 1) == 0) Cell.Stone else Cell.Air
        }
    })

    private val game = Game()

    private fun getBestPreviewSize(width: Int, height: Int, parameters: Camera.Parameters): Camera.Size? {
        var result: Camera.Size? = null
        for (size in parameters.supportedPreviewSizes) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size
                } else {
                    val resultArea = result.width * result.height
                    val newArea = size.width * size.height
                    if (newArea > resultArea) {
                        result = size
                    }
                }
            }
        }
        return result
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        thread.setRunning(true)
        thread.start()
        camera.stopPreview()
        camera.setPreviewDisplay(holder)
    }

    private var currentZoomLevel = 0
    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        val params = camera.parameters!!
        params.flashMode = Camera.Parameters.FLASH_MODE_ON
        val size = getBestPreviewSize(width, height, params)
        if (size != null) {
            params.setPreviewSize(size.width, size.height)
            camera.parameters = params
            camera.startPreview()
            inPreview = true
            val zoomControls = findViewById<ZoomControls>(R.id.zoomControls)

            if (params.isZoomSupported) {
                val maxZoomLevel = params.maxZoom
                Log.i("max ZOOM ", "is $maxZoomLevel")
                zoomControls.setIsZoomInEnabled(true)
                zoomControls.setIsZoomOutEnabled(true)

                zoomControls.setOnZoomInClickListener {
                    if (currentZoomLevel < maxZoomLevel) {
                        currentZoomLevel++
                        //mCamera.startSmoothZoom(currentZoomLevel);
                        params.zoom = currentZoomLevel
                        camera.parameters = params
                    }
                }

                zoomControls.setOnZoomOutClickListener {
                    if (currentZoomLevel > 0) {
                        currentZoomLevel--
                        params.zoom = currentZoomLevel
                        camera.parameters = params
                    }
                }
            } else zoomControls.visibility = View.GONE
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        while (true) catchPrint {
            thread.setRunning(false)
            thread.join()
            return
        }
    }

    fun update() = game.update()

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        drawer.drawField(field, canvas)
    }
}