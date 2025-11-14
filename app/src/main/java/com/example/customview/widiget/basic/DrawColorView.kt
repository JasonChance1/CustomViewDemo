package com.example.customview.widiget.basic

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import com.example.customview.utils.dp

/**
 * @author wandervogel
 * @date 2025-10-20  星期一
 * @description
 */
class DrawColorView : View {
    constructor(context: Context) : super(context)

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)


    var mColor = Color.RED
        set(value) {
            field = value;invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(mColor)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)

        // 如果测量模式是未指定的，就使用 match_parent 的行为
        val measuredWidth = when (widthMode) {
            MeasureSpec.UNSPECIFIED -> resources.displayMetrics.widthPixels
            else -> widthSize
        }



        setMeasuredDimension(measuredWidth, 50.dp)
    }

}