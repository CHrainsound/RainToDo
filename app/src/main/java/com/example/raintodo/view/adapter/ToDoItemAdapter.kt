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
    private val onCheckChanged: (Int, Boolean) -> Unit = { _, _ -> }
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
        holder.bind(items[position], position)
    }

    override fun getItemCount() = items.size

    inner class TodoItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val ivSubCheck: ImageView by lazy { itemView.findViewById(R.id.iv_item) }
        private val etSubContent: EditText by lazy { itemView.findViewById(R.id.et_item) }

        // 自定义 TextWatcher，方便移除
        private var currentTextWatcher: TextWatcher? = null

        fun bind(item: TodoItem, position: Int) {
            // 设置勾选状态
            ivSubCheck.setImageResource(
                if (item.isCompleted) R.drawable.ic_main_check
                else R.drawable.ic_main_uncheck
            )

            // 设置内容（先移除旧的监听器，再设置文本，再添加新的监听器）
            removeTextWatcher()
            etSubContent.setText(item.content)
            addTextWatcher(position)

            // 勾选点击
            ivSubCheck.setOnClickListener {
                onCheckChanged(position, !item.isCompleted)
            }
        }

        private fun addTextWatcher(position: Int) {
            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    onContentChanged(position, s.toString())
                }
            }
            etSubContent.addTextChangedListener(watcher)
            currentTextWatcher = watcher
        }

        private fun removeTextWatcher() {
            currentTextWatcher?.let {
                etSubContent.removeTextChangedListener(it)
            }
            currentTextWatcher = null
        }
    }
}
