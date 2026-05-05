package com.example.raintodo.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.raintodo.R
import com.example.raintodo.adapter.TodoListAdapter
import com.example.raintodo.repository.TodoItem
import com.example.raintodo.repository.TodoList
import com.example.raintodo.viewmodel.SharedViewModel

class MainFragment : Fragment() {

    // 手动初始化 ViewModel
    private lateinit var sharedViewModel: SharedViewModel

    // 使用 TodoList 数据模型
    private val todoLists = ArrayList<TodoList>()
    private lateinit var todoListAdapter: TodoListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        // 手动获取与 Activity 共享的 ViewModel
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        // 初始化 RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.rv_list)

        // 使用 TodoListAdapter 替代原来的 TaskAdapter
        todoListAdapter = TodoListAdapter(
            todoLists = todoLists,
            onCheckChanged = { todoList, isChecked ->
                // 更新 TodoList 的完成状态
                sharedViewModel.updateTodoListStatus(todoList.id, isChecked)
            },
            onTitleChanged = { todoList, newTitle ->
                // 更新 TodoList 的标题
                sharedViewModel.updateTodoListTitle(todoList.id, newTitle)
            },
            onItemContentChanged = { todoList, position, newContent ->
                // 更新子项内容
                val item = todoList.items[position]
                sharedViewModel.updateTodoItemContent(item.id, newContent)
            },
            onItemCheckChanged = { todoList, position, isChecked ->
                // 更新子项完成状态
                val item = todoList.items[position]
                sharedViewModel.updateTodoItemStatus(item.id, isChecked)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = todoListAdapter

        // 观察 ViewModel 中的点击事件（加号按钮）
        sharedViewModel.addItemClick.observe(viewLifecycleOwner) {
            // MainActivity 里的加号按钮被点击时，添加新的待办清单
            addNewTodoList()
        }

        // 观察 ViewModel 中的待办清单数据变化
        sharedViewModel.todoLists.observe(viewLifecycleOwner) { newTodoLists ->
            todoListAdapter.submitList(newTodoLists)
        }

        return view
    }

    /**
     * 添加新的待办清单
     */
    private fun addNewTodoList() {
        val newTodoList = TodoList(
            id = System.currentTimeMillis().toInt(), // 临时 ID
            userId = sharedViewModel.currentUserId,
            title = "",
            isCompleted = false,
            items = mutableListOf(
                TodoItem(
                    id = System.currentTimeMillis().toInt() + 1,
                    todoId = System.currentTimeMillis().toInt(),
                    content = "",
                    isCompleted = false
                )
            ),
            createdAt = "2026-05-05",
            updatedAt = "2026-05-05"
        )

        todoLists.add(newTodoList)
        todoListAdapter.notifyItemInserted(todoLists.size - 1)

        // 滚动到最新项
        val recyclerView = requireView().findViewById<RecyclerView>(R.id.rv_list)
        recyclerView.smoothScrollToPosition(todoLists.size - 1)

        // 同步到 ViewModel
        sharedViewModel.addTodoList(newTodoList)
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}