package com.example.customview.ui.activities

import com.example.customview.databinding.ActivityPieBinding
import com.example.customview.entity.PieData
import com.example.customview.ui.base.BaseActivity
import kotlin.random.Random

/**
 * @author wandervogel
 * @date 2025-10-22  星期三
 * @description
 */
class PieActivity : BaseActivity<ActivityPieBinding>() {
    override fun getVB() = ActivityPieBinding.inflate(layoutInflater)

    override fun initData() {
        val dataList = listOf(
            PieData("数据一", Random.nextFloat()),
            PieData("数据二", Random.nextFloat()),
            PieData("数据三", Random.nextFloat()),
            PieData("数据四", Random.nextFloat()),
            PieData("数据五", Random.nextFloat()),
        )
        binding.pieView.setData(dataList)
    }
}