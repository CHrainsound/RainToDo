package com.example.raintodo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.raintodo.repository.TodoItem
import com.example.raintodo.repository.TodoList
import com.example.raintodo.repository.TodoRepository

class SharedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TodoRepository(application)

    var currentUserId: Int = 0
        private set

    private val _addItemClick = MutableLiveData<Unit>()
    val addItemClick: LiveData<Unit> get() = _addItemClick

    private val _todoLists = MutableLiveData<List<TodoList>>(emptyList())
    val todoLists: LiveData<List<TodoList>> get() = _todoLists

    fun onFabClicked() {
        _addItemClick.value = Unit
    }

    fun init(userId: Int) {
        currentUserId = userId
        loadTodoLists()
    }

    fun loadTodoLists() {
        Thread {
            val lists = repository.getTodosByUserId(currentUserId)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                _todoLists.value = lists
            }
        }.start()
    }

    fun addTodoList(title: String = "", callback: ((Int) -> Unit)? = null) {
        Thread {
            val newId = repository.addTodo(currentUserId, title)
            if (newId != -1L) {
                val newList = TodoList(
                    id = newId.toInt(),
                    userId = currentUserId,
                    title = title,
                    isCompleted = false,
                    items = emptyList(),
                    createdAt = "",
                    updatedAt = ""
                )
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    val currentList = _todoLists.value?.toMutableList() ?: mutableListOf()
                    currentList.add(0, newList)
                    _todoLists.value = currentList
                    callback?.invoke(newId.toInt())
                }
            }
        }.start()
    }

    fun updateTodoListTitle(todoListId: Int, title: String) {
        updateLocalListTitle(todoListId, title)
        Thread { repository.updateTodoTitle(todoListId, title) }.start()
    }

    fun updateTodoListStatus(todoListId: Int, isCompleted: Boolean) {
        updateLocalListStatus(todoListId, isCompleted)
        Thread { repository.toggleTodoStatus(todoListId, isCompleted) }.start()
    }

    fun addTodoItem(todoListId: Int, content: String = "", callback: ((Int) -> Unit)? = null) {
        Thread {
            val newId = repository.addTodoItem(todoListId, content)
            if (newId != -1L) {
                val newItem = TodoItem(
                    id = newId.toInt(),
                    todoId = todoListId,
                    content = content,
                    isCompleted = false,
                    createdAt = ""
                )
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    addItemToLocalList(todoListId, newItem)
                    callback?.invoke(newId.toInt())
                }
            }
        }.start()
    }

    fun updateTodoItemContent(itemId: Int, content: String, todoListId: Int) {
        updateLocalItemContent(todoListId, itemId, content)
        Thread { repository.updateTodoItem(itemId, content) }.start()
    }

    fun updateTodoItemStatus(itemId: Int, isCompleted: Boolean, todoListId: Int) {
        updateLocalItemStatus(todoListId, itemId, isCompleted)
        Thread {
            repository.toggleTodoItemStatus(itemId, isCompleted)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                updateTodoListStatusBasedOnItems(todoListId)
            }
        }.start()
    }

    private fun updateTodoListStatusBasedOnItems(todoListId: Int) {
        val currentList = _todoLists.value ?: return
        val todoList = currentList.find { it.id == todoListId } ?: return
        if (todoList.items.isEmpty()) {
            if (todoList.isCompleted) updateTodoListStatus(todoListId, false)
            return
        }
        val allCompleted = todoList.items.all { it.isCompleted }
        if (todoList.isCompleted != allCompleted) {
            updateTodoListStatus(todoListId, allCompleted)
        }
    }

    fun deleteTodoItem(itemId: Int, todoListId: Int) {
        removeItemFromLocalList(todoListId, itemId)
        Thread {
            repository.deleteTodoItem(itemId)
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                updateTodoListStatusBasedOnItems(todoListId)
            }
        }.start()
    }

    fun deleteTodoList(todoListId: Int) {
        val currentList = _todoLists.value?.toMutableList() ?: return
        currentList.removeAll { it.id == todoListId }
        _todoLists.value = currentList
        Thread { repository.deleteTodo(todoListId) }.start()
    }

    private fun updateLocalListStatus(todoListId: Int, isCompleted: Boolean) {
        val currentList = _todoLists.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == todoListId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(isCompleted = isCompleted)
            _todoLists.value = currentList
        }
    }

    private fun updateLocalListTitle(todoListId: Int, title: String) {
        val currentList = _todoLists.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == todoListId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(title = title)
            _todoLists.value = currentList
        }
    }

    private fun updateLocalItemStatus(todoListId: Int, itemId: Int, isCompleted: Boolean) {
        val currentList = _todoLists.value?.toMutableList() ?: return
        val listIndex = currentList.indexOfFirst { it.id == todoListId }
        if (listIndex != -1) {
            val todoList = currentList[listIndex]
            val newItems = todoList.items.map {
                if (it.id == itemId) it.copy(isCompleted = isCompleted) else it
            }
            currentList[listIndex] = todoList.copy(items = newItems)
            _todoLists.value = currentList
        }
    }

    private fun updateLocalItemContent(todoListId: Int, itemId: Int, content: String) {
        val currentList = _todoLists.value?.toMutableList() ?: return
        val listIndex = currentList.indexOfFirst { it.id == todoListId }
        if (listIndex != -1) {
            val todoList = currentList[listIndex]
            val newItems = todoList.items.map {
                if (it.id == itemId) it.copy(content = content) else it
            }
            currentList[listIndex] = todoList.copy(items = newItems)
            _todoLists.value = currentList
        }
    }

    private fun addItemToLocalList(todoListId: Int, newItem: TodoItem) {
        val currentList = _todoLists.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == todoListId }
        if (index != -1) {
            val newItems = currentList[index].items.toMutableList()
            newItems.add(newItem)
            currentList[index] = currentList[index].copy(items = newItems)
            _todoLists.value = currentList.toList()
        }
    }

    private fun removeItemFromLocalList(todoListId: Int, itemId: Int) {
        val currentList = _todoLists.value?.toMutableList() ?: return
        val listIndex = currentList.indexOfFirst { it.id == todoListId }
        if (listIndex != -1) {
            val newItems = currentList[listIndex].items.filter { it.id != itemId }
            currentList[listIndex] = currentList[listIndex].copy(items = newItems)
            _todoLists.value = currentList
        }
    }

    fun moveToTop(todoListId: Int) {
        val currentList = _todoLists.value?.toMutableList() ?: return
        val index = currentList.indexOfFirst { it.id == todoListId }
        if (index > 0) {
            val item = currentList.removeAt(index)
            currentList.add(0, item)
            _todoLists.value = currentList
        }
    }

    fun clearData() {
        _todoLists.value = emptyList()
        currentUserId = 0
    }
}