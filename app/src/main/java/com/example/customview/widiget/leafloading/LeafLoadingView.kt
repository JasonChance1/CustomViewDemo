package com.example.customview.widiget.leafloading;

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import com.example.customview.utils.dp
import com.example.customview.utils.fdp

class LeafLoadingView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var maxProgress: Int = 100

    fun setProgress(value: Int) {
        val newProgress = value.coerceIn(0, maxProgress)
        val now = SystemClock.uptimeMillis()

        // ========= 进度满 & 进入完成态 =========
        if (newProgress >= maxProgress) {
            mProgress = maxProgress.toFloat()

            // 第一次到达 100% 时，开启完成动画
            if (!isCompleted) {
                isCompleted = true
                completeStartTime = now
                completeAnimProgress = 0f

                // 目标速度设为 0，让风扇/叶子自然减速
                targetSpeedFactor = 0f
            }

            lastProgressUpdateTime = now
            lastProgress = mProgress
            if (autoRun) invalidate()
            return
        }

        // ========= 非完成态 =========
        // 如果之前是完成态，而现在进度又降下来了，相当于重置
        if (isCompleted) {
            isCompleted = false
            completeAnimProgress = 0f
        }

        // 原来的速度计算逻辑
        if (lastProgressUpdateTime > 0L) {
            val dt = (now - lastProgressUpdateTime) / 1000f
            if (dt > 0f) {
                val dp = newProgress - lastProgress.toInt()
                val speed = dp / dt
                val maxSpeed = 60f
                val target = (speed / maxSpeed).coerceIn(0f, 1f)
                targetSpeedFactor = target
            }
        }
        lastProgressUpdateTime = now
        lastProgress = newProgress.toFloat()
        mProgress = newProgress.toFloat()

        if (autoRun) invalidate()
    }

    /** 是否自动运行动画（默认 true）*/
    var autoRun: Boolean = true
        set(value) {
            field = value
            if (value) invalidate()
        }

    // ============= 内部状态 =============

    var mProgress: Float = 0f
    private var lastProgress: Float = 0f
    private var lastProgressUpdateTime: Long = 0L

    // 进度速度映射到 [0,1] 的因子，用于控制风扇/叶子
    private var speedFactor: Float = 0f
    private var targetSpeedFactor: Float = 0f

    // 动画时间
    private var lastFrameTime: Long = 0L

    // 叶子参数
    private data class Leaf(
        var x: Float = 0f,
        var baseY: Float = 0f,
        var amplitude: Float = 0f,
        var frequency: Float = 0f,
        var phase: Float = 0f,
        var speed: Float = 0f
    )

    private val leaves = mutableListOf<Leaf>()
    private var lastLeafSpawnTime: Long = 0L

    // 叶子数量上限
    private val maxLeafCount = 20

    // 叶子速度/生成间隔随进度速度变化
    private val leafSpeedBase = 80f     // px/s
    private val leafSpeedExtra = 120f   // px/s
    private val leafSpawnMinMs = 80L
    private val leafSpawnMaxMs = 600L

    // 进度条区域
    private val barRect = RectF()
    private var barRadius = 0f

    // 风扇区域
    private var fanCenterX = 0f
    private var fanCenterY = 0f
    private var fanRadius = 0f
    private var fanAngle = 0f
    private val fanBaseSpeed = 60f      // deg/s
    private val fanExtraSpeed = 600f    // deg/s

    // 画笔
    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFFCF0")  // 进度条背景
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFFB8C00") // 进度条前景
    }

    private val leafPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFFFC107") // 叶子
    }

    private val fanBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FFFBE082") // 风扇圆背景
    }

    private val fanRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 2.fdp
        color = Color.WHITE
    }

    private val fanBladePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    // 预计算用
    private val leafRadius get() = 4.dp


    // ======= 完成态相关 =======
    private var isCompleted = false                 // 是否已完成
    private var completeAnimProgress = 0f          // 0f..1f 完成动画进度
    private var completeStartTime = 0L
    private val completeAnimDuration = 500L        // 完成态过渡时间 ms

    // 完成态文字画笔
    private val fanTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }


    // ============= 布局计算 =============


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (w <= 0 || h <= 0) return

        val contentLeft = paddingLeft.toFloat()
        val contentTop = paddingTop.toFloat()
        val contentRight = (w - paddingRight).toFloat()
        val contentBottom = (h - paddingBottom).toFloat()

        val contentHeight = contentBottom - contentTop

        fanRadius = contentHeight / 2f
        fanCenterX = contentRight - fanRadius
        fanCenterY = contentTop + contentHeight / 2f

        val barLeft = contentLeft + fanRadius * 0.2f
        val barRight = fanCenterX - fanRadius * 0.5f
        val barHeight = contentHeight * 0.6f
        val barTop = fanCenterY - barHeight / 2f
        val barBottom = fanCenterY + barHeight / 2f

        barRect.set(barLeft, barTop, barRight, barBottom)
        barRadius = barRect.height() / 2f

        initLeaves()
    }

    private fun initLeaves() {
        leaves.clear()
        repeat(5) {
            leaves.add(createLeaf(initial = true))
        }
        lastLeafSpawnTime = SystemClock.uptimeMillis()
    }

    private fun createLeaf(initial: Boolean): Leaf {
        val barRight = barRect.right
        val barWidth = barRect.width().coerceAtLeast(1f)

        val x = if (initial) {
            // 初始时把叶子均匀铺在进度条中后半段
            barRight - Math.random().toFloat() * barWidth
        } else {
            // 从风扇附近生成
            barRight + Math.random().toFloat() * barWidth * 0.3f
        }

        val centerY = (barRect.top + barRect.bottom) / 2f
        val amp = 4.dp + Math.random().toFloat() * 6.dp // 振幅
        val freq = 0.01f + Math.random().toFloat() * 0.02f
        val phase = (Math.random().toFloat() * 2f * Math.PI).toFloat()

        val baseSpeed = leafSpeedBase
        val extra = leafSpeedExtra * (0.3f + speedFactor * 0.7f)
        val speed = baseSpeed + extra * (0.5f + Math.random().toFloat() * 0.5f)

        return Leaf(
            x = x,
            baseY = centerY + (Math.random().toFloat() - 0.5f) * barRect.height() * 0.3f,
            amplitude = amp,
            frequency = freq,
            phase = phase,
            speed = speed
        )
    }

    // ============= 动画 & 绘制 =============

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val now = SystemClock.uptimeMillis()
        val dt = if (lastFrameTime == 0L) 0f else (now - lastFrameTime) / 1000f
        lastFrameTime = now

        // 平滑插值速度因子
        if (dt > 0f) {
            val lerpFactor = (dt * 3f).coerceAtMost(1f)
            speedFactor = speedFactor + (targetSpeedFactor - speedFactor) * lerpFactor
        }

        // ★ 更新完成动画进度
        if (isCompleted && completeAnimProgress < 1f) {
            val t = if (completeAnimDuration > 0)
                ((now - completeStartTime).toFloat() / completeAnimDuration).coerceIn(0f, 1f)
            else 1f
            completeAnimProgress = t
        }

        // 更新风扇角度 & 叶子
        updateFan(dt)
        updateLeaves(now, dt)

        drawBar(canvas)
        drawLeaves(canvas)
        drawFan(canvas)

        // 完成后也要把动画收好：叶子飞完 & 动画结束就可以停了
        val needMoreFrame = autoRun && (
                !isCompleted ||
                        completeAnimProgress < 1f ||
                        leaves.isNotEmpty() ||
                        speedFactor > 0.01f
                )

        if (needMoreFrame) {
            postInvalidateOnAnimation()
        }
    }


    private fun updateFan(dt: Float) {
        if (dt <= 0f) return
        val speedDegPerSec = fanBaseSpeed + fanExtraSpeed * speedFactor
        fanAngle = (fanAngle + speedDegPerSec * dt) % 360f
    }

    private fun updateLeaves(now: Long, dt: Float) {
        if (dt <= 0f || barRect.width() <= 0f) return

        val barLeft = barRect.left
        val progressFraction = (mProgress / maxProgress).coerceIn(0f, 1f)
        val progressX = barLeft + barRect.width() * progressFraction

        val iterator = leaves.iterator()
        while (iterator.hasNext()) {
            val leaf = iterator.next()
            leaf.x -= leaf.speed * dt

            // 叶子到达当前进度边界 -> 被吸收
            if (leaf.x <= progressX) {
                iterator.remove()
            }
        }

        // ★ 已完成时不再生成新的叶子
        if (isCompleted) return

        // 下面是原来的生成逻辑
        val spawnInterval = lerp(
            leafSpawnMaxMs.toFloat(),
            leafSpawnMinMs.toFloat(),
            speedFactor
        ).toLong()

        if (now - lastLeafSpawnTime >= spawnInterval && leaves.size < maxLeafCount) {
            leaves.add(createLeaf(initial = false))
            lastLeafSpawnTime = now
        }
    }


    private fun drawBar(canvas: Canvas) {
        // 背景条
        canvas.drawRoundRect(barRect, barRadius, barRadius, trackPaint)

        // 进度部分
        val fraction = (mProgress / maxProgress).coerceIn(0f, 1f)
        if (fraction > 0f) {
            val right = barRect.left + barRect.width() * fraction
            val rect = RectF(barRect.left, barRect.top, right, barRect.bottom)
            canvas.drawRoundRect(rect, barRadius, barRadius, progressPaint)
        }
    }

    private fun drawLeaves(canvas: Canvas) {
        if (leaves.isEmpty()) return

        val centerY = (barRect.top + barRect.bottom) / 2f
        for (leaf in leaves) {
            val y = leaf.baseY + leaf.amplitude *
                    kotlin.math.sin(leaf.frequency * leaf.x + leaf.phase)
            canvas.drawCircle(leaf.x, y, leafRadius.toFloat(), leafPaint)
        }
    }

    private fun drawFan(canvas: Canvas) {
        // 底圆
        canvas.drawCircle(fanCenterX, fanCenterY, fanRadius, fanBgPaint)
        canvas.drawCircle(fanCenterX, fanCenterY, fanRadius - 1.dp, fanRingPaint)

        // ========= 画叶片（完成态中逐渐淡出） =========
        val bladeAlpha = ((1f - completeAnimProgress) * 255).toInt().coerceIn(0, 255)
        if (bladeAlpha > 0) {
            fanBladePaint.alpha = bladeAlpha

            canvas.save()
            canvas.translate(fanCenterX, fanCenterY)
            canvas.rotate(fanAngle)

            val bladeLen = fanRadius * 0.6f
            val bladeWidth = fanRadius * 0.25f
            val rect = RectF(
                fanRadius * 0.15f,
                -bladeWidth / 2f,
                fanRadius * 0.15f + bladeLen,
                bladeWidth / 2f
            )

            repeat(3) {
                canvas.drawRoundRect(rect, bladeWidth / 2f, bladeWidth / 2f, fanBladePaint)
                canvas.rotate(120f)
            }

            canvas.restore()
        }

        // ========= 完成态文字“100%”（淡入） =========
        if (isCompleted) {
            val textAlpha = (completeAnimProgress * 255).toInt().coerceIn(0, 255)
            if (textAlpha > 0) {
                fanTextPaint.alpha = textAlpha
                fanTextPaint.textSize = fanRadius * 0.6f

                val text = "100%"
                val fm = fanTextPaint.fontMetrics
                val baseline = fanCenterY - (fm.ascent + fm.descent) / 2f

                canvas.drawText(text, fanCenterX, baseline, fanTextPaint)
            }
        }
    }


    // 线性插值
    private fun lerp(start: Float, end: Float, t: Float): Float {
        return start + (end - start) * t
    }
}
