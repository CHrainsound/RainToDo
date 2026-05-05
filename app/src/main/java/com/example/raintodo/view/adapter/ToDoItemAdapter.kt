package com.example.raintodo.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.raintodo.R
import com.example.raintodo.repository.TodoItem

class TodoItemAdapter(
    private var items: List<TodoItem> = emptyList(),
    private val onContentChanged: (Int, String) -> Unit = { _, _ -> },
    private val onCheckChanged: (Int, Boolean) -> Unit = { _, _ -> },
    private val onAddNewItem: () -> Unit = {},
    private val onDeleteItem: (Int) -> Unit = {}
) : RecyclerView.Adapter<TodoItemAdapter.TodoItemViewHolder>() {

    fun submitList(newItems: List<TodoItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TodoItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoItemViewHolder, position: Int) {
        if (position == items.size) {
            holder.bindAddButton()
        } else {
            holder.bind(items[position])
        }
    }

    override fun getItemCount() = items.size + 1

    inner class TodoItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivSubCheck: ImageView = itemView.findViewById(R.id.iv_item)
        private val etSubContent: EditText = itemView.findViewById(R.id.et_item)

        private var currentTextWatcher: TextWatcher? = null
        private var lastContent: String = ""
        private var currentItemId: Int = -1

        fun bind(item: TodoItem) {
            currentItemId = item.id
            lastContent = item.content

            ivSubCheck.visibility = View.VISIBLE
            ivSubCheck.setImageResource(
                if (item.isCompleted) R.drawable.ic_main_check
                else R.drawable.ic_main_uncheck
            )

            removeTextWatcher()

            if (etSubContent.text.toString() != item.content) {
                etSubContent.setText(item.content)
            }

            addTextWatcher()

            ivSubCheck.setOnClickListener {
                onCheckChanged(currentItemId, !item.isCompleted)
            }
        }

        fun bindAddButton() {
            currentItemId = -1
            ivSubCheck.visibility = View.GONE
            etSubContent.hint = "+ 添加子任务"
            etSubContent.setText("")

            etSubContent.setOnClickListener { onAddNewItem() }
        }

        private fun addTextWatcher() {
            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val newContent = s.toString()
                    if (newContent.isEmpty() && lastContent.isNotEmpty() && currentItemId != -1) {
                        onDeleteItem(currentItemId)
                        return
                    }
                    if (newContent != lastContent && currentItemId != -1) {
                        lastContent = newContent
                        onContentChanged(currentItemId, newContent)
                    }
                }
            }
            etSubContent.addTextChangedListener(watcher)
            currentTextWatcher = watcher
        }

        private fun removeTextWatcher() {
            currentTextWatcher?.let { etSubContent.removeTextChangedListener(it) }
            currentTextWatcher = null
        }
    }
}