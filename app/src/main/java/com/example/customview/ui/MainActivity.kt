package com.example.customview.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.customview.adapter.OptionAdapter
import com.example.customview.databinding.ActivityMainBinding
import com.example.customview.ui.base.BaseActivity
import com.example.customview.ui.activities.BitmapActivity
import com.example.customview.ui.activities.OtherActivity
import com.example.customview.ui.activities.PieActivity
import com.example.customview.ui.activities.ReverseClockActivity
import com.example.customview.ui.activities.ClockActivity
import com.example.customview.utils.addItemSpacing

class MainActivity : BaseActivity<ActivityMainBinding>() {
    private val options = listOf(
        "基础", "饼图", "时钟", "反方向的钟", "所有帧放在一张图实现动画"
    )

    override fun getVB() = ActivityMainBinding.inflate(layoutInflater)

    override fun initViews() {
        binding.rvOptions.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = OptionAdapter(options).apply {
                onItemClickListener = {
                    onItemClick(it)
                }
            }
            addItemSpacing()
        }
    }

    private fun onItemClick(type: String) {
        val target: Class<out AppCompatActivity> = when (type) {
            "基础" -> BasicActivity::class.java
            "饼图" -> PieActivity::class.java
            "时钟" -> ClockActivity::class.java
            "反方向的钟" -> ReverseClockActivity::class.java
            "所有帧放在一张图实现动画" -> BitmapActivity::class.java
            else -> OtherActivity::class.java
        }
        val intent = Intent(this, target)
        startActivity(intent)

    }
}