package com.example.raintodo.repository

/**
 * 待办清单（包含标题、完成状态和明细事项列表）
 */
data class TodoList(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val isCompleted: Boolean = false,
    val items: List<TodoItem> = emptyList(),
    val createdAt: String = "",
    val updatedAt: String = ""
)

/**
 * 待办清单下的明细事项
 */
data class TodoItem(
    val id: Int = 0,
    val todoId: Int,
    val content: String,
    val isCompleted: Boolean = false,
    val createdAt: String = ""
)
