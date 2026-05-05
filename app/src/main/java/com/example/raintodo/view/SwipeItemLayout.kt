package com.example.raintodo.view.widget
import com.example.raintodo.R
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.HorizontalScrollView

class SwipeItemLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : HorizontalScrollView(context, attrs, defStyleAttr) {

    private var buttonsWidth = 0
    private var isOpen = false
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var downX = 0f
    private var downY = 0f
    private var isHorizontalScroll = false

    var onDeleteClick: (() -> Unit)? = null
    var onTopClick: (() -> Unit)? = null

    init {
        isHorizontalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        val contentLayout = getChildAt(0) as? ViewGroup ?: return

        val btnTop = contentLayout.findViewById<View>(R.id.btn_top)
        val btnDelete = contentLayout.findViewById<View>(R.id.btn_delete)
        val cardMain = contentLayout.findViewById<View>(R.id.card_main)

        btnTop?.setOnClickListener {
            onTopClick?.invoke()
            close()
        }
        btnDelete?.setOnClickListener {
            onDeleteClick?.invoke()
            close()
        }

        post {
            // 按钮宽度
            buttonsWidth = (btnTop?.measuredWidth ?: 0) + (btnDelete?.measuredWidth ?: 0)
            val containerWidth = this@SwipeItemLayout.width

            contentLayout.layoutParams = contentLayout.layoutParams.apply {
                width = containerWidth + buttonsWidth
            }

            // 设置卡片宽度
            cardMain?.layoutParams = cardMain?.layoutParams?.apply {
                width = containerWidth
            }

            contentLayout.requestLayout()
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.rawX
                downY = ev.rawY
                isHorizontalScroll = false
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isHorizontalScroll) {
                    val dx = ev.rawX - downX
                    val dy = ev.rawY - downY
                    if (Math.abs(dx) > touchSlop && Math.abs(dx) > Math.abs(dy)) {
                        isHorizontalScroll = true
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                }
            }
        }
        return isHorizontalScroll || super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isHorizontalScroll) {
                    if (scrollX > buttonsWidth / 2) {
                        open()
                    } else {
                        close()
                    }
                    parent.requestDisallowInterceptTouchEvent(false)
                    return true
                }
            }
        }
        return super.onTouchEvent(ev)
    }

    fun open() {
        smoothScrollTo(buttonsWidth, 0)
        isOpen = true
    }

    fun close() {
        smoothScrollTo(0, 0)
        isOpen = false
    }
}