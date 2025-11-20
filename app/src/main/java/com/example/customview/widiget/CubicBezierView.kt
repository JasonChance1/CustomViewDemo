package com.example.customview.widiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.customview.utils.fdp

class CubicBezierView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val p0 = PointF()
    private val p1 = PointF()
    private val p2 = PointF()
    private val p3 = PointF()
    private val p4 = PointF()
    private val p5 = PointF()
    private val p6 = PointF()
    private val p7 = PointF()
    private val p8 = PointF()
    private val p9 = PointF()
    private val p10 = PointF()
    private val p11 = PointF()

    private val path = Path()

    // 曲线画笔
    private val curvePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f.fdp
        color = Color.RED
    }

    // 辅助线画笔（P0-P1-P2-P3）
    private val helpLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2f.fdp
        color = Color.GRAY
        pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f)
    }

    // 控制点画笔
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.BLUE
    }

    // 控制点半径 & 点击判定半径
    private val pointRadius = 6f.fdp

    private fun drawL(canvas: Canvas) {
        canvas.translate(width / 4f, height / 4f)
        val w = 200.fdp
        val h = 200.fdp
        p0.set(0f, (-50).fdp)
        p1.set((-50).fdp, (-100).fdp)
        p2.set((-100).fdp, (-75).fdp)
        p3.set((-100).fdp, (0).fdp)
        p4.set((-90).fdp, 70.fdp)
        p5.set((-50).fdp, 75.fdp)
        p6.set(0.fdp, 100.fdp)
        p7.set(50.fdp, 75.fdp)
        p8.set(90.fdp, 70.fdp)
        p9.set(100.fdp, 0.fdp)
        p10.set(100.fdp, (-75).fdp)
        p11.set(75.fdp, (-100).fdp)


        // 1. 画辅助折线 P0-P1-P2-P3
        path.reset()
        path.moveTo(p0.x, p0.y)
        path.lineTo(p1.x, p1.y)
        path.lineTo(p2.x, p2.y)
        path.lineTo(p3.x, p3.y)
        path.moveTo(p3.x, p3.y)
        path.lineTo(p4.x, p4.y)
        path.lineTo(p5.x, p5.y)
        path.lineTo(p6.x, p6.y)
        path.moveTo(p6.x, p6.y)
        path.lineTo(p7.x, p7.y)
        path.lineTo(p8.x, p8.y)
        path.lineTo(p9.x, p9.y)
        path.moveTo(p9.x, p9.y)
        path.lineTo(p10.x, p10.y)
        path.lineTo(p11.x, p11.y)
        path.lineTo(p0.x, p0.y)
        canvas.drawPath(path,helpLinePaint)

        // 2. 画四个控制点
        canvas.drawCircle(p0.x, p0.y, pointRadius, pointPaint)
        canvas.drawCircle(p1.x, p1.y, pointRadius, pointPaint)
        canvas.drawCircle(p2.x, p2.y, pointRadius, pointPaint)
        canvas.drawCircle(p3.x, p3.y, pointRadius, pointPaint)
        canvas.drawCircle(p4.x, p4.y, pointRadius, pointPaint)
        canvas.drawCircle(p5.x, p5.y, pointRadius, pointPaint)
        canvas.drawCircle(p6.x, p6.y, pointRadius, pointPaint)
        canvas.drawCircle(p7.x, p7.y, pointRadius, pointPaint)
        canvas.drawCircle(p8.x, p8.y, pointRadius, pointPaint)
        canvas.drawCircle(p9.x, p9.y, pointRadius, pointPaint)
        canvas.drawCircle(p10.x, p10.y, pointRadius, pointPaint)
        canvas.drawCircle(p11.x, p11.y, pointRadius, pointPaint)

        path.reset()
        path.moveTo(p0.x, p0.y)
        path.cubicTo(
            p1.x, p1.y,
            p2.x, p2.y,
            p3.x, p3.y
        )
        path.cubicTo(
            p4.x, p4.y,
            p5.x, p5.y,
            p6.x, p6.y
        )
        path.cubicTo(
            p7.x, p7.y,
            p8.x, p8.y,
            p9.x, p9.y
        )
        path.cubicTo(
            p10.x, p10.y,
            p11.x, p11.y,
            p0.x, p0.y
        )
        canvas.drawPath(path, curvePaint)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawL(canvas)
    }
}
