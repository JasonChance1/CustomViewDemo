package com.example.customview.widiget

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import kotlin.math.*

/**
 * @author wandervogel
 * @date 2026-06-06  星期五
 * @description
 */
class CubeAlbumView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    /** 0..5：front, right, back, left, top, bottom */
    private var faceBitmaps: Array<Bitmap?> = arrayOfNulls(6)

    /** 自动旋转角度（度） */
    private var rotX = 20f
    private var rotY = 0f

    /** 自动旋转速度（度/秒） */
    var autoSpeedY = 24f
    var autoSpeedX = 10f

    /** 立方体半边长（像素），会在 onSizeChanged 里按 view 尺寸算 */
    private var half = 0f

    /** 透视参数：越大透视越弱（更“远”），建议 6~12 * half */
    private var persp = 0f


    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        isFilterBitmap = true
    }

    /** 每个面投影后的 2D 四边形（8 个 float：x0,y0..x3,y3）用于点击命中 */
    private val projected = Array(6) { FloatArray(8) }

    /** 每个面平均深度（用于排序，越大越靠近相机） */
    private val faceDepth = FloatArray(6)

    /** 点击回调：faceIndex 0..5 */
    var onFaceClick: ((Int) -> Unit)? = null

    // --- 动画 ---
    private var running = true
    private var lastFrameNs = 0L// 记录上一帧画面渲染(上一次执行onDraw)的时间，单位：纳秒

    private var recoverAnim: ValueAnimator? = null

    // --- 触摸暂停/恢复 ---
    private var downX = 0f
    private var downY = 0f
    private var downTime = 0L
    private var moved = false
    private val touchSlopPx = 30f


    private var lastX = 0f
    private var lastY = 0f
    private var manualDragging = false

    // 灵敏度：像素 → 角度
    private var degPerPx = 0.25f   // 最佳0.15 ~ 0.4

    private var cx: Float = 0f
    private var cy: Float = 0f

    /**
     * 设置6个面的图片，超出6张只取前6张
     */
    fun setFaces(bitmaps: List<Bitmap>) {
        require(bitmaps.size >= 6) { "至少需要6张图片" }
        for (i in 0 until 6) faceBitmaps[i] = bitmaps[i]
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        half = min(w, h) * 0.28f
        persp = half * 8f
        cx = w / 2f
        cy = h / 2f
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        running = true
        lastFrameNs = 0L
        postInvalidateOnAnimation()
    }

    override fun onDetachedFromWindow() {
        running = false
        recoverAnim?.cancel()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // 自动旋转（如果没暂停）
        val now = System.nanoTime()// 纳秒，更精确的计算两帧之间的间隔
        if (running) {
            if (lastFrameNs != 0L) {
                val dt = (now - lastFrameNs) / 1_000_000_000f
                rotY = (rotY + autoSpeedY * dt) % 360f// onDraw一帧旋转的速度，60Hz刷新率对应1000/60≈16ms,dt≈16
                rotX = (rotX + autoSpeedX * dt) % 360f
            }
            lastFrameNs = now
        } else {
            lastFrameNs = 0L
        }

//        val cx = width * 0.5f
//        val cy = height * 0.5f

        // 计算 6 个面的投影与深度
        computeAllFacesProjection()

        // 按深度排序后绘制：远的先画，近的后画
        val order = (0 until 6).sortedBy { faceDepth[it] } // depth 小=远
        for (idx in order) {
            val bmp = faceBitmaps[idx] ?: continue
            drawFace(canvas, bmp, projected[idx])
        }

        if (isAttachedToWindow) postInvalidateOnAnimation()
    }

    /** 画一面：把 bitmap 通过 polyToPoly 映射到四边形 */
    private fun drawFace(canvas: Canvas, bmp: Bitmap, dst8: FloatArray) {
        val src = floatArrayOf(
            0f, 0f,
            bmp.width.toFloat(), 0f,
            bmp.width.toFloat(), bmp.height.toFloat(),
            0f, bmp.height.toFloat()
        )

        val m = Matrix()
        m.setPolyToPoly(src, 0, dst8, 0, 4)

        canvas.save()
        canvas.concat(m)
        canvas.drawBitmap(bmp, 0f, 0f, paint)
        canvas.restore()

        // 可选：加一层轻微边框
        // canvas.drawPath(pathFrom(dst8), borderPaint)
    }

    /**
     * 计算6面四边形投影 + 深度
     */
    private fun computeAllFacesProjection() {
        // 立方体局部坐标：中心 (0,0,0)，每个面是一个正方形
        // 顶点顺序保持一致（顺时针/逆时针都行，但要统一）
        val h = half

        // 6个面3D四个顶点（x,y,z），水平向右x，垂直向下y，屏幕由内朝外z
        val faces = arrayOf(
            // front (z轴全为+h)
            floatArrayOf(-h, -h, +h, +h, -h, +h, +h, +h, +h, -h, +h, +h),// 左上，右上，右下，左下
            // right (x轴全为+h);前上，后上，后下，前下
            floatArrayOf(+h, -h, +h, +h, -h, -h, +h, +h, -h, +h, +h, +h),
            // back (z轴全为-h)右上，左上，左下，右下
            floatArrayOf(+h, -h, -h, -h, -h, -h, -h, +h, -h, +h, +h, -h),
            // left (x轴全为-h);后上，前上，前下，后下
            floatArrayOf(-h, -h, -h, -h, -h, +h, -h, +h, +h, -h, +h, -h),
            // top (y轴全为-h);左后，右后，右前，左前
            floatArrayOf(-h, -h, -h, +h, -h, -h, +h, -h, +h, -h, -h, +h),
            // bottom (y轴全为+h);左前，右前，右后，左后
            floatArrayOf(-h, +h, +h, +h, +h, +h, +h, +h, -h, -h, +h, -h)
        )

        for (i in 0 until 6) {// 遍历每个面
            val quad3d = faces[i]
            // 旋转 + 投影
            val dst = projected[i]
            var zSum = 0f

            for (v in 0 until 4) {// 遍历一个面的每个点
                // 获取对应点的xyz坐标
                val x = quad3d[v * 3]
                val y = quad3d[v * 3 + 1]
                val z = quad3d[v * 3 + 2]

                val p = rotateAndProject(x, y, z)
                dst[v * 2] = p.first
                dst[v * 2 + 1] = p.second

                // 这里用旋转后的 z 来估深度（在 rotateAndProject 里已经隐含了旋转）
                zSum += lastRotatedZ
            }

            faceDepth[i] = zSum / 4f
        }
    }


    // rotateAndProject 里会更新这个值（用于深度排序）
    private var lastRotatedZ = 0f

    /**
     * 旋转投影(欧拉旋转)
     */
    private fun rotateAndProject(
        x: Float,
        y: Float,
        z: Float
    ): Pair<Float, Float> {
        // 角度转弧度,sin、cos需要用弧度
        val rx = rotX * (Math.PI / 180.0)
        val ry = rotY * (Math.PI / 180.0)

        /**
         * 欧拉旋转矩阵定义(assets/img.png)：
         * 绕x旋转矩阵
         * |1,  0,      0   |
         * |0,  cosX, -sinX |
         * |0,  sinX, cosX  |
         */
        val cosX = cos(rx).toFloat()
        val sinX = sin(rx).toFloat()
        val x1 = x
        val y1 = y * cosX - z * sinX
        val z1 = y * sinX + z * cosX

        /**
         * 绕y旋转矩阵
         * |cosY,  0,   sinY|
         * | 0,    1,    0  |
         * |-sinY, 0,   cosY|
         */
        val cosY = cos(ry).toFloat()
        val sinY = sin(ry).toFloat()
        val x2 = x1 * cosY + z1 * sinY
        val y2 = y1
        val z2 = -x1 * sinY + z1 * cosY

        lastRotatedZ = z2

        // perspective projection
        // scale = persp / (persp - z)  -> z 越大（靠近相机）scale 越大
        val scale = persp / (persp - z2)
        val sx = cx + x2 * scale
        val sy = cy + y2 * scale
        return sx to sy
    }

    // 点击命中：点是否在四边形内（凸多边形）
    private fun hitTestFace(x: Float, y: Float): Int {
        var best = -1
        var bestDepth = -Float.MAX_VALUE

        for (i in 0 until 6) {
            val quad = projected[i]
            // 只允许“朝向相机”的面点击（深度较大通常就是朝向）
            // if (faceDepth[i] < 0f) continue

            if (pointInConvexQuad(x, y, quad)) {
                // 同时命中多个面时选更靠近相机的
                if (faceDepth[i] > bestDepth) {
                    bestDepth = faceDepth[i]
                    best = i
                }
            }
        }
        return best
    }

    private fun pointInConvexQuad(px: Float, py: Float, q: FloatArray): Boolean {
        // 4 个点：p0,p1,p2,p3
        fun cross(ax: Float, ay: Float, bx: Float, by: Float, cx: Float, cy: Float): Float {
            return (bx - ax) * (cy - ay) - (by - ay) * (cx - ax)
        }

        val x0 = q[0];
        val y0 = q[1]
        val x1 = q[2];
        val y1 = q[3]
        val x2 = q[4];
        val y2 = q[5]
        val x3 = q[6];
        val y3 = q[7]

        val c1 = cross(x0, y0, x1, y1, px, py)
        val c2 = cross(x1, y1, x2, y2, px, py)
        val c3 = cross(x2, y2, x3, y3, px, py)
        val c4 = cross(x3, y3, x0, y0, px, py)

        val hasNeg = (c1 < 0) || (c2 < 0) || (c3 < 0) || (c4 < 0)
        val hasPos = (c1 > 0) || (c2 > 0) || (c3 > 0) || (c4 > 0)
        return !(hasNeg && hasPos)
    }

    // 触摸：按下暂停，松手缓慢恢复；点击进入详情
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                downX = event.x; downY = event.y
                lastX = event.x; lastY = event.y
                downTime = System.currentTimeMillis()
                moved = false
                manualDragging = true
                pauseRotation()      // 自动暂停
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val dx = event.x - lastX
                val dy = event.y - lastY

                // 触摸滑动驱动旋转：横向控制 Y 轴旋转，纵向控制 X 轴旋转
                rotY = (rotY + dx * degPerPx) % 360f
                rotX = (rotX - dy * degPerPx) % 360f

                lastX = event.x
                lastY = event.y

                val adx = abs(event.x - downX)
                val ady = abs(event.y - downY)
                if (adx > touchSlopPx || ady > touchSlopPx) moved = true

                invalidate()
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                val isClick = !moved && (System.currentTimeMillis() - downTime) < 250
                if (isClick) {
                    val face = hitTestFace(event.x, event.y)
                    if (face != -1) onFaceClick?.invoke(face)
                }
                manualDragging = false
                recoverRotation()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun pauseRotation() {
        running = false
        recoverAnim?.cancel()
    }

    private fun recoverRotation() {
        recoverAnim?.cancel()
        val anim = ValueAnimator.ofFloat(0f, 1f)
        anim.duration = 450
        anim.interpolator = DecelerateInterpolator()
        anim.addUpdateListener {

        }
        anim.addListener(onEnd = {
            running = true
        })
        recoverAnim = anim
        anim.start()
    }

    // 避免写一堆 AnimatorListener
    private inline fun ValueAnimator.addListener(
        crossinline onEnd: () -> Unit
    ) {
        addListener(object : android.animation.Animator.AnimatorListener {
            override fun onAnimationStart(animation: android.animation.Animator) {}
            override fun onAnimationEnd(animation: android.animation.Animator) = onEnd()
            override fun onAnimationCancel(animation: android.animation.Animator) {}
            override fun onAnimationRepeat(animation: android.animation.Animator) {}
        })
    }
}
