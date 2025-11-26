package com.example.customview.widiget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import com.example.customview.utils.fdp
import kotlin.math.min

/**
 * @author wandervogel
 * @date 2025-11-26  星期三
 * @description 仿https://github.com/GcsSloop/AndroidNote/blob/master/CustomView/Advance/Code/SearchView.md
 */
class SearchView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {
    private val pathSearch = Path()
    private val searchDst = Path()

    private val pathCircle = Path()
    private val circleDst = Path()
    private val pos = FloatArray(2)


    private val rectSearch = RectF(-50f, -50f, 50f, 50f)
    private val rectCircle = RectF(-100f, -100f, 100f, 100f)

    val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2.fdp
        isAntiAlias = true
    }

    var progress = 0f

    val animator: ValueAnimator

    private var isSearching = false
    private val pathMeasure = PathMeasure()

    init {
        animator = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                progress = it.animatedValue as Float
                invalidate()
            }
            duration = 2000
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
        }
        animator.start()
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val mW = MeasureSpec.getSize(widthMeasureSpec)
        val mH = MeasureSpec.getSize(heightMeasureSpec)
        val size = min(mW, mH)
        setMeasuredDimension(size, size)
        val innerRadius = size * 0.2f
        val outerRadius = size * 0.4f
        rectSearch.set(-innerRadius, -innerRadius, innerRadius, innerRadius)
        rectCircle.set(-outerRadius, -outerRadius, outerRadius, outerRadius)
        pathSearch.reset()
        pathCircle.reset()
        pathSearch.addArc(rectSearch, 45f, 359.9f)
        pathCircle.addArc(rectCircle, 45f, 359.9f)
        pathMeasure.setPath(pathCircle, false)

        pathMeasure.getPosTan(0f, pos, null)// 得到外圆的起点坐标，也可以用半径*cos(pi/4)和sin计算
        pathSearch.lineTo(pos[0], pos[1])// 把手
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(width / 2f, height / 2f)

        drawSearch(canvas)
    }

    private fun drawSearch(canvas: Canvas) {
        canvas.drawColor(Color.parseColor("#0082D7"))
        if (isSearching) {
            // 搜索中：外圈动画保持原来的线性效果
            animator.repeatMode = ValueAnimator.RESTART
            circleDst.reset()
            pathMeasure.setPath(pathCircle, false)
            val length = pathMeasure.length
            val end = progress * length
            val start = end - (0.5f - kotlin.math.abs(progress - 0.5f)) * (length * 0.4f)
            pathMeasure.getSegment(start, end, circleDst, true)
            canvas.drawPath(circleDst, paint)
        } else {
            // 放大镜：加“出现后停顿”
            animator.repeatMode = ValueAnimator.REVERSE
            pathMeasure.setPath(pathSearch, false)
            searchDst.reset()
            val length = pathMeasure.length

            val drawProgress = mapProgressWithHold(progress)
            pathMeasure.getSegment(drawProgress * length, length, searchDst, true)
            canvas.drawPath(searchDst, paint)
        }
    }
    // 出现后停顿的时间占整段动画的比例，比如 0.25f 就是停 25% * duration
    private val holdFraction = 0.25f

    /**
     * 动画停顿映射
     * raw: ValueAnimator 原始 0-1 (再反向 1-0)
     * return: 用来实际绘制的 progress
     */
    private fun mapProgressWithHold(raw: Float): Float {
        return if (raw <= holdFraction) {
            // 这段时间里进度锁在 0，放大镜保持完整出现
            0f
        } else {
            // 剩余时间再把 0-1 跑完
            (raw - holdFraction) / (1f - holdFraction)
        }
    }


    fun startSearch() {
        isSearching = true
        invalidate()
    }

     fun stopSearch() {
        isSearching = false
        invalidate()
    }
}