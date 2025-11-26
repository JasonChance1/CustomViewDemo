package com.example.customview.widiget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.withStyledAttributes
import androidx.core.widget.NestedScrollView
import com.example.customview.R
import com.example.customview.utils.dp
import kotlin.math.max
import kotlin.math.min

class DragResizeContainer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val scrollView: NestedScrollView
    private val innerContentContainer: FrameLayout
    private val dragHandle: ImageView

    private var maxHeightRatio: Float = 0.6f
    private var minHeightPx: Int = 20.dp
    private var maxHeightPx: Int

    /** 内容达到多少高度才允许拖动，默认 200dp */
    private var draggableHeightPx: Int = 200.dp

    private val handleHeightPx: Int = 24.dp

    private var enableDrag: Boolean = false

    private var downY: Float = 0f
    private var startHeight: Int = 0

    init {
        context.withStyledAttributes(attrs, R.styleable.DragResizeContainer) {
            maxHeightRatio = getFloat(
                R.styleable.DragResizeContainer_drc_maxHeightRatio,
                0.8f
            )
            minHeightPx = getDimensionPixelSize(
                R.styleable.DragResizeContainer_drc_minHeight,
                20.dp
            )
        }

        val dm = resources.displayMetrics
        maxHeightPx = (dm.heightPixels * maxHeightRatio).toInt()

        scrollView = NestedScrollView(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
            )
            isFillViewport = true
            clipToPadding = false
            setPadding(
                paddingLeft,
                paddingTop,
                paddingRight,
                paddingBottom + handleHeightPx
            )
        }

        innerContentContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
        }
        scrollView.addView(innerContentContainer)

        dragHandle = ImageView(context).apply {
            layoutParams = LayoutParams(
                40.dp,
                handleHeightPx
            ).apply {
                gravity = android.view.Gravity.BOTTOM or android.view.Gravity.CENTER_HORIZONTAL
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
//            setBackgroundResource(android.R.drawable.drag)
            setImageResource(R.drawable.drag)
            visibility = View.GONE
        }

        super.addView(scrollView)
        super.addView(dragHandle)

        // ==== 手柄拖动：禁用水平滑动 ====
        dragHandle.setOnTouchListener { v, event ->
            if (!enableDrag) return@setOnTouchListener false

            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downY = event.rawY
                    startHeight = layoutParams?.height
                        ?.takeIf { it > 0 }
                        ?: height

                    // 告诉父布局不要拦截（比如 ViewPager 的横向滑动）
                    v.parent?.requestDisallowInterceptTouchEvent(true)
                    true
                }

                MotionEvent.ACTION_MOVE -> {
                    val dy = event.rawY - downY
                    var newHeight = (startHeight + dy).toInt()
                    newHeight = max(minHeightPx, min(maxHeightPx, newHeight))

                    layoutParams = (layoutParams ?: LayoutParams(
                        LayoutParams.MATCH_PARENT,
                        newHeight
                    )).apply {
                        height = newHeight
                    }
                    true
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    // 恢复父布局拦截行为
                    v.parent?.requestDisallowInterceptTouchEvent(false)
                    true
                }

                else -> false
            }
        }

        // 初次布局完成后计算一次
        viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                recalculateHeightInternal()
                viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        // 0: scrollView, 1: dragHandle, 2...N: XML 中的子 View
        while (childCount > 2) {
            val child = getChildAt(2)
            removeViewAt(2)
            innerContentContainer.addView(child)
        }
    }

    /** WebView 加载完成等情况时调用 */
    fun recalculateHeight() {
        post { recalculateHeightInternal() }
    }

    // ==== “可拖动默认收起到 80dp” 的逻辑在这里 ====
    private fun recalculateHeightInternal() {
        val contentHeight = innerContentContainer.height

        if (contentHeight > draggableHeightPx) {
            enableDrag = true
            dragHandle.visibility = View.VISIBLE

            // 默认高度 = 可拖动高度（比如 80dp），再夹在[min,max]之间
            val collapsed = draggableHeightPx.coerceIn(minHeightPx, maxHeightPx)
            layoutParams = (layoutParams ?: LayoutParams(
                LayoutParams.MATCH_PARENT,
                collapsed
            )).apply {
                height = collapsed
            }
        } else {
            enableDrag = false
            dragHandle.visibility = View.GONE

            layoutParams = (layoutParams ?: LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )).apply {
                height = LayoutParams.WRAP_CONTENT
            }
        }
    }

    // ============ 对外设置方法 ============

    fun setMaxHeightRatio(ratio: Float) {
        if (ratio <= 0f || ratio > 1f) return
        maxHeightRatio = ratio
        val dm = resources.displayMetrics
        maxHeightPx = (dm.heightPixels * maxHeightRatio).toInt()
    }

    fun setMinHeightDp(dp: Int) {
        minHeightPx = dp.dp
    }

    /** 设置“可拖动默认高度”（dp），默认 80dp */
    fun setDraggableHeightDp(dp: Int) {
        draggableHeightPx = dp.dp
    }
}
