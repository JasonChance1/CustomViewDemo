package com.example.customview.utils

import android.content.res.Resources
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView

import androidx.annotation.ColorInt
import com.example.customview.widiget.SpacingItemDecoration

/**
 * 为RecyclerView添加项间距的扩展方法
 * @param spacingDp 间距大小（dp）,默认12dp
 * @param color 间距颜色,默认透明
 * @param orientation 布局方向, 默认垂直
 * @param includeEdge 是否在首尾添加间距（默认false）
 */
fun RecyclerView.addItemSpacing(
    spacingDp: Int = 12,
    @ColorInt color: Int = Color.TRANSPARENT,
    orientation: Int = RecyclerView.VERTICAL,
    includeEdge: Boolean = false
) {
    addItemDecoration(SpacingItemDecoration(spacingDp.dp, color, orientation, includeEdge))
}


val Number.dp: Int
    get() = (this.toFloat() * Resources.getSystem().displayMetrics.density + 0.5f).toInt()


val Number.fdp: Float
    get() = this.toFloat() * Resources.getSystem().displayMetrics.density