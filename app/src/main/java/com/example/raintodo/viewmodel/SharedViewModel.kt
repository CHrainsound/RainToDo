package com.example.raintodo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.raintodo.repository.TodoItem
import com.example.raintodo.repository.TodoList

class SharedViewModel : ViewModel() {

    // 当前用户 ID
    var currentUserId: Int = 1

    // 传递点击事件的 LiveData
    private val _addItemClick = MutableLiveData<Unit>()
    val addItemClick: LiveData<Unit> get() = _addItemClick

    // 待办清单列表数据
    private val _todoLists = MutableLiveData<List<TodoList>>(emptyList())
    val todoLists: LiveData<List<TodoList>> get() = _todoLists

    /**
     * MainActivity 的加号按钮被点击时调用
     */
    fun onFabClicked() {
        _addItemClick.value = Unit
    }

    /**
     * 添加新的待办清单
     */
    fun addTodoList(todoList: TodoList) {
        val currentList = _todoLists.value?.toMutableList() ?: mutableListOf()
        currentList.add(todoList)
        _todoLists.value = currentList
    }

    /**
     * 更新 TodoList 的完成状态
     */
    fun updateTodoListStatus(todoListId: Int, isCompleted: Boolean) {
        val currentList = _todoLists.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == todoListId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isCompleted = isCompleted)
            _todoLists.value = currentList
        }
    }

    /**
     * 更新 TodoList 的标题
     */
    fun updateTodoListTitle(todoListId: Int, title: String) {
        val currentList = _todoLists.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == todoListId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(title = title)
            _todoLists.value = currentList
        }
    }

    /**
     * 更新 TodoItem 的内容
     */
    fun updateTodoItemContent(itemId: Int, content: String) {
        val currentList = _todoLists.value?.toMutableList() ?: return
        for (i in currentList.indices) {
            val todoList = currentList[i]
            val itemIndex = todoList.items.indexOfFirst { it.id == itemId }
            if (itemIndex != -1) {
                val newItems = todoList.items.toMutableList()
                newItems[itemIndex] = newItems[itemIndex].copy(content = content)
                currentList[i] = todoList.copy(items = newItems)
                _todoLists.value = currentList
                break
            }
        }
    }

    /**
     * 更新 TodoItem 的完成状态
     */
    fun updateTodoItemStatus(itemId: Int, isCompleted: Boolean) {
        val currentList = _todoLists.value?.toMutableList() ?: return
        for (i in currentList.indices) {
            val todoList = currentList[i]
            val itemIndex = todoList.items.indexOfFirst { it.id == itemId }
            if (itemIndex != -1) {
                val newItems = todoList.items.toMutableList()
                newItems[itemIndex] = newItems[itemIndex].copy(isCompleted = isCompleted)
                currentList[i] = todoList.copy(items = newItems)
                _todoLists.value = currentList
                break
            }
        }
    }
}