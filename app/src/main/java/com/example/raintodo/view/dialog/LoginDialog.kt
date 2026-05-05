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

    // 登录回调：传入用户名和密码
    var onLoginClickListener: ((String, String) -> Unit)? = null

    // 注册回调：传入用户名和密码
    var onRegisterClickListener: ((String, String) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 加载自定义布局
        setContentView(R.layout.login)

        // 初始化控件
        initViews()

        // 设置背景透明
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 设置密码可见性切换
        eyeicon()

        // 设置按钮点击事件
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
        // 登录按钮点击事件
        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // 触发登录回调
            onLoginClickListener?.invoke(username, password)

            // 注意：不要在这里 dismiss，等 ViewModel 回调结果再决定是否关闭
        }

        // 注册按钮点击事件
        btnSignup.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // 触发注册回调
            onRegisterClickListener?.invoke(username, password)

            // 注意：不要在这里 dismiss，等 ViewModel 回调结果再决定是否关闭
        }
    }

    /**
     * 登录成功后关闭 Dialog
     */
    fun onLoginSuccess() {
        dismiss()
    }

    /**
     * 注册成功后提示
     */
    fun onRegisterSuccess() {
        // 清空输入
        etUsername.text?.clear()
        etPassword.text?.clear()
        // 提示用户登录
        Toast.makeText(context, "注册成功，请登录", Toast.LENGTH_SHORT).show()
    }

    /**
     * 显示错误信息
     */
    fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}