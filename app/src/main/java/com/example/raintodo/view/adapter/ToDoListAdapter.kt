package com.example.raintodo.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.raintodo.R
import com.example.raintodo.repository.TodoItem
import com.example.raintodo.repository.TodoList

class TodoListAdapter(
    private var todoLists: List<TodoList> = emptyList(),
    private val onCheckChanged: (TodoList, Boolean) -> Unit = { _, _ -> },
    private val onTitleChanged: (TodoList, String) -> Unit = { _, _ -> },
    private val onItemContentChanged: (TodoList, Int, String) -> Unit = { _, _, _ -> },
    private val onItemCheckChanged: (TodoList, Int, Boolean) -> Unit = { _, _, _ -> }
) : RecyclerView.Adapter<TodoListAdapter.TodoListViewHolder>() {

    fun submitList(newList: List<TodoList>) {
        todoLists = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return TodoListViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoListViewHolder, position: Int) {
        holder.bind(todoLists[position])
    }

    override fun getItemCount() = todoLists.size

    inner class TodoListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivCheck: ImageView by lazy { itemView.findViewById(R.id.iv_cmain_check) }
        private val etTitle: EditText by lazy { itemView.findViewById(R.id.et_cmain_list) }
        private val rvItems: RecyclerView by lazy { itemView.findViewById(R.id.rv_detail) }

        // 标题的 TextWatcher 管理
        private var titleTextWatcher: TextWatcher? = null

        fun bind(todoList: TodoList) {
            // 设置勾选状态图标
            ivCheck.setImageResource(
                if (todoList.isCompleted) R.drawable.ic_main_check
                else R.drawable.ic_main_uncheck
            )

            // 设置标题（先移除监听器，再设置文本，再添加监听器）
            removeTitleTextWatcher()
            etTitle.setText(todoList.title)
            addTitleTextWatcher(todoList)

            // 设置子项列表
            setupSubRecyclerView(todoList)

            // 勾选点击事件
            ivCheck.setOnClickListener {
                val newStatus = !todoList.isCompleted
                onCheckChanged(todoList, newStatus)
            }
        }

        private fun addTitleTextWatcher(todoList: TodoList) {
            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    onTitleChanged(todoList, s.toString())
                }
            }
            etTitle.addTextChangedListener(watcher)
            titleTextWatcher = watcher
        }

        private fun removeTitleTextWatcher() {
            titleTextWatcher?.let {
                etTitle.removeTextChangedListener(it)
            }
            titleTextWatcher = null
        }

        private fun setupSubRecyclerView(todoList: TodoList) {
            val itemAdapter = TodoItemAdapter(
                items = todoList.items,
                onContentChanged = { position, newContent ->
                    onItemContentChanged(todoList, position, newContent)
                },
                onCheckChanged = { position, isChecked ->
                    onItemCheckChanged(todoList, position, isChecked)
                }
            )
            rvItems.layoutManager = object : LinearLayoutManager(itemView.context) {
                override fun canScrollVertically(): Boolean = false
            }
            rvItems.layoutManager = LinearLayoutManager(itemView.context)
            rvItems.adapter = itemAdapter
        }
    }
}
