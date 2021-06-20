package com.cloudlevi.weatherwizard.ui.cityList

import android.content.ContentValues
import android.graphics.Canvas
import android.graphics.Color
import android.util.Log
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.cloudlevi.weatherwizard.R
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator

class ItemTouchHelperCallback(private val listener: SimpleCallbackListener): ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition

        if (direction == ItemTouchHelper.LEFT){
            listener.onItemSwiped(position)
        }
    }

    override fun onChildDraw(c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean) {
        RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            .addSwipeLeftBackgroundColor(Color.RED)
            .addSwipeLeftActionIcon(R.drawable.ic_delete)
            .create()
            .decorate()
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
    }

    override fun getSwipeDirs(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        val position = viewHolder.adapterPosition
        return createSwipeFlags(position, recyclerView, viewHolder)
    }

    private fun createSwipeFlags(position: Int, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        return if (position == 0 || viewHolder is CityListQueryAdapter.CityEntryViewHolder) 0 else super.getSwipeDirs(recyclerView, viewHolder)
    }
}

interface SimpleCallbackListener{
    fun onItemSwiped(position: Int)
}