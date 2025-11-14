package com.example.customview.widiget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.example.customview.entity.PieData
import com.example.customview.utils.dp
import com.example.customview.utils.fdp
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * @author wandervogel
 * @date 2025-10-22  星期三
 * @description
 */
class PieView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyle: Int = 0
) :
    View(context, attributeSet, defStyle) {
    val arcPaint = Paint()
    val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 14.fdp
        textAlign = Paint.Align.CENTER
    }
    var totalValue = 0f

    private var cx = 0f
    private var cy = 0f

    val dataList = mutableListOf<PieData>()
    val colorList = listOf(
        Color.parseColor("#FF6B6B"),
        Color.parseColor("#4ECDC4"),
        Color.parseColor("#45B7D1"),
        Color.parseColor("#96CEB4"),
        Color.parseColor("#FFEAA7"),
        Color.parseColor("#DDA0DD"),
        Color.parseColor("#98D8C8"),
        Color.parseColor("#F7DC6F"),
        Color.parseColor("#BB8FCE"),
        Color.parseColor("#85C1E9")
    )

    var radius = 40.dp
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawArc(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val size = min(widthSize, heightSize)
        setMeasuredDimension(size, size)
        radius = size / 2

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cx = w / 2f
        cy = h / 2f
    }

    private fun drawArc(canvas: Canvas) {
        if (dataList.isEmpty()) return

        totalValue = dataList.sumOf { it.value.toDouble() }.toFloat()
        if (totalValue == 0f) return

        var startDegree = -90f

        // 计算并调整百分比，确保总和为100%
        val percentages = calculateAdjustedPercentages()

        dataList.forEachIndexed { index, pieData ->
            val percent = percentages[index]
            val angle = 360 * percent

            // 绘制扇形
            arcPaint.color = getColor(index)
            canvas.drawArc(
                cx - radius,
                cy - radius,
                cx + radius,
                cy + radius,
                startDegree,
                angle,
                true,
                arcPaint
            )

            // 计算文本位置
            val midDegree = startDegree + angle / 2
            val textRadius = radius * 0.6f

            val radian = Math.toRadians(midDegree.toDouble())
            val textX = cx + (textRadius * cos(radian)).toFloat()
            val textY = cy + (textRadius * sin(radian)).toFloat()

            // 绘制文本
            val displayText = "${pieData.name}\n${(percent * 100).toInt()}%"
            canvas.drawText(displayText, textX, textY, textPaint)

            startDegree += angle
        }
    }


    fun getColor(index: Int): Int {
        return colorList[index % colorList.size]
    }

    fun setData(pieDataList: List<PieData>) {
        dataList.apply {
            clear()
            addAll(pieDataList)
            invalidate()
        }
    }

    private fun calculateAdjustedPercentages(): List<Float> {
        val rawPercentages = dataList.map { it.value / totalValue }
        val roundedPercentages = rawPercentages.map { (it * 100).toInt() }

        val totalRounded = roundedPercentages.sum()
        val diff = 100 - totalRounded

        if (diff != 0) {
            // 找到原始百分比与四舍五入后差异最大的项，调整它
            val adjustedPercentages = roundedPercentages.toMutableList()
            val differences = rawPercentages.mapIndexed { index, raw ->
                index to (raw * 100 - roundedPercentages[index])
            }

            // 对差异排序，如果diff为正，给差异最大的加；如果为负，给差异最小的减
            val sortedDifferences = if (diff > 0) {
                differences.sortedByDescending { it.second }
            } else {
                differences.sortedBy { it.second }
            }

            repeat(abs(diff)) { i ->
                val indexToAdjust = sortedDifferences[i % sortedDifferences.size].first
                adjustedPercentages[indexToAdjust] += if (diff > 0) 1 else -1
            }

            return adjustedPercentages.map { it / 100f }
        }

        return roundedPercentages.map { it / 100f }
    }
}