package com.example.customview.widiget

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView

class SpacingItemDecoration(
    private val spacing: Int,
    @ColorInt private val color: Int,
    private val orientation: Int,
    private val includeEdge: Boolean
) : RecyclerView.ItemDecoration() {

    private val paint = Paint().apply {
        this.color = color
        style = Paint.Style.FILL
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val position = parent.getChildAdapterPosition(view)
        if (position == RecyclerView.NO_POSITION) return

        val itemCount = state.itemCount

        when (orientation) {
            RecyclerView.VERTICAL -> {
                if (includeEdge) {
                    // 垂直布局 - 包含首尾间距
                    when (position) {
                        0 -> outRect.set(0, spacing, 0, spacing / 2) // 第一个项
                        itemCount - 1 -> outRect.set(0, spacing / 2, 0, spacing) // 最后一个项
                        else -> outRect.set(0, spacing / 2, 0, spacing / 2) // 中间项
                    }
                } else {
                    // 垂直布局 - 不包含首尾间距
                    if (position < itemCount - 1) {
                        outRect.set(0, 0, 0, spacing)
                    }
                }
            }
            RecyclerView.HORIZONTAL -> {
                if (includeEdge) {
                    // 水平布局 - 包含首尾间距
                    when (position) {
                        0 -> outRect.set(spacing, 0, spacing / 2, 0) // 第一个项
                        itemCount - 1 -> outRect.set(spacing / 2, 0, spacing, 0) // 最后一个项
                        else -> outRect.set(spacing / 2, 0, spacing / 2, 0) // 中间项
                    }
                } else {
                    // 水平布局 - 不包含首尾间距
                    if (position < itemCount - 1) {
                        outRect.set(0, 0, spacing, 0)
                    }
                }
            }
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)

        when (orientation) {
            RecyclerView.VERTICAL -> drawVerticalDividers(c, parent)
            RecyclerView.HORIZONTAL -> drawHorizontalDividers(c, parent)
        }
    }

    private fun drawVerticalDividers(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        if (childCount < 2) return

        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val nextChild = parent.getChildAt(i + 1)

            val dividerTop = child.bottom + (spacing / 2)
            val dividerBottom = nextChild.top - (spacing / 2)

            // 绘制间距区域
            c.drawRect(
                parent.paddingLeft.toFloat(),
                dividerTop.toFloat(),
                (parent.width - parent.paddingRight).toFloat(),
                dividerBottom.toFloat(),
                paint
            )
        }

        // 绘制首尾间距（如果包含）
        if (includeEdge) {
            val firstChild = parent.getChildAt(0)
            val lastChild = parent.getChildAt(childCount - 1)

            // 顶部间距
            c.drawRect(
                parent.paddingLeft.toFloat(),
                (parent.paddingTop).toFloat(),
                (parent.width - parent.paddingRight).toFloat(),
                (firstChild.top - spacing / 2).toFloat(),
                paint
            )

            // 底部间距
            c.drawRect(
                parent.paddingLeft.toFloat(),
                (lastChild.bottom + spacing / 2).toFloat(),
                (parent.width - parent.paddingRight).toFloat(),
                (parent.height - parent.paddingBottom).toFloat(),
                paint
            )
        }
    }

    private fun drawHorizontalDividers(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        if (childCount < 2) return

        for (i in 0 until childCount - 1) {
            val child = parent.getChildAt(i)
            val nextChild = parent.getChildAt(i + 1)

            val dividerLeft = child.right + (spacing / 2)
            val dividerRight = nextChild.left - (spacing / 2)

            // 绘制间距区域
            c.drawRect(
                dividerLeft.toFloat(),
                parent.paddingTop.toFloat(),
                dividerRight.toFloat(),
                (parent.height - parent.paddingBottom).toFloat(),
                paint
            )
        }

        // 绘制首尾间距（如果包含）
        if (includeEdge) {
            val firstChild = parent.getChildAt(0)
            val lastChild = parent.getChildAt(childCount - 1)

            // 左侧间距
            c.drawRect(
                parent.paddingLeft.toFloat(),
                parent.paddingTop.toFloat(),
                (firstChild.left - spacing / 2).toFloat(),
                (parent.height - parent.paddingBottom).toFloat(),
                paint
            )

            // 右侧间距
            c.drawRect(
                (lastChild.right + spacing / 2).toFloat(),
                parent.paddingTop.toFloat(),
                (parent.width - parent.paddingRight).toFloat(),
                (parent.height - parent.paddingBottom).toFloat(),
                paint
            )
        }
    }
}