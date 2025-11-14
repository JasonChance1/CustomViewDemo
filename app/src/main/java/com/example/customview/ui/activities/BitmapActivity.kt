package com.example.customview.ui.activities
import com.example.customview.databinding.ActivityBitmapBinding
import com.example.customview.ui.base.BaseActivity

/**
 * @author wandervogel
 * @date 2025-10-31  星期五
 * @description
 */
class BitmapActivity : BaseActivity<ActivityBitmapBinding>() {
    override fun getVB() = ActivityBitmapBinding.inflate(layoutInflater)

    override fun initViews() {
    }
}