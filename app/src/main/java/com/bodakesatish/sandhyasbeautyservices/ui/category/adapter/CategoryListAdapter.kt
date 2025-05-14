package com.bodakesatish.sandhyasbeautyservices.ui.category.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.databinding.ListRowCategoryBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category

class CategoryListAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemList: List<Category> = emptyList()
    var onClickedCategory: ((Category) -> Unit)? = null
    var onLongPressedCategory: ((Category) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding =
            ListRowCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {

            is CategoryViewHolder -> {
                holder.bind(itemList[position], position)
            }

        }
    }

    fun setData(data: List<Category>) {
        itemList = data
        notifyItemRangeChanged(0, data.size)
    }

    fun setOnClickListener(onClickedCategory: ((Category)) -> Unit) {
        this.onClickedCategory = onClickedCategory
    }

    fun setOnLongClickListener(onLongPressedCategory: ((Category)) -> Unit) {
        this.onLongPressedCategory = onLongPressedCategory
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class CategoryViewHolder(val binding: ListRowCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Category, position: Int) {

            binding.tvCategoryName.text = data.categoryName

            binding.root.setOnClickListener {
                onClickedCategory?.invoke(data)
            }

            binding.root.setOnLongClickListener {
                onLongPressedCategory?.invoke(data)
                true
            }
        }

    }
}