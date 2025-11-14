package com.example.customview.widiget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.View
import com.example.customview.R

/**
 * @author wandervogel
 * @date 2025-10-31  星期五
 * @description
 */
class BitmapAnim @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val maxPage = 22                // 总帧数
    private val duration = 1000             // 总动画时长（ms）
    private val frameDelay = duration / maxPage  // 每帧间隔时间
    private var curPage = 0                 // 当前帧索引

    // 解码整张序列帧图
    private val bitmap: Bitmap = BitmapFactory.decodeResource(context.resources, R.mipmap.zombie)

    // 图片共两排，每排 11 帧
    private val singleWidth: Int = bitmap.width / 11
    private val singleHeight: Int = bitmap.height / 2
    private val srcRect = Rect()
    private val desRect = Rect()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val mHandler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            invalidate() // 触发绘制
            curPage = (curPage + 1) % maxPage // 循环帧数
            sendEmptyMessageDelayed(0, frameDelay.toLong())
        }
    }

    init {

        // 启动动画
        mHandler.sendEmptyMessage(0)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.translate(width / 2f - singleWidth / 2f, height / 2f - singleHeight / 2f)
        drawImg(canvas)
        canvas.restore()
    }

    private fun drawImg(canvas: Canvas) {
        // 根据帧号计算行列
        val col = curPage % 11
        val row = curPage / 11

        // 设置源区域（从大图取哪一块）
        srcRect.set(
            col * singleWidth,
            row * singleHeight,
            (col + 1) * singleWidth,
            (row + 1) * singleHeight
        )

        // 目标绘制区域（按原尺寸显示）
        desRect.set(0, 0, singleWidth, singleHeight)

        // 绘制该帧
        canvas.drawBitmap(bitmap, srcRect, desRect, paint)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // 停止动画消息，释放资源
        mHandler.removeCallbacksAndMessages(null)
        if (!bitmap.isRecycled) {
            bitmap.recycle()
        }
    }
}
