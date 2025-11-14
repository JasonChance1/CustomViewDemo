package com.example.customview.ui.base

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.viewbinding.ViewBinding
import com.example.customview.R

/**
 * @author wandervogel
 * @date 2025-10-20  星期一
 * @description
 */
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: VB
    override fun onCreate(savedInstanceState: Bundle?) {
        prepare()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = getVB()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initData()
        initViews()
        bindEvent()
    }

    abstract fun getVB(): VB

    protected open fun initViews() {}
    protected open fun bindEvent() {}
    protected open fun initData() {}
    protected open fun prepare() {}
}