package com.example.customview.widiget

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.customview.utils.fdp
import java.util.*
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * 时钟
 */
class TimePanel(context: Context, attributeSet: AttributeSet?) : View(context, attributeSet) {

    // 表盘参数
    private var radius = 100.fdp
    private val centerPointRadius = 3.fdp // 中心圆点半径

    // 画笔
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val handPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 时间对象
    private val calendar = Calendar.getInstance()

    // 控制刷新
    private var running = true

    init {
        // 初始化文字画笔
        textPaint.color = Color.BLACK
        textPaint.textSize = 12.fdp
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.typeface = Typeface.DEFAULT_BOLD
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        running = true
        postInvalidateOnAnimation() // 启动刷新循环
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        running = false
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 计算半径，保证自适应布局大小
        radius = min(width, height) / 2f - 10.fdp

        // 坐标移动到中心，也可以不移动，后面计算直接用cx,cy
        canvas.translate(width / 2f, height / 2f)

        // 绘制外环
        paint.color = Color.BLACK
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 2.fdp
        canvas.drawCircle(0f, 0f, radius, paint)

        // 绘制刻度
        drawScale(canvas)

        // 绘制数字（12、3、6、9）
        drawNumbers(canvas)

        // 绘制指针
        drawHands(canvas)

        // 绘制中心点
        paint.style = Paint.Style.FILL
        canvas.drawCircle(0f, 0f, centerPointRadius, paint)

        // 不使用 ValueAnimator，而用系统时间更新（更精准）
        if (running) postInvalidateOnAnimation()
    }

    /**
     * 绘制表盘刻度
     */
    private fun drawScale(canvas: Canvas) {
        for (i in 0 until 60) {// 通过旋转画布实现，也可以不旋转，计算每个刻度的角度，然后通过sin、cos计算每个刻度的startX,startY,endX,endY,需要注意sin、cos的参数为弧度，而不是角度,使用如:sin(Math.toRadians(60))
            val isMajor = i % 5 == 0
            paint.strokeWidth = if (isMajor) 2.fdp else 1.fdp
            val startY = if (isMajor) -radius + 12.fdp else -radius + 6.fdp
            canvas.drawLine(0f, startY, 0f, -radius, paint)
            canvas.rotate(6f)
        }
    }

    /**
     * 绘制数字（12、3、6、9）
     */
    private fun drawNumbers(canvas: Canvas) {
        val positions = arrayOf("12", "3", "6", "9")
        val angles = arrayOf(-90f, 0f, 90f, 180f)
        val textOffset = radius - 30.fdp // 数字距离圆心的距离
        val fontMetrics = textPaint.fontMetrics

        for (i in positions.indices) {
            val angle = Math.toRadians(angles[i].toDouble())
            val x = (cos(angle) * textOffset).toFloat()
            val y = (sin(angle) * textOffset - (fontMetrics.ascent + fontMetrics.descent) / 2).toFloat()
            canvas.drawText(positions[i], x, y, textPaint)
        }
    }

    /**
     * 绘制时针、分针、秒针
     */
    private fun drawHands(canvas: Canvas) {
        calendar.timeInMillis = System.currentTimeMillis()

        val hour = calendar.get(Calendar.HOUR)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)
        val millis = calendar.get(Calendar.MILLISECOND)

        // 计算角度（平滑过渡）
        val secondAngle = (second + millis / 1000f) * 6f
        val minuteAngle = (minute + second / 60f) * 6f
        val hourAngle = (hour + minute / 60f) * 30f

        // 时针
        handPaint.color = Color.BLACK
        handPaint.strokeWidth = 4.fdp
        handPaint.strokeCap = Paint.Cap.ROUND
        canvas.save()
        canvas.rotate(hourAngle)
        canvas.drawLine(0f, 0f, 0f, -radius * 0.5f, handPaint)
        canvas.restore()

        // 分针
        handPaint.color = Color.DKGRAY
        handPaint.strokeWidth = 3.fdp
        canvas.save()
        canvas.rotate(minuteAngle)
        canvas.drawLine(0f, 0f, 0f, -radius * 0.7f, handPaint)
        canvas.restore()

        // 秒针
        handPaint.color = Color.RED
        handPaint.strokeWidth = 2.fdp
        canvas.save()
        canvas.rotate(secondAngle)
        canvas.drawLine(0f, 10.fdp, 0f, -radius * 0.9f, handPaint)
        canvas.restore()
    }
}