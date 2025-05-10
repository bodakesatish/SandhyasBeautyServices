package com.bodakesatish.sandhyasbeautyservices.ui.category.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.databinding.ListRowCategoryBinding
import com.bodakesatish.sandhyasbeautyservices.databinding.ListRowCustomerBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category
import com.bodakesatish.sandhyasbeautyservices.domain.model.Customer

class CategoryListAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var itemList: List<Category> = emptyList()
    var onBatchSelected: ((Category) -> Unit)? = null

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

    fun setOnClickListener(onBatchSelected: ((Category)) -> Unit) {
        this.onBatchSelected = onBatchSelected
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class CategoryViewHolder(val binding: ListRowCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(data: Category, position: Int) {

            binding.tvCategoryName.text = "${position+1}. ${data.categoryName + 1}"

            binding.root.setOnClickListener {
                onBatchSelected?.invoke(data)
            }
        }

    }
}