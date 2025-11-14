package com.example.customview.ui.activities

import com.example.customview.databinding.ActivityPieBinding
import com.example.customview.entity.PieData
import com.example.customview.ui.base.BaseActivity

/**
 * @author wandervogel
 * @date 2025-10-22  星期三
 * @description
 */
class PieActivity : BaseActivity<ActivityPieBinding>() {
    override fun getVB() = ActivityPieBinding.inflate(layoutInflater)

    override fun initData() {
        val dataList = listOf(
            PieData("数据1", 125f),
            PieData("数据2", 24.25f),
            PieData("数据3", 24.25f),
            PieData("数据4", 24.25f),
            PieData("数据5", 24.25f),
        )
        binding.pieView.setData(dataList)
    }
}