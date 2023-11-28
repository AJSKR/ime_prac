package com.example.ime_prac

import android.inputmethodservice.InputMethodService
import android.view.View

class FastInputIME : InputMethodService() {

    override fun onCreateInputView(): View {
        return layoutInflater.inflate(androidx.appcompat.R.layout.abc_action_bar_title_item, null)
    }

}