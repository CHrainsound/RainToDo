package com.example.raintodo.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.raintodo.R
import com.example.raintodo.adapter.TodoListAdapter
import com.example.raintodo.viewmodel.SharedViewModel

class MainFragment : Fragment() {

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var todoListAdapter: TodoListAdapter
    private lateinit var recyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_main, container, false)

        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        recyclerView = view.findViewById(R.id.rv_list)

        todoListAdapter = TodoListAdapter(
            todoLists = emptyList(),
            onCheckChanged = { todoList, isChecked ->
                sharedViewModel.updateTodoListStatus(todoList.id, isChecked)
            },
            onTitleChanged = { todoList, newTitle ->
                sharedViewModel.updateTodoListTitle(todoList.id, newTitle)
            },
            onItemContentChanged = { todoList, itemId, newContent ->
                sharedViewModel.updateTodoItemContent(itemId, newContent, todoList.id)
            },
            onItemCheckChanged = { todoList, itemId, isChecked ->
                sharedViewModel.updateTodoItemStatus(itemId, isChecked, todoList.id)
            },
            onAddNewItem = { todoList ->
                sharedViewModel.addTodoItem(todoList.id, "")
            },
            onDeleteTodoList = { todoList ->
                sharedViewModel.deleteTodoList(todoList.id)
            },
            onTopTodoList = { todoList ->
                sharedViewModel.moveToTop(todoList.id)
            },
            onDeleteTodoItem = { todoList, itemId ->
                sharedViewModel.deleteTodoItem(itemId, todoList.id)
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = todoListAdapter

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    todoListAdapter.closeOpenItem()
                }
            }
        })

        sharedViewModel.addItemClick.observe(viewLifecycleOwner) {
            addNewTodoList()
        }

        sharedViewModel.todoLists.observe(viewLifecycleOwner) { newTodoLists ->
            todoListAdapter.closeOpenItem()
            todoListAdapter.submitList(newTodoLists)
            todoListAdapter.refreshAll()
        }

        return view
    }

    private fun addNewTodoList() {
        sharedViewModel.addTodoList("") {
            recyclerView.post {
                recyclerView.smoothScrollToPosition(0)
                todoListAdapter.focusFirstItem()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}