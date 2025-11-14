package com.example.customview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.customview.databinding.ItemViewSampleBinding
import com.example.customview.entity.ViewSample

/**
 * @author wandervogel
 * @date 2025-10-20  星期一
 * @description
 */
class ViewAdapter(val samples: List<ViewSample>) : RecyclerView.Adapter<ViewAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemViewSampleBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        ItemViewSampleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
    )

    override fun getItemCount() = samples.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position in samples.indices) {
            samples[position].let { sample ->
                holder.binding.root.addView(sample.view, 0)

                holder.binding.tvDes.text = sample.des
            }
        }
    }
}