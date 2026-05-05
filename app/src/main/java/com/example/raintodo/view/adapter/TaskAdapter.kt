package com.example.raintodo.view.adapter

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.raintodo.R

class TaskAdapter(
    private val taskList: MutableList<String>,
    private val onTextChanged: (Int, String) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val etTaskContent: EditText = view.findViewById(R.id.et_item)
        val IvFinish: ImageView = view.findViewById(R.id.iv_item)

        private val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    taskList[position] = s.toString()
                    onTextChanged.invoke(position, s.toString())
                }
            }
        }

        init {
            etTaskContent.addTextChangedListener(textWatcher)

            // 回车键监听
            etTaskContent.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_NEXT ||
                    actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    // 通知外部添加新项
                    (itemView.parent as? RecyclerView)?.let { recyclerView ->
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION && position == taskList.size - 1) {
                            // 如果是最后一项，添加新项
                            taskList.add("")
                            notifyItemInserted(taskList.size - 1)
                            recyclerView.smoothScrollToPosition(taskList.size - 1)
                        }
                    }
                    return@setOnEditorActionListener true
                }
                false
            }
        }

        fun bind(text: String) {
            etTaskContent.removeTextChangedListener(textWatcher)
            etTaskContent.setText(text)
            etTaskContent.addTextChangedListener(textWatcher)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(taskList[position])

        holder.IvFinish.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                taskList.removeAt(pos)
                notifyItemRemoved(pos)
            }
        }
    }

    override fun getItemCount() = taskList.size
}