package com.example.customview.ui.activities

import com.example.customview.databinding.ActivityGomokuBinding
import com.example.customview.ui.base.BaseActivity
import com.example.customview.utils.toast
import com.example.customview.widiget.GomokuView

/**
 * @author wandervogel
 * @date 2026-02-02  星期一
 * @description
 */
class GomokuActivity : BaseActivity<ActivityGomokuBinding>() {
    override fun getVB() = ActivityGomokuBinding.inflate(layoutInflater)

    override fun initViews() {
        super.initViews()
        binding.gomoku.apply {
            onGameOver = {
                val winner = when (it) {
                    GomokuView.BLACK -> "黑棋"
                    else -> "白棋"
                }
                "${winner}胜".toast(this@GomokuActivity)
            }
        }
        binding.btnReset.setOnClickListener { binding.gomoku.reset() }
        binding.btnUndo.setOnClickListener { binding.gomoku.undo() }
    }
}