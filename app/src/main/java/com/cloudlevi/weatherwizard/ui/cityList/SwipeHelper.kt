package com.cloudlevi.weatherwizard.ui.cityList

import android.annotation.SuppressLint
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper.*
import androidx.recyclerview.widget.RecyclerView
import com.cloudlevi.weatherwizard.databinding.ObservedCityItemBinding


class SwipeHelper(
    private var receivedButtonAction: SwipeControllerActions,
    private var mainRecyclerView: RecyclerView
): Callback() {

    private var swipeBack = false
    private var buttonState = ButtonState.GONE
    private val buttonWidth = 300F
    private var buttonInstance: RectF? = null
    private var currentItemViewHolder: RecyclerView.ViewHolder? = null
    private var buttonAction: SwipeControllerActions? = null

    init {
        buttonAction = receivedButtonAction

        setItemsClickable(mainRecyclerView, true)
    }

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        return makeMovementFlags(0, LEFT)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
    }

    override fun convertToAbsoluteDirection(flags: Int, layoutDirection: Int): Int {
        if (swipeBack) {
            swipeBack = buttonState != ButtonState.GONE
            return 0
        }
        return super.convertToAbsoluteDirection(flags, layoutDirection)
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    )
    {
        var dXLocal = dX

        if(viewHolder.adapterPosition != 0 && viewHolder !is CityListQueryAdapter.CityEntryViewHolder){
        if (actionState == ACTION_STATE_SWIPE){
            if (buttonState != ButtonState.GONE) {
                if (buttonState == ButtonState.DELETE_VISIBLE) dXLocal = Math.min(
                    dXLocal,
                    -buttonWidth
                )
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dXLocal,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
            else {
                setTouchListener(
                    c,
                    recyclerView,
                    viewHolder,
                    dXLocal,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }
        if (buttonState == ButtonState.GONE)
            super.onChildDraw(
                c,
                recyclerView,
                viewHolder,
                dXLocal,
                dY,
                actionState,
                isCurrentlyActive
            )

        currentItemViewHolder = viewHolder
        drawButton(c, viewHolder)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListener(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
            recyclerView.setOnTouchListener { view, motionEvent ->
                swipeBack = motionEvent?.action == MotionEvent.ACTION_CANCEL ||
                        motionEvent?.action == MotionEvent.ACTION_UP

                if (swipeBack) {
                    if (dX < -buttonWidth) buttonState = ButtonState.DELETE_VISIBLE
                    if (buttonState != ButtonState.GONE) {
                        setTouchDownListener(
                            c,
                            recyclerView,
                            viewHolder,
                            dX,
                            dY,
                            actionState,
                            isCurrentlyActive
                        )
                        setItemsClickable(recyclerView, false)
                    }
                }
                false
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchDownListener(
        c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
            recyclerView.setOnTouchListener { view, motionEvent ->
                if (motionEvent?.action == MotionEvent.ACTION_DOWN)
                    setTouchUpListener(
                        c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
                    )
                false
            }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchUpListener(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
            recyclerView.setOnTouchListener { view, motionEvent ->
                if (motionEvent?.action == MotionEvent.ACTION_UP) {
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        0F,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    recyclerView.setOnTouchListener{ view, motionEvent ->
                        false
                    }
                    setItemsClickable(recyclerView, true)
                    swipeBack = false

                    if (buttonAction != null && buttonInstance != null && (buttonInstance as RectF).contains(
                            motionEvent.x,
                            motionEvent.y
                        )) {
                        if (buttonState == ButtonState.DELETE_VISIBLE) {

                            (buttonAction as SwipeControllerActions).onDeleteClicked(viewHolder.adapterPosition)
                            setItemsClickable(recyclerView, true)
                        }
                    }
                    buttonState = ButtonState.GONE
                    currentItemViewHolder = null
                }
                false
            }
    }


    private fun setItemsClickable(recyclerView: RecyclerView, isClickable: Boolean){
        for (i in 0 until recyclerView.childCount) {
            recyclerView.getChildAt(i).isClickable = isClickable

//            val function: (View) -> Unit = {
//                buttonAction?.onItemClicked(
//                    recyclerView.getChildAdapterPosition(
//                        recyclerView.getChildAt(i)
//                    ), recyclerView.getChildAt(i).rootView
//                )
//            }
//            recyclerView.getChildAt(i).rootView.setOnClickListener(function)
        }
    }

    private fun drawButton(c: Canvas, viewHolder: RecyclerView.ViewHolder){
        val buttonWidthWithoutPadding = buttonWidth - 20
        val corners = 16F

        val itemView = viewHolder.itemView
        val p = Paint()

        val deleteButton = RectF(
            itemView.right - buttonWidthWithoutPadding,
            itemView.top.toFloat(),
            itemView.right.toFloat(),
            itemView.bottom.toFloat()
        )
        p.color = Color.RED
        c.drawRoundRect(deleteButton, corners, corners, p)
        drawText("DELETE", c, deleteButton, p)

        buttonInstance = deleteButton
        buttonAction?.onItemSwiped(viewHolder.adapterPosition)
    }

    private fun drawText(text: String, c: Canvas, button: RectF, p: Paint) {
        val textSize = 50f
        p.color = Color.WHITE
        p.isAntiAlias = true
        p.textSize = textSize

        val textWidth = p.measureText(text)
        c.drawText(text, button.centerX() - textWidth / 2, button.centerY() + textSize / 2, p)
    }

    fun onDraw(c: Canvas) {
        if (currentItemViewHolder != null) {
            drawButton(c, (currentItemViewHolder as CityListObservedAdapter.CityListItemViewHolder))
        }
    }


    enum class ButtonState {
        GONE,
        DELETE_VISIBLE
    }

    interface SwipeControllerActions {
        fun onDeleteClicked(position: Int)
//        fun onItemClicked(position: Int, childView: View)
        fun onItemSwiped(position: Int)
    }
}