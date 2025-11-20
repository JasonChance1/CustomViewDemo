package com.example.customview.widiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.Path
import android.graphics.PointF
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.customview.utils.fdp

/**
 * @author wandervogel
 * @date 2025-11-19  星期三
 * @description
 */
class BezierView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet? = null) :
    View(context, attributeSet) {
    private val startPoint: PointF = PointF(0f, 0f)
    private val endPoint: PointF = PointF(0f, 0f)
    private val controlPoint: PointF = PointF(0f, 0f)
    private val path = Path()
    val paint = Paint().apply {
        color = Color.GRAY
        isAntiAlias = true
        style = Style.STROKE
    }

    var cx = 0f
    var cy = 0f

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cx = w / 2f
        cy = h / 2f

        startPoint.x = 0f + 50.fdp
        startPoint.y = cy
        endPoint.x = w.toFloat() - 50.fdp
        endPoint.y = cy
        controlPoint.x = cx
        controlPoint.y = cy + 100
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_MOVE -> {
                controlPoint.x = event.x
                controlPoint.y = event.y
                invalidate()
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.strokeWidth = 10.fdp
        canvas.drawPoint(startPoint.x, startPoint.y, paint)
        canvas.drawPoint(endPoint.x, endPoint.y, paint)
        canvas.drawPoint(controlPoint.x, controlPoint.y, paint)
        paint.strokeWidth = 3.fdp
        path.reset()
        path.moveTo(startPoint.x, startPoint.y)
        path.lineTo(controlPoint.x, controlPoint.y)
        path.lineTo(endPoint.x, endPoint.y)
        path.moveTo(startPoint.x, startPoint.y)
        path.quadTo(controlPoint.x, controlPoint.y, endPoint.x, endPoint.y)
        canvas.drawPath(path, paint)
    }

}