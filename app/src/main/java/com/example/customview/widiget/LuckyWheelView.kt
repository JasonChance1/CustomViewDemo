package com.example.customview.widiget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.animation.addListener
import kotlin.math.min
import kotlin.random.Random

/**
 * 抽奖转盘 View
 * 作者：Chance Jason（示例实现）
 */
class LuckyWheelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    // 奖项数据类
    data class AwardData(
        var name: String,               // 奖品名称
        var showWinWeight: Float,       // 显示概率
        var realWeight: Float = showWinWeight, // 实际概率
        var color: Int = Color.LTGRAY,  // 扇形颜色
        var iconRes: Int? = null        // 可选图标
    )

    // 当前奖项列表
    private var awards = listOf<AwardData>(
        AwardData("测试1",0.1f, color = Color.parseColor("#f64921")),
        AwardData("测试2",0.3f, color = Color.parseColor("#36f921")),
        AwardData("测试3",0.4f, color = Color.parseColor("#364f21")),
        AwardData("测试4",0.4f, color = Color.parseColor("#3649f1")),
    )

    // 画笔
    private val sectorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        textSize = 40f
        typeface = Typeface.DEFAULT_BOLD
    }

    private val rectF = RectF()
    private var startAngle = 0f
    private var isSpinning = false
    private var spinAnimator: ValueAnimator? = null

    // 设置奖项数据
    fun setAwards(list: List<AwardData>) {
        awards = list
        invalidate()
    }

    // 抽奖开始
    fun startSpin(onResult: (AwardData) -> Unit) {
        if (isSpinning || awards.isEmpty()) return
        isSpinning = true

        // 1. 按权重随机选择中奖项
        val target = pickAward() ?: return
        val index = awards.indexOf(target)

        // 2. 计算每个奖区角度
        val sweepAngle = 360f / awards.size

        // 3. 转几圈后停到目标奖项（减去一半角度让指针正中）
        val endAngle =
            360f * 5 + (awards.size - index) * sweepAngle - sweepAngle / 2f

        // 4. 动画控制旋转
        spinAnimator?.cancel()
        spinAnimator = ValueAnimator.ofFloat(startAngle, endAngle).apply {
            duration = 5000L
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                startAngle = it.animatedValue as Float
                invalidate()
            }
            addListener(
                onEnd = {
                    isSpinning = false
                    startAngle %= 360f
                    onResult(target)
                }
            )
            start()
        }
    }

    // 按权重随机选择奖项
    private fun pickAward(): AwardData? {
        if (awards.isEmpty()) return null
        val totalWeight = awards.sumOf { it.realWeight.toDouble() }
        var randomValue = Random.nextDouble() * totalWeight
        for (award in awards) {
            randomValue -= award.realWeight
            if (randomValue <= 0) return award
        }
        return awards.last()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (awards.isEmpty()) return

        val size = min(width, height)
        val radius = size / 2f
        rectF.set(-radius, -radius, radius, radius)

        canvas.save()
        canvas.translate(width / 2f, height / 2f)
        canvas.rotate(startAngle)

        val sweepAngle = 360f / awards.size

        awards.forEachIndexed { index, award ->
            // 扇形颜色
            sectorPaint.color = award.color
            // 绘制扇形
            canvas.drawArc(rectF, index * sweepAngle, sweepAngle, true, sectorPaint)

            // 绘制文字
            canvas.save()
            canvas.rotate(index * sweepAngle + sweepAngle / 2f)
            textPaint.color = Color.BLACK
            drawTextCentered(canvas, award.name, 0f, -radius * 0.6f, textPaint)
            canvas.restore()
        }

        canvas.restore()

        // 绘制中间指针
        drawCenterPointer(canvas)
    }

    private fun drawTextCentered(canvas: Canvas, text: String, cx: Float, cy: Float, paint: Paint) {
        val fm = paint.fontMetrics
        val textHeight = fm.descent - fm.ascent
        canvas.drawText(text, cx, cy + textHeight / 4f, paint)
    }

    private fun drawCenterPointer(canvas: Canvas) {
        val pointerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.RED }
        val path = Path().apply {
            val cx = width / 2f
            val cy = height / 2f
            moveTo(cx, cy - 20)
            lineTo(cx + 40, cy)
            lineTo(cx - 40, cy)
            close()
        }
        canvas.drawPath(path, pointerPaint)
    }
}
