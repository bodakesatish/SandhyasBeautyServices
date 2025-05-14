package com.bodakesatish.sandhyasbeautyservices.extension

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService

fun View.hideKeyboard() {
    val inputManager =
        this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(this.windowToken, 0)
}

fun View.showKeyboard() {
    if (this.requestFocus()) {
        this.postDelayed({
            val inputMethodManager =
                this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }, 100) // Small delay to allow view to settle
    }
//    val inputManager =
//        this.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//    inputManager.toggleSoftInput(
//        InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY
//    )
}