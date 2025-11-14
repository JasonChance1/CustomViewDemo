package com.example.customview.widiget.basic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
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
class DrawRectView : View {
    val rect = Rect(0, 0, 50.dp, 50.dp)
    val rectF = RectF(0f, 0f, 50.fdp, 50.fdp)
    val rectPaint = Paint()
    var isRound: Boolean = false
    var roundRadius = 10.fdp
    var mColor = Color.RED
        set(value) {
            field = value;invalidate()
        }

    init {
        rectPaint.apply {
            color = mColor
        }
    }


    constructor(context: Context, isRound: Boolean = false) : super(context) {
        this.isRound = isRound
    }

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (isRound) {
            canvas.drawRoundRect(rectF, roundRadius, roundRadius, rectPaint)
        } else {
            canvas.drawRect(rect, rectPaint)
        }
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