package com.example.customview.ui

import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.customview.adapter.ViewAdapter
import com.example.customview.databinding.ActivityBasicBinding
import com.example.customview.entity.ViewSample
import com.example.customview.ui.base.BaseActivity
import com.example.customview.widiget.basic.DrawArcView
import com.example.customview.widiget.basic.DrawColorView
import com.example.customview.widiget.basic.DrawOvalView
import com.example.customview.widiget.basic.DrawRectView

/**
 * @author wandervogel
 * @date 2025-10-20  星期一
 * @description
 */
class BasicActivity : BaseActivity<ActivityBasicBinding>() {
    private val options = mutableListOf<ViewSample>()
    override fun getVB()= ActivityBasicBinding .inflate(layoutInflater)

    override fun initData() {
        options.apply {
            add(ViewSample(DrawColorView(this@BasicActivity),"drawColor"))
            add(ViewSample(DrawRectView(this@BasicActivity),"rect"))
            add(ViewSample(DrawRectView(this@BasicActivity,true),"roundRect"))
            add(ViewSample(DrawOvalView(this@BasicActivity),"drawOval"))
            add(ViewSample(DrawArcView(this@BasicActivity),"DrawArcView"))
        }
    }

    override fun initViews() {
        binding.rvOptions.apply {
            layoutManager = GridLayoutManager(this@BasicActivity,3)
            adapter = ViewAdapter(options)
        }
    }
}