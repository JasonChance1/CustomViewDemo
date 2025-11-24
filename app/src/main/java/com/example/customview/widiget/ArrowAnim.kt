package com.example.customview.widiget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.util.AttributeSet
import android.view.View
import com.example.customview.R
import com.example.customview.utils.fdp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author wandervogel
 * @date 2025-11-24  星期一
 * @description
 */
class ArrowAnim @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {
    private var radius = 100.fdp
    private var anim: ValueAnimator
    private var curPercent = 0f
    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2.fdp
    }
    private val pathMeasure = PathMeasure()
    private val path = Path()
    private val pos = FloatArray(2)
    private val tan = FloatArray(2)
    private val arrow: Bitmap
    private val matrix = Matrix()

    init {
        path.addCircle(0f, 0f, radius, Path.Direction.CW)
        pathMeasure.setPath(path, false)
        arrow = BitmapFactory.decodeResource(context.resources, R.drawable.arrow)
        anim = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                curPercent = it.animatedValue as Float
                pathMeasure.getPosTan(curPercent * pathMeasure.length, pos, tan)
                invalidate()
            }
            duration = 5000
            repeatCount = -1
            repeatMode = ValueAnimator.RESTART
        }
        anim.start()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.translate(width / 2f, height / 2f)

        canvas.drawPath(path, paint)

        val angle = atan2(tan[1].toDouble(), tan[0].toDouble()).toFloat()
        val degrees = Math.toDegrees(angle.toDouble()).toFloat()
        matrix.reset()

        matrix.postTranslate(-arrow.width / 2f, -arrow.height / 2f)
        matrix.postRotate(degrees)
        matrix.postTranslate(pos[0], pos[1])

        // 绘制箭头
        canvas.drawBitmap(arrow, matrix, paint)

        paint.color = Color.RED
        paint.style = Paint.Style.FILL
        canvas.drawCircle(pos[0], pos[1], 5f, paint)
        paint.style = Paint.Style.STROKE
        paint.color = Color.BLACK
    }
}