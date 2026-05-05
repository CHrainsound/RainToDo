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

        // 1. 先设置沉浸式（在 setContentView 之前）
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. 初始化 ViewModel
        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        viewModel = ViewModelProvider(this)[DolistViewModel::class.java]

        // 3. 初始化视图
        init()

        // 4. 观察登录结果
        observeLoginResult()

        // 5. 观察注册结果
        observeRegisterResult()

        // 6. 检查登录状态，未登录才弹出 Dialog
        checkLoginStatus()

        val fabAdd = findViewById<FloatingActionButton>(R.id.fabtn_add)
        fabAdd.setOnClickListener {
            // 按钮被点击，通知 ViewModel
            sharedViewModel.onFabClicked()
        }
    }

    /**
     * 检查登录状态
     */
    private fun checkLoginStatus() {
        if (!viewModel.isLoggedIn()) {
            showLoginDialog()
        } else {
            // 已登录，加载数据
           // viewModel.loadAllDolists()
        }
    }

    /**
     * 显示登录 Dialog
     */
    private fun showLoginDialog() {
        loginDialog = LoginDialog(this)

        // 先设置回调，再显示
        loginDialog?.onLoginClickListener = { username, password ->
            viewModel.login(username, password)
        }

        loginDialog?.onRegisterClickListener = { username, password ->
            viewModel.register(username, password)
        }

        loginDialog?.show()
    }

    /**
     * 观察登录结果
     */
    private fun observeLoginResult() {
        viewModel.loginResult.observe(this) { resource ->
            when (resource.status) {
                SUCCESS -> {
                    if (resource.data == true) {
                        Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show()
                        // 关闭 Dialog
                        loginDialog?.onLoginSuccess()
                        loginDialog = null
                        // 刷新数据
                      //  viewModel.loadAllDolists()
                    }
                }
                ERROR -> {
                    Toast.makeText(this, resource.message ?: "登录失败", Toast.LENGTH_SHORT).show()
                }
                LOADING -> {
                    Toast.makeText(this,"loading", Toast.LENGTH_SHORT).show()
                }
                null -> {
                    Toast.makeText(this,"aoanao", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 观察注册结果
     */
    private fun observeRegisterResult() {
        viewModel.registerResult.observe(this) { resource ->
            when (resource.status) {
                SUCCESS -> {
                    if (resource.data == true) {
                        Toast.makeText(this, "注册成功，请登录", Toast.LENGTH_SHORT).show()
                        // 注册成功后清空输入，让用户登录
                        loginDialog?.onRegisterSuccess()
                    }
                }
                ERROR -> {
                    Toast.makeText(this, resource.message ?: "注册失败", Toast.LENGTH_SHORT).show()
                }
                LOADING -> {
                    Toast.makeText(this, resource.message ?: "loading", Toast.LENGTH_SHORT).show()
                }
                null -> {
                    Toast.makeText(this, resource.message ?: "null.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun init() {
        mvp2 = findViewById<ViewPager2>(R.id.vp_main)
        mvp2.adapter = ViewPager2Adapter(this)

        mbnv = findViewById<BottomNavigationView>(R.id.bnv_tab)
        mbnv.itemActiveIndicatorColor = ColorStateList.valueOf(Color.TRANSPARENT)

        // 只保留一个监听器（推荐使用 setOnItemSelectedListener）
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

        // ViewPager2 页面切换同步导航栏
        mvp2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                mbnv.selectedItemId = when (position) {
                    0 -> R.id.item_main
                    1 -> R.id.item_my
                    else -> R.id.item_main
                }
            }
        })

        // 禁止滑动切换
        mvp2.isUserInputEnabled = false
    }
}