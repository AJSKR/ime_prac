package com.example.ime_prac

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.IBinder
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodSubtype
import android.widget.Button
import android.widget.FrameLayout
import androidx.core.view.allViews

class SimpleFewIME : InputMethodService() {
    private var currText = ""

    private val spinJaum: Map<String, List<String>>
    private val lookUpSpinJaum: Map<String, String>

    private var shiftState = ShiftState.NONE

    init {
        spinJaum = mapOf(
            "ㄱ" to listOf("ㄱ", "ㅋ", "ㄲ")
        )
        lookUpSpinJaum = mapOf(
            "ㄱ" to "ㄱ",
            "ㅋ" to "ㄱ",
            "ㄲ" to "ㄱ"
        )
    }

    private fun CharSequence.isJaum(): Boolean = toString().let {
        // "ㄱㄴㄷㄹㅁㅂㅅㅇㅊㅋㅌㅍㅎㅃㅉㄸㄲㅆ"
        it >= "ㄱ" && it <= "ㅎ"
    }

    private fun CharSequence.isAlphaNumeric(): Boolean = toString().let {
        ("A" <= it && it <= "Z") || ("a" <= it && it <= "z") || ("0" <= it && it <= "9")
    }

    private fun CharSequence.isAlphabet(): Boolean = toString().let {
        ("A" <= it && it <= "Z") || ("a" <= it && it <= "z")
    }

    override fun onCreateInputView(): View {
        Log.d("!!!!", "onCreateInputView")
        // 처음 자동 작성된 코드: 그대로 리턴.
//        return layoutInflater.inflate(R.layout.keyboard, null)

        // #1 변수에 담기
//        val keyboard = layoutInflater.inflate(R.layout.keyboard, null)
//        keyboard.findViewById<Button>(R.id.key_11).setOnClickListener {  }
//        return keyboard
        // 참고로, 뷰바인딩 방식도 있으나, IME에 못 쓰는 듯함.

        // #2 apply나 also를 쓰는 것. -> 채택
        return FrameLayout(this@SimpleFewIME).also {
//            setupSehum(it)
            setupQwerty(it)
        }
    }

    private fun setupQwerty(root: FrameLayout) {
        root.removeAllViews()
        val layoutId = R.layout.kb_qwerty
        layoutInflater.inflate(layoutId, root).apply {
            val onEngClick: (View) -> Unit = {
                (it as Button).let { button ->
                    currentInputConnection.apply {
                        //                        commitText(button.text, 1)
                        val pressedKey = button.text
                        when {
                            pressedKey.isAlphaNumeric() -> {
//                                currText += pressedKey
//                                currText += (pressedKey.toString())
                                when (shiftState) {
                                    ShiftState.NONE -> commitText(pressedKey, 1)
                                    ShiftState.CAPSLOCK -> {
                                        commitText(pressedKey, 1)
                                    }

                                    ShiftState.OT_SHIFT -> {
                                        commitText(pressedKey, 1)
                                        shiftState = ShiftState.NONE
                                        invalidateQuertyLayout(root)
                                    }
                                }
                            }
                        }
//                        setComposingText(currText, 1)
                    }
                }
            } //onKorClick
            findViewById<Button>(R.id.switch_kor).setOnClickListener {
                setupSehum(root)
            }
            val excludedIds = setOf(
                R.id.switch_kor, R.id.switch_sym, R.id.shift, R.id.backspace, R.id.delete,
                R.id.space,
            )
            allViews.forEach {
                Log.d("!!!!", "$it")
                if (it is Button && !excludedIds.contains(it.id)) {
                    it.setOnClickListener(onEngClick)
                }

            }

            val specialKeyHandler: (Int) -> Unit = {
                currentInputConnection.setComposingText(currText, 1)
                currText = ""
                keyDownUp(it)
            }

            findViewById<Button>(R.id.space).setOnClickListener {
                specialKeyHandler(KeyEvent.KEYCODE_SPACE)
            }

            findViewById<Button>(R.id.delete).setOnClickListener {
                specialKeyHandler(KeyEvent.KEYCODE_FORWARD_DEL)
            }

            findViewById<Button>(R.id.tab).setOnClickListener {
                specialKeyHandler(KeyEvent.KEYCODE_TAB)
            }

            findViewById<Button>(R.id.backspace).setOnClickListener {
                val currTextLen = currText.length
                Log.d("!!!!", "currTextLen: $currTextLen")
                when {
                    currTextLen > 1 -> {
                        currText = currText.dropLast(1)
                        currentInputConnection.setComposingText(currText, 1)
                    }

                    currTextLen > 0 -> {
                        currText = ""
                        currentInputConnection.commitText("", 0)
                    }

                    else -> keyDownUp(KeyEvent.KEYCODE_DEL)
                }
            }
            findViewById<Button>(R.id.shift).apply {
                when (shiftState) {
                    ShiftState.NONE -> null
                    ShiftState.OT_SHIFT -> 0xFF00FF00
                    ShiftState.CAPSLOCK -> 0xFFFF0000
                }?.let {
                    setTextColor(it.toInt())
//                    setBackgroundColor(it.toInt())
                    // TODO: 버튼 틴트를 처리해야 함.
                }

                setOnClickListener {
                    shiftState = when (shiftState) {
                        ShiftState.NONE -> ShiftState.OT_SHIFT
                        ShiftState.OT_SHIFT -> ShiftState.CAPSLOCK
                        ShiftState.CAPSLOCK -> ShiftState.NONE
                    }
                    invalidateQuertyLayout(root)
                }
            }
        }
    }

    private fun invalidateQuertyLayout(root: FrameLayout) {
        root.allViews.forEach { it ->
            Log.d(TAG, "${(it as? Button)?.text} $shiftState")
            when {
                it is Button && it.text.length == 1 && it.text.isAlphabet() -> {
                    val charOnKey = it.text.toString()
                    it.text = if (shiftState == ShiftState.NONE) {
                        charOnKey.lowercase()
                    } else {
                        charOnKey.uppercase()
                    }
                    Log.d("!!!!", "${it.text}")
                }
                it.id == R.id.shift -> {
                    (it as Button).apply {
                        when (shiftState) {
                            ShiftState.NONE -> 0xFF000000
                            ShiftState.OT_SHIFT -> 0xFF00FF00
                            ShiftState.CAPSLOCK -> 0xFFFF0000
                        }.let {
                            setTextColor(it.toInt())
                            //                    setBackgroundColor(it.toInt())
                            // TODO: 버튼 틴트를 처리해야 함.
                        }
                    }
                }
            }
        }
    }

    private fun keyDownUp(targetKey: Int) {
        currentInputConnection.apply {
            sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, targetKey))
            sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, targetKey))
        }
    }

    private fun setupSehum(root: FrameLayout) {
        root.removeAllViews()
        layoutInflater.inflate(R.layout.kb_sehum, root).apply {
            val onKorClick: (View) -> Unit = {
                (it as Button).let { button ->
                    currentInputConnection.apply {
                        //                        commitText(button.text, 1)
                        val pressedKey = button.text
                        Log.d("!!!!", "pressedKey: $pressedKey")
                        Log.d("!!!!", "isJaum: ${pressedKey.isJaum()}")
                        when {
                            pressedKey.isJaum() -> {
                                val lastChar = currText.lastOrNull()
                                Log.d("!!!!", "lastChar: $lastChar")

                                if (lastChar == null) {
                                    currText += pressedKey
                                    setComposingText(currText, 1)
                                } else {
                                    R.id.backspace
                                    if (lastChar.toString() in lookUpSpinJaum) {
                                        val left = currText.dropLast(1)
                                        val list = lookUpSpinJaum[lastChar.toString()]!!
                                        val index = (list.indexOf(lastChar) + 1) % list.length
                                        val spinJaum = list[index]
                                        Log.d("!!!!", "$left $index $spinJaum")
                                        currText = left + spinJaum
                                        Log.d("!!!!", "currtext: $currText")
                                        setComposingText(currText, 1)
                                    } else {
                                        currText += pressedKey
                                        setComposingText(currText, 1)
                                    }
                                }
//                                commitText(pressedKey, 1)
                                spinJaum
                                currText += (pressedKey.toString())
                            }
                        }
                        Log.d("!!!!", "text: $currText")
                        setComposingText(currText, 1)
                    }
                }
            } //onKorClick

            val excludedIds = setOf(R.id.switch_eng, R.id.switch_sym, R.id.backspace)
            allViews.forEach {
                Log.d("!!!!", "$it")
                if (it is Button && !excludedIds.contains(it.id)) {
                    it.setOnClickListener(onKorClick)
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
                // composing text
                val currTextLen = currText.length
                Log.d("!!!!", "currTextLen: $currTextLen")
                when {
                    currTextLen > 1 -> {
                        currText = currText.dropLast(1)
                        currentInputConnection.setComposingText(currText, 1)
                    }

                    currTextLen > 0 -> {
                        currText = ""
                        currentInputConnection.commitText("", 0)
                    }

                    else -> {
                        currentInputConnection.apply {
                            Log.d("!!!!", "enter keyDownUp")
                            sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_DEL))
                            sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_DEL))
                        }
                    }
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

        currText = ""

        Log.d("!!!!", "onStartInput")
        Log.d("!!!!", "" + attribute?.inputType)

        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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

        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
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
        Log.d(TAG, "onCurrentInputMethodSubtypeChanged")
        Log.d(TAG, "" + newSubtype?.languageTag)
        Log.d(TAG, "" + newSubtype?.mode)
        Log.d(TAG, "" + newSubtype?.toString())
    }

    companion object {
        val TAG = SimpleFewIME::class.simpleName
    }
}

enum class ShiftState {
    NONE,
    OT_SHIFT,
    CAPSLOCK
}