package com.example.customview.ui.activities

import com.example.customview.databinding.ActivityLuckyBinding
import com.example.customview.ui.base.BaseActivity

/**
 * @author wandervogel
 * @date 2025-10-31  星期五
 * @description
 */
class LuckyActivity :BaseActivity<ActivityLuckyBinding>() {
    override fun getVB() = ActivityLuckyBinding .inflate(layoutInflater)

    override fun initViews() {
        super.initViews()
        binding.btnStart.setOnClickListener { binding.lucky.startSpin {  } }
    }
}