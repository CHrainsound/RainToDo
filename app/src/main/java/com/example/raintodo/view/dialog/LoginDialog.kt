package com.example.raintodo.view.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.raintodo.R
import com.google.android.material.textfield.TextInputLayout

class LoginDialog(context: Context) : Dialog(context) {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var mTialteyeicon: TextInputLayout
    private lateinit var btnLogin: Button
    private lateinit var btnSignup: Button

    // 登录回调
    var onLoginClickListener: ((String, String) -> Unit)? = null

    // 注册回调
    var onRegisterClickListener: ((String, String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        initViews()
        // 设置dialog背景透明
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //密码可见性
        eyeicon()
        setupClickListeners()
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }

    private fun initViews() {
        etUsername = findViewById(R.id.et_main_username)
        etPassword = findViewById(R.id.et_main_password)
        mTialteyeicon = findViewById(R.id.lt_main_eyeicon)
        btnLogin = findViewById(R.id.btn_main_login)
        btnSignup = findViewById(R.id.btn_main_signUp)
    }

    private fun eyeicon() {
        mTialteyeicon.setEndIconOnClickListener { v: View? ->
            val selection: Int = etPassword.selectionEnd
            if (etPassword.inputType == (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD)
            }
            etPassword.setSelection(selection)
        }
    }

    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // 登录回调
            onLoginClickListener?.invoke(username, password)
        }
        btnSignup.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            //注册回调
            onRegisterClickListener?.invoke(username, password)

        }
    }

    fun onLoginSuccess() {
        dismiss()
    }

    fun onRegisterSuccess() {
        etUsername.text?.clear()
        etPassword.text?.clear()
        Toast.makeText(context, "注册成功，请登录", Toast.LENGTH_SHORT).show()
    }

    fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}