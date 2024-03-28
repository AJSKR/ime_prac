package com.example.ime_prac

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodSubtype
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.view.allViews

class SimpleFewIME : InputMethodService() {

    override fun onCreateInputView(): View {
        Log.d("!!!!", "onCreateInputView")
        // 처음에 자동 작성된 코드는 그대로 리턴.
//        return layoutInflater.inflate(R.layout.keyboard, null)

        // #1 변수에 담기
//        val keyboard = layoutInflater.inflate(R.layout.keyboard, null)
//        keyboard.findViewById<Button>(R.id.key_11).setOnClickListener {  }
//        return keyboard
        // 참고로, 뷰바인딩 방식도 있으나, IME에 못 쓰는 듯함.

        // #2 apply나 also를 쓰는 것. -> 채택
        return FrameLayout(this@SimpleFewIME).also {
            setupCjk(it)
        }
    }

    private fun setupQwerty(root: FrameLayout) {
        root.removeAllViews()
        layoutInflater.inflate(R.layout.kb_qwerty, root).apply {
            findViewById<Button>(R.id.switch_eng).setOnClickListener {
                setupCjk(root)
            }
        }
    }

    private fun setupCjk(root: FrameLayout) {
        root.removeAllViews()
        layoutInflater.inflate(R.layout.keyboard, root).apply {
            var a = ""
            val onAlphanumClick: (View) -> Unit = {
                (it as Button).let { button ->
                    currentInputConnection.apply {
    //                        commitText(button.text, 1)
                        a += (button.text.toString())
                        Log.d("!!!!", "text: $a")
                        setComposingText(a, 1)
                    }
                }
            }

            val excludedIds = setOf(R.id.switch_eng, R.id.switch_sym, R.id.backspace)
            allViews.forEach {
                Log.d("!!!!", "$it")
                if (it is Button && !excludedIds.contains(it.id)) {
                    it.setOnClickListener(onAlphanumClick)
                }
//                else if (it.tag == "1A") {
//                    it.setOnClickListener {
    //                        val intent = Intent(this@SimpleFewIME, SettingsActivity::class.java).apply {
    //                            flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
    //                        }
//                        val intent = Intent().apply {
//                            action = Intent.ACTION_MAIN
//                            addCategory(Intent.CATEGORY_LAUNCHER)
//                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                            component =
//                                ComponentName(this@SimpleFewIME, SettingsActivity::class.java)
//                        }
//                        this@SimpleFewIME.startActivity(intent)
//                        setupQwerty(root)
//                    }
//                }
            }
            findViewById<Button>(R.id.switch_eng).setOnClickListener {
                setupQwerty(root)
//                switchToNextInputMethod(true)

    //                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    //                inputMethodManager.switchToNextInputMethod(token, false)
            }
            findViewById<Button>(R.id.backspace).setOnClickListener {
                a = a.dropLast(1)
                currentInputConnection.apply {
                    setComposingText(a, 1)
                }
            }
        }
    }

    val token: IBinder?
        get() {
            return (window.window ?: return null).attributes.token
        }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)

        Log.d("!!!!", "onStartInput")
        Log.d("!!!!", "" + attribute?.inputType)

        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        Log.d("!!!!", "mode: ${inputMethodManager.currentInputMethodSubtype?.mode}")
        Log.d("!!!!", "lanTag: ${inputMethodManager.currentInputMethodSubtype?.languageTag}")

        inputMethodManager.inputMethodList.forEach {
            Log.d("!!!!", "pn ${it.packageName}")
            Log.d("!!!!", "sn ${it.serviceName}")
            Log.d("!!!!", "it ${it}")
            Log.d("!!!!", "id ${it.id}")

            if (it.serviceName == "com.example.ime_prac.SimpleFewIME") {
                inputMethodManager.getEnabledInputMethodSubtypeList(it, true).forEach {
                    Log.d("!!!!", "mode: ${it.mode}")
                    Log.d("!!!!", "lanTag: ${it.languageTag}")
                }
            }
        }
    }

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)

        Log.d("!!!!", "onStartInputView")
        Log.d("!!!!", "" + editorInfo?.inputType)

        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        Log.d("!!!!", "mode: ${inputMethodManager.currentInputMethodSubtype?.mode}")
        Log.d("!!!!", "lanTag: ${inputMethodManager.currentInputMethodSubtype?.languageTag}")
    }

    override fun onInitializeInterface() {
        super.onInitializeInterface()
        Log.d("!!!!", "onInitializeInterface")
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("!!!!", "onCreate")
    }

    override fun onCurrentInputMethodSubtypeChanged(newSubtype: InputMethodSubtype?) {
        super.onCurrentInputMethodSubtypeChanged(newSubtype)
        Log.d("!!!!", "onCurrentInputMethodSubtypeChanged")
        Log.d("!!!!", "" + newSubtype?.languageTag)
        Log.d("!!!!", "" + newSubtype?.mode)
        Log.d("!!!!", "" + newSubtype?.toString())
    }
}