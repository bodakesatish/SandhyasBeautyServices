package com.bodakesatish.sandhyasbeautyservices.util

import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.widget.ListPopupWindow
import com.bodakesatish.sandhyasbeautyservices.R

object AppListPopupWindow {

    fun showListPopupWindow(anchorView: View,arrayAdapter: ArrayAdapter<*>, onItemSelected: (Int) -> Unit) {
        val listPopupWindow =
            ListPopupWindow(anchorView.context, null, R.attr.listPopupWindowStyle)
        listPopupWindow.setAdapter(arrayAdapter)
        listPopupWindow.anchorView = anchorView
        listPopupWindow.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
            onItemSelected(position)
            listPopupWindow.dismiss()
        }
        listPopupWindow.show()
    }

}