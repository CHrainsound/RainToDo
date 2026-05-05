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
import com.example.raintodo.repository.TodoList
import com.example.raintodo.view.widget.SwipeItemLayout

class TodoListAdapter(
    private var todoLists: List<TodoList> = emptyList(),
    private val onCheckChanged: (TodoList, Boolean) -> Unit = { _, _ -> },
    private val onTitleChanged: (TodoList, String) -> Unit = { _, _ -> },
    private val onItemContentChanged: (TodoList, Int, String) -> Unit = { _, _, _ -> },
    private val onItemCheckChanged: (TodoList, Int, Boolean) -> Unit = { _, _, _ -> },
    private val onAddNewItem: (TodoList) -> Unit = {},
    private val onDeleteTodoList: (TodoList) -> Unit = {},
    private val onTopTodoList: (TodoList) -> Unit = {},
    private val onDeleteTodoItem: (TodoList, Int) -> Unit = { _, _ -> }
) : RecyclerView.Adapter<TodoListAdapter.TodoListViewHolder>() {

    private val itemAdapters = mutableMapOf<Int, TodoItemAdapter>()
    private var openLayout: SwipeItemLayout? = null

    fun submitList(newList: List<TodoList>) {
        todoLists = newList
    }

    fun refreshAll() {
        notifyDataSetChanged()
    }

    fun focusFirstItem() {
        if (todoLists.isNotEmpty()) {
            notifyItemChanged(0)
        }
    }

    fun closeOpenItem() {
        openLayout?.close()
        openLayout = null
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
        private val swipeLayout: SwipeItemLayout = itemView.findViewById(R.id.swipe_layout)
        private val ivCheck: ImageView = itemView.findViewById(R.id.iv_cmain_check)
        private val etTitle: EditText = itemView.findViewById(R.id.et_cmain_list)
        private val rvItems: RecyclerView = itemView.findViewById(R.id.rv_detail)

        private var titleTextWatcher: TextWatcher? = null

        init {
            // 关闭侧滑时清除引用
            swipeLayout.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {}
                override fun onViewDetachedFromWindow(v: View) {
                    if (openLayout == swipeLayout) openLayout = null
                }
            })
        }

        fun bind(todoList: TodoList) {
            // 重置状态
            swipeLayout.close()

            // 设置按钮回调
            swipeLayout.onDeleteClick = {
                openLayout = null
                onDeleteTodoList(todoList)
            }
            swipeLayout.onTopClick = {
                openLayout = null
                onTopTodoList(todoList)
            }

            // 监听打开状态
            swipeLayout.setOnTouchListener { v, event ->
                if (event.action == android.view.MotionEvent.ACTION_DOWN) {
                    if (openLayout != swipeLayout) {
                        openLayout?.close()
                        openLayout = swipeLayout
                    }
                }
                false
            }

            ivCheck.setImageResource(
                if (todoList.isCompleted) R.drawable.ic_main_check
                else R.drawable.ic_main_uncheck
            )

            removeTitleTextWatcher()
            if (etTitle.text.toString() != todoList.title) {
                etTitle.setText(todoList.title)
            }
            addTitleTextWatcher(todoList)

            setupSubRecyclerView(todoList)

            ivCheck.setOnClickListener {
                onCheckChanged(todoList, !todoList.isCompleted)
            }
        }

        private fun addTitleTextWatcher(todoList: TodoList) {
            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val newText = s.toString()
                    if (newText != todoList.title) {
                        onTitleChanged(todoList, newText)
                    }
                }
            }
            etTitle.addTextChangedListener(watcher)
            titleTextWatcher = watcher
        }

        private fun removeTitleTextWatcher() {
            titleTextWatcher?.let { etTitle.removeTextChangedListener(it) }
            titleTextWatcher = null
        }

        private fun setupSubRecyclerView(todoList: TodoList) {
            val adapter = TodoItemAdapter(
                items = todoList.items,
                onContentChanged = { itemId, newContent ->
                    onItemContentChanged(todoList, itemId, newContent)
                },
                onCheckChanged = { itemId, isChecked ->
                    onItemCheckChanged(todoList, itemId, isChecked)
                },
                onAddNewItem = { onAddNewItem(todoList) },
                onDeleteItem = { itemId -> onDeleteTodoItem(todoList, itemId) }
            )
            itemAdapters[todoList.id] = adapter
            rvItems.layoutManager = LinearLayoutManager(itemView.context)
            rvItems.adapter = adapter
            rvItems.isNestedScrollingEnabled = false
            rvItems.setHasFixedSize(false)
            adapter.submitList(todoList.items)
        }
    }
}