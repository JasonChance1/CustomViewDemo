package com.example.customview.ui.activities
import com.example.customview.databinding.ActivitySearchBinding
import com.example.customview.ui.base.BaseActivity

/**
 * @author wandervogel
 * @date 2025-10-31  星期五
 * @description
 */
class SearchViewActivity : BaseActivity<ActivitySearchBinding>() {
    override fun getVB() = ActivitySearchBinding.inflate(layoutInflater)

    override fun initViews() {
        binding.btnStop.setOnClickListener { binding.searchView.stopSearch() }
        binding.btnStart.setOnClickListener { binding.searchView.startSearch() }
    }
}