package com.example.customview.widiget.basic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.customview.utils.dp
import com.example.customview.utils.fdp

/**
 * @author wandervogel
 * @date 2025-10-20  星期一
 * @description
 */
class DrawArcView : View {
    val rectF = RectF(0f, 0f, 50f.fdp, 40f.fdp)
    val rectPaint = Paint()
    private val path = Path()
    var mColor = Color.RED
        set(value) {
            field = value;invalidate()
        }

    init {
        rectPaint.apply {
            color = mColor
            style = Paint.Style.STROKE
        }
    }


    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        path.reset()
//        path.arcTo(rectF,135f,270f)
//        canvas.drawPath(path,rectPaint)
        canvas.drawArc(rectF, 135f, 270f, true, rectPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        // 如果测量模式是未指定的，就使用 match_parent 的行为
        val measuredWidth = when (widthMode) {
            MeasureSpec.UNSPECIFIED -> resources.displayMetrics.widthPixels
            else -> widthSize
        }

        val measuredHeight = when (heightMode) {
            MeasureSpec.UNSPECIFIED -> rectF.height().toInt() + paddingTop + paddingBottom
            MeasureSpec.AT_MOST -> {
                val desiredHeight = rectF.height().toInt() + paddingTop + paddingBottom
                desiredHeight.coerceAtMost(heightSize)
            }

            else -> heightSize
        }
        setMeasuredDimension(measuredWidth, measuredHeight)
    }

}