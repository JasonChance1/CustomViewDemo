package com.example.customview.ui.activities

import android.os.Handler
import android.os.Looper
import com.example.customview.databinding.ActivityLoadingBinding
import com.example.customview.ui.base.BaseActivity
import kotlin.random.Random

/**
 * @author wandervogel
 * @date 2025-11-14  星期五
 * @description
 */
class LeafLoadingActivity : BaseActivity<ActivityLoadingBinding>() {
    private val handler = Handler(Looper.getMainLooper()) { msg ->
        if (msg.what == 1) {
            val addProgress = Random.nextInt(5)
            val progress = binding.loading.mProgress.toInt() + addProgress
            binding.loading.setProgress(progress)
            updateProgress()
        }
        true
    }

    override fun initData() {
        super.initData()
        updateProgress()
    }

    override fun getVB() = ActivityLoadingBinding.inflate(layoutInflater)

    private fun updateProgress() {
        if (binding.loading.mProgress < 100) {
            handler.sendEmptyMessageDelayed(1, 200)
        }
    }
}