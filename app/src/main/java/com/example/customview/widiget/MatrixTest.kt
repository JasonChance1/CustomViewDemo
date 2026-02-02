package com.example.customview.widiget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.example.customview.R

/**
 * @author wandervogel
 * @date 2025-12-04  星期四
 * @description
 */
class MatrixTest(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    val rect = Rect(0, 0, 100, 100)
    val bitmap: Bitmap = BitmapFactory.decodeResource(resources, R.mipmap.test)
//    val desBitmap: Bitmap
    val paint = Paint()

    private val mMatrix = Matrix()
    init {
//        desBitmap = Bitmap.createBitmap(bitmap, 0, bitmap.height, 100, 100, matrix, false)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val scaleX = 100 / bitmap.width.toFloat()
        val scaleY = 100 / bitmap.height.toFloat()
        mMatrix.postScale(scaleX, scaleY)
        canvas.drawBitmap(bitmap,mMatrix,paint)
    }
}