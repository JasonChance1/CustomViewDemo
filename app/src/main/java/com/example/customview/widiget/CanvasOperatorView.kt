package com.example.customview.widiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View

/**
 * @author wandervogel
 * @date 2025-10-23  星期四
 * @description
 */
class CanvasOperatorView(context:Context,attributeSet: AttributeSet?): View(context,attributeSet) {
    private val paint = Paint().apply{
        color = Color.BLACK
        style = Paint.Style.STROKE
    }
    private val rect = Rect()
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(width/2f,height/2f)
        rect.set(0,-400,400,0)
        canvas.drawColor(Color.YELLOW)
        canvas.drawRect(rect,paint)
        canvas.scale(-0.5f,0.5f,-200f,0f)
        canvas.drawColor(Color.CYAN)
        canvas.drawRect(rect,paint)
    }
}