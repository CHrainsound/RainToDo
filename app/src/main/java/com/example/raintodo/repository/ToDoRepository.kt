// TodoRepository.kt
package com.example.raintodo.repository

import android.content.Context

class TodoRepository(context: Context) {
    private val dbHelper = UserDatabaseHelper(context)

    /**
     * 获取指定用户的所有待办清单
     * 直接复用 Helper 中已写好的逻辑，避免 Cursor 映射错误
     */
    fun getTodosByUserId(userId: Int): List<TodoList> {
        return dbHelper.getTodosByUserId(userId)
    }

    /**
     * 添加待办清单
     * 注意：这里只传 title，Helper 会自动处理 user_id 和默认状态
     */
    fun addTodo(userId: Int, title: String): Long {
        return dbHelper.addTodo(userId, title)
    }

    /**
     * 更新待办清单标题
     * 注意：Helper 中的方法名为 updateTodoTitle
     */
    fun updateTodoTitle(id: Int, title: String): Int {
        return dbHelper.updateTodoTitle(id, title)
    }

    /**
     * 删除待办清单
     * 注意：Helper 会自动级联删除明细事项
     */
    fun deleteTodo(id: Int): Int {
        return dbHelper.deleteTodo(id)
    }

    /**
     * 切换清单的完成状态
     * 注意：Helper 中的方法名为 toggleTodoStatus
     */
    fun toggleTodoStatus(id: Int, isCompleted: Boolean): Int {
        return dbHelper.toggleTodoStatus(id, isCompleted)
    }

    // --- 事项 (Items) 相关操作 ---
    // 你的 Helper 中已经包含了事项的操作，Repository 应该暴露这些接口

    fun addTodoItem(todoId: Int, content: String): Long {
        return dbHelper.addTodoItem(todoId, content)
    }

    fun updateTodoItem(id: Int, content: String): Int {
        return dbHelper.updateTodoItem(id, content)
    }

    fun toggleTodoItemStatus(id: Int, isCompleted: Boolean): Int {
        return dbHelper.toggleTodoItemStatus(id, isCompleted)
    }

    fun deleteTodoItem(id: Int): Int {
        return dbHelper.deleteTodoItem(id)
    }
}