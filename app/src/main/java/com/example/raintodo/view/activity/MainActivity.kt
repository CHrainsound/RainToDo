package com.example.raintodo.view.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.raintodo.R
import com.example.raintodo.repository.Resource.Status.*
import com.example.raintodo.repository.UserDatabaseHelper
import com.example.raintodo.view.adapter.ViewPager2Adapter
import com.example.raintodo.view.dialog.LoginDialog
import com.example.raintodo.viewmodel.DolistViewModel
import com.example.raintodo.viewmodel.SharedViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: DolistViewModel
    private lateinit var mvp2: ViewPager2
    private lateinit var mbnv: BottomNavigationView
    private var loginDialog: LoginDialog? = null
    private lateinit var sharedViewModel: SharedViewModel

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
//小白条沉浸
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        viewModel = ViewModelProvider(this)[DolistViewModel::class.java]

        init()
        observeLoginResult()
        observeRegisterResult()
        checkLoginStatus()

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabtn_add)
        fabAdd.setOnClickListener {
            sharedViewModel.onFabClicked()
        }
    }

    private fun checkLoginStatus() {
        if (!viewModel.isLoggedIn()) {
            showLoginDialog()
        } else {
            loadUserDataAndInit()
        }
    }

    //加载用户数据

    private fun loadUserDataAndInit() {
        val username = viewModel.getCurrentUsername()
        if (username != null) {
            val dbHelper = UserDatabaseHelper(this)
            val userId = dbHelper.getUserIdByUsername(username) ?: -1
            dbHelper.close()

            if (userId != -1) {
                sharedViewModel.init(userId)
            } else {
                viewModel.logout()
                showLoginDialog()
            }
        } else {
            showLoginDialog()
        }
    }

    private fun showLoginDialog() {
        loginDialog = LoginDialog(this)

        loginDialog?.onLoginClickListener = { username, password ->
            viewModel.login(username, password)
        }

        loginDialog?.onRegisterClickListener = { username, password ->
            viewModel.register(username, password)
        }

        loginDialog?.show()
    }

    private fun observeLoginResult() {
        viewModel.loginResult.observe(this) { resource ->
            when (resource.status) {
                SUCCESS -> {
                    if (resource.data == true) {
                        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
                        loginDialog?.onLoginSuccess()
                        loginDialog = null
                        loadUserDataAndInit()
                    }
                }

                ERROR -> {
                    Toast.makeText(this, resource.message ?: "登录失败", Toast.LENGTH_SHORT).show()
                }

                else -> {
                }
            }
        }
    }

    private fun observeRegisterResult() {
        viewModel.registerResult.observe(this) { resource ->
            when (resource.status) {
                SUCCESS -> {
                    if (resource.data == true) {
                        Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show()
                        loginDialog?.onRegisterSuccess()
                    }
                }

                ERROR -> {
                    Toast.makeText(this, resource.message ?: "注册失败", Toast.LENGTH_SHORT).show()
                }

                else -> {
                }
            }
        }
    }

    fun init() {
        mvp2 = findViewById<ViewPager2>(R.id.vp_main)
        mvp2.adapter = ViewPager2Adapter(this)

        mbnv = findViewById<BottomNavigationView>(R.id.bnv_tab)
        mbnv.itemActiveIndicatorColor = ColorStateList.valueOf(Color.TRANSPARENT)

        mbnv.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.item_main -> {
                    mvp2.setCurrentItem(0, false)
                    true
                }

                R.id.item_my -> {
                    mvp2.setCurrentItem(1, false)
                    true
                }

                else -> false
            }
        }

        mvp2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                mbnv.selectedItemId = when (position) {
                    0 -> R.id.item_main
                    1 -> R.id.item_my
                    else -> R.id.item_main
                }
            }
        })

        mvp2.isUserInputEnabled = false
    }
}