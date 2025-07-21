package com.bodakesatish.sandhyasbeautyservices.ui.category.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter // Import ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bodakesatish.sandhyasbeautyservices.databinding.ListRowCategoryBinding
import com.bodakesatish.sandhyasbeautyservices.domain.model.Category

// Change from RecyclerView.Adapter to ListAdapter
class CategoryListAdapter : ListAdapter<Category, CategoryListAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    // These can remain as they are, or you can pass them via constructor if preferred
    var onClickedCategory: ((Category) -> Unit)? = null
    var onLongPressedCategory: ((Category) -> Unit)? = null

    // No longer need to manage 'itemList' manually; ListAdapter handles it.

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding =
            ListRowCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        // ListAdapter provides getItem(position) to get the current item
        val category = getItem(position)
        holder.bind(category) // Removed position from bind, not strictly needed for this logic
    }

    // Replace setData with submitList (provided by ListAdapter)
    // fun setData(data: List<Category>) {
    //    submitList(data) // Call submitList to trigger DiffUtil
    // }
    // No need for a custom setData method if you're just submitting the list.
    // The Fragment/ViewModel will call adapter.submitList(newCategories) directly.


    // Click listener setters can remain the same
    fun setOnClickListener(onClickedCategory: ((Category) -> Unit)?) {
        this.onClickedCategory = onClickedCategory
    }

    fun setOnLongClickListener(onLongPressedCategory: ((Category) -> Unit)?) {
        this.onLongPressedCategory = onLongPressedCategory
    }

    // getItemCount() is handled by ListAdapter, so you can remove your override.

    inner class CategoryViewHolder(private val binding: ListRowCategoryBinding) : // Made binding private
        RecyclerView.ViewHolder(binding.root) {

        // The 'position' parameter is often not needed in bind if click listeners refer to 'data'
        fun bind(data: Category) {
            binding.tvCategoryName.text = data.categoryName

            binding.root.setOnClickListener {
                // 'data' here is the specific category item for this ViewHolder
                onClickedCategory?.invoke(data)
            }

            binding.root.setOnLongClickListener {
                onLongPressedCategory?.invoke(data)
                true // Return true to indicate the long press was consumed
            }
        }
    }

    // Define the DiffUtil.ItemCallback
    class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            // Check if items represent the same entity, typically by a unique ID
            // Assuming 'firestoreDocId' or 'categoryName' (if unique) can serve as an ID
            // If you have a proper unique ID field in your Category model, use that.
            // For example, if Category has an 'id' field: return oldItem.id == newItem.id
            return oldItem.firestoreDocId == newItem.firestoreDocId // Or oldItem.categoryName == newItem.categoryName if names are unique
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            // Check if the visual representation of the item has changed
            // This is called only if areItemsTheSame returns true.
            return oldItem == newItem // Relies on Category being a data class or having a proper equals() implementation
        }
    }
}
