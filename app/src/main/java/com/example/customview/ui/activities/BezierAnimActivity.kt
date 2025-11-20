package com.example.customview.ui.activities

import com.example.customview.databinding.ActivityBezierAnimBinding
import com.example.customview.ui.base.BaseActivity

/**
 * @author wandervogel
 * @date 2025-11-20  星期四
 * @description
 */
class BezierAnimActivity :BaseActivity<ActivityBezierAnimBinding>() {
    override fun getVB()= ActivityBezierAnimBinding.inflate(layoutInflater)
}