package com.example.ime_prac

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.core.view.allViews

class SimpleFewIME : InputMethodService() {

    override fun onCreateInputView(): View {
        // 처음에 자동 작성된 코드는 그대로 리턴.
//        return layoutInflater.inflate(R.layout.keyboard, null)

        // #1 변수에 담기
//        val keyboard = layoutInflater.inflate(R.layout.keyboard, null)
//        keyboard.findViewById<Button>(R.id.key_11).setOnClickListener {  }
//        return keyboard
        // 참고로, 뷰바인딩 방식도 있으나, IME에 못 쓰는 듯함.

        // #2 apply나 also를 쓰는 것. -> 채택
        return layoutInflater.inflate(R.layout.keyboard, null).apply {
            val onClickListener: (View) -> Unit = {
                (it as Button).let { button ->
                    currentInputConnection.apply {
                        commitText(button.text, 1)
                    }
                }
            }
//            findViewById<Button>(R.id.key_11).setOnClickListener(onClickListener)
//            findViewById<Button>(R.id.key_12).setOnClickListener(onClickListener)
//            findViewById<Button>(R.id.key_13).setOnClickListener(onClickListener)
//            findViewById<Button>(R.id.key_14).setOnClickListener(onClickListener)
//            findViewById<Button>(R.id.key_15).setOnClickListener(onClickListener)
//            findViewById<Button>(R.id.key_13).setOnClickListener(onClickListener)
            allViews.forEach {
                Log.d("!!!!", "$it")
                if (it is Button) {
                    it.setOnClickListener(onClickListener)
                }
            }
        }
    }

}