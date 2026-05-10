// UserRepository.kt
package com.example.raintodo.repository

import android.content.Context
import android.content.SharedPreferences

class UserRepository(context: Context) {
    private val dbHelper = UserDatabaseHelper(context)
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USERNAME = "current_username"
    }

    //注册新用户
    fun register(username: String, password: String): Resource<Boolean> {
        return try {
            //校验
            if (username.length < 3) {
                return Resource.error("用户名至少3个字符", false)
            }
            if (password.length < 6) {
                return Resource.error("密码至少6个字符", false)
            }

            //检查用户名
            if (dbHelper.isUsernameExists(username)) {
                return Resource.error("用户名已存在", false)
            }

            //注册用户
            val success = dbHelper.registerUser(username, password)
            if (success) {
                Resource.success(true)
            } else {
                Resource.error("注册失败，请重试", false)
            }
        } catch (e: Exception) {
            Resource.error("注册异常：${e.message}", false)
        }
    }

    //用户登录
    fun login(username: String, password: String): Resource<Boolean> {
        return try {
            //校验
            if (username.isEmpty()) {
                return Resource.error("请输入用户名", false)
            }
            if (password.isEmpty()) {
                return Resource.error("请输入密码", false)
            }

            // 验证登录
            val success = dbHelper.loginUser(username, password)
            if (success) {
                //保存登录状态
                saveLoginState(username)
                Resource.success(true)
            } else {
                Resource.error("用户名或密码错误", false)
            }
        } catch (e: Exception) {
            Resource.error("登录异常：${e.message}", false)
        }
    }

    //保存登录状态
    private fun saveLoginState(username: String) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USERNAME, username)
        editor.apply()
    }

    //检查是否已登录
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    //获取当前登录用户名
    fun getCurrentUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    //退出登录
    fun logout() {
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
    }
}