package com.example.customview.ui.activities
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.customview.R
import com.example.customview.databinding.ActivityCubicAlbumBinding
import com.example.customview.ui.base.BaseActivity
import com.example.customview.utils.toast

/**
 * @author wandervogel
 * @date 2025-10-31  星期五
 * @description
 */
class CubeAlbumActivity : BaseActivity<ActivityCubicAlbumBinding>() {
    private val bitmapList = mutableListOf<Bitmap>()
    override fun getVB() = ActivityCubicAlbumBinding.inflate(layoutInflater)

    override fun initViews() {
        bitmapList.add(BitmapFactory.decodeResource(resources, R.mipmap.img_1))
        bitmapList.add(BitmapFactory.decodeResource(resources, R.mipmap.img_2))
        bitmapList.add(BitmapFactory.decodeResource(resources, R.mipmap.img_3))
        bitmapList.add(BitmapFactory.decodeResource(resources, R.mipmap.img_4))
        bitmapList.add(BitmapFactory.decodeResource(resources, R.mipmap.img_5))
        bitmapList.add(BitmapFactory.decodeResource(resources, R.mipmap.img_6))

        binding.cubeAlbumView.setFaces(bitmapList)
        binding.cubeAlbumView.onFaceClick = {index->
            "点击第${index}面".toast(this)
        }
    }
}