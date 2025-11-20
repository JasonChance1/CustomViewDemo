package com.example.customview.widiget

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View

/**
 * @author wandervogel
 * @date 2025-11-20  星期四
 * @description
 */
class BezierAnimView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null
) : View(context, attributeSet) {
    var anim1: Animator
    private var p1Y = -50f
    private var p2Y = 50f
    private val path = Path()
    private val paint = Paint().apply {
        isAntiAlias = true
        isDither = true
        style = Paint.Style.STROKE
    }

    init {
        anim1 = ValueAnimator.ofFloat(-50f, 50f).apply {
            addUpdateListener {
                p1Y = it.animatedValue as Float
                p2Y = -p1Y
                invalidate()
            }
            duration = 500
            repeatCount = -1
            repeatMode = ValueAnimator.REVERSE
        }
        anim1.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(width / 2f, height / 2f)
        drawBezier(canvas)
    }

    fun drawBezier(canvas: Canvas) {
        path.reset()
        path.moveTo(-100f, 0f)
        path.quadTo(-50f, p1Y, 0f, 0f)
        path.quadTo(50f, p2Y, 100f, 0f)
        canvas.drawPath(path, paint)
    }
}