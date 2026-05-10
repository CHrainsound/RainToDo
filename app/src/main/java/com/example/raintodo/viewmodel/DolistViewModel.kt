package com.example.raintodo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.raintodo.repository.*

class DolistViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository(application)

    private val _loginResult = MutableLiveData<Resource<Boolean>>()
    val loginResult: LiveData<Resource<Boolean>> = _loginResult

    private val _registerResult = MutableLiveData<Resource<Boolean>>()
    val registerResult: LiveData<Resource<Boolean>> = _registerResult

    private val _currentUser = MutableLiveData<String?>()
    val currentUser: LiveData<String?> = _currentUser

    private val _currentUserId = MutableLiveData<Int?>()
    val currentUserId: LiveData<Int?> = _currentUserId

    init {
        if (userRepository.isLoggedIn()) {
            _currentUser.value = userRepository.getCurrentUsername()
        }
    }

    fun login(username: String, password: String) {
        Thread {
            try {
                val result = userRepository.login(username, password)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    if (result.data == true) {
                        _currentUser.value = username
                        //获取并保存 userId
                        val dbHelper = UserDatabaseHelper(getApplication())
                        val userId = dbHelper.getUserIdByUsername(username)
                        dbHelper.close()
                        _currentUserId.value = userId
                    }
                    _loginResult.value = result
                }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    _loginResult.value = Resource.error("登录异常：${e.message}", false)
                }
            }
        }.start()
    }

    fun register(username: String, password: String) {
        Thread {
            try {
                val result = userRepository.register(username, password)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    _registerResult.value = result
                }
            } catch (e: Exception) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    _registerResult.value = Resource.error("注册异常：${e.message}", false)
                }
            }
        }.start()
    }

    fun logout() {
        userRepository.logout()
        _currentUser.value = null
        _currentUserId.value = null
        _loginResult.value = Resource.success(false)
    }

    fun isLoggedIn(): Boolean {
        return userRepository.isLoggedIn()
    }

    fun getCurrentUsername(): String? {
        return _currentUser.value
    }

    //获取当前用户ID
    fun getCurrentUserId(): Int? {
        return _currentUserId.value
    }
}