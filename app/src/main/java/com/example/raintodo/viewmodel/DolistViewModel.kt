// DolistViewModel.kt
package com.example.raintodo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.raintodo.repository.*

class DolistViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository(application)

    // 登录状态
    private val _loginResult = MutableLiveData<Resource<Boolean>>()
    val loginResult: LiveData<Resource<Boolean>> = _loginResult

    // 注册状态
    private val _registerResult = MutableLiveData<Resource<Boolean>>()
    val registerResult: LiveData<Resource<Boolean>> = _registerResult

    // 当前用户
    private val _currentUser = MutableLiveData<String?>()
    val currentUser: LiveData<String?> = _currentUser

    init {
        // 初始化时检查登录状态
        if (userRepository.isLoggedIn()) {
            _currentUser.value = userRepository.getCurrentUsername()
        }
    }

    /**
     * 用户登录
     */
    fun login(username: String, password: String) {
        // 1. 先校验输入
        if (username.isEmpty()) {
            _loginResult.value = Resource.error("请输入用户名", false)
            return
        }
        if (password.isEmpty()) {
            _loginResult.value = Resource.error("请输入密码", false)
            return
        }


        // 3. 在后台线程执行登录
        Thread {
            try {
                val result = userRepository.login(username, password)

                // 4. 回到主线程更新 UI
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    _loginResult.value = result
                }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    _loginResult.value = Resource.error("登录异常：${e.message}", false)
                }
            }
        }.start()
    }

    /**
     * 用户注册
     */
    fun register(username: String, password: String) {
        // 1. 先校验输入
        if (username.isEmpty()) {
            _registerResult.value = Resource.error("请输入用户名", false)
            return
        }
        if (username.length < 3) {
            _registerResult.value = Resource.error("用户名至少3个字符", false)
            return
        }
        if (password.isEmpty()) {
            _registerResult.value = Resource.error("请输入密码", false)
            return
        }
        if (password.length < 6) {
            _registerResult.value = Resource.error("密码至少6个字符", false)
            return
        }
        // 3. 在后台线程执行注册
        Thread {
            try {
                val result = userRepository.register(username, password)

                // 4. 回到主线程更新 UI
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    _registerResult.value = Resource.success(true)
                }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    _registerResult.value = Resource.error("注册异常：${e.message}", false)
                }
            }
        }.start()
    }

    /**
     * 退出登录
     */
    fun logout() {
        userRepository.logout()
        _currentUser.value = null
        _loginResult.value = Resource.success(false)
    }

    /**
     * 检查是否已登录
     */
    fun isLoggedIn(): Boolean {
        return userRepository.isLoggedIn()
    }

    /**
     * 获取当前用户名
     */
    fun getCurrentUsername(): String? {
        return _currentUser.value
    }
}