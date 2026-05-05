package com.example.raintodo.repository

data class TodoList(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val isCompleted: Boolean = false,
    val items: List<TodoItem> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = ""
)

data class TodoItem(
    val id: Int = 0,
    val todoId: Int,
    val content: String,
    val isCompleted: Boolean = false,
    val createdAt: String = ""
)
