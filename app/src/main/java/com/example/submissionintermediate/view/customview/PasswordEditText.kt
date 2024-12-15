package com.example.submissionintermediate.view.customview

import android.content.Context
import android.graphics.Canvas
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD


class PasswordEditText @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatEditText(context, attrs) {
    init {
        // Set input type to password
        inputType = TYPE_CLASS_TEXT or TYPE_TEXT_VARIATION_PASSWORD

        // Add a text watcher for validation
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Do nothing
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Show error if password is less than 8 characters
                if (s.toString().length < 8) {
                    setError("Password tidak boleh kurang dari 8 karakter", null)
                } else {
                    error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
                // Do nothing
            }
        })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Set hint for the EditText
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    }
}
