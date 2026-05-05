// TodoRepository.kt
package com.example.raintodo.repository

import android.content.Context

class TodoRepository(context: Context) {
    private val dbHelper = UserDatabaseHelper(context)

    fun getTodosByUserId(userId: Int): List<TodoList> {
        return dbHelper.getTodosByUserId(userId)
    }

    fun addTodo(userId: Int, title: String): Long {
        return dbHelper.addTodo(userId, title)
    }

    fun updateTodoTitle(id: Int, title: String): Int {
        return dbHelper.updateTodoTitle(id, title)
    }

    fun deleteTodo(id: Int): Int {
        return dbHelper.deleteTodo(id)
    }

    fun toggleTodoStatus(id: Int, isCompleted: Boolean): Int {
        return dbHelper.toggleTodoStatus(id, isCompleted)
    }

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