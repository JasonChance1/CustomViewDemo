package com.example.customview.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.customview.databinding.ItemOptionBinding

class OptionAdapter(val options: List<String>) : RecyclerView.Adapter<OptionAdapter.ViewHolder>() {
    var onItemClickListener: ((String) -> Unit) = {}

    inner class ViewHolder(val mBinding: ItemOptionBinding) :
        RecyclerView.ViewHolder(mBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemOptionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount() = options.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position in options.indices) {
            options[position].let{option->
                holder.mBinding.tvContent.text = option
                holder.mBinding.root.setOnClickListener {
                    onItemClickListener.invoke(option)
                }
            }
        }
    }
}