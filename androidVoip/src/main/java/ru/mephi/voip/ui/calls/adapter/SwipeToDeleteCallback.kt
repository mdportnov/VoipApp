package ru.mephi.voip.ui.calls.adapter

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import org.koin.java.KoinJavaComponent.inject
import ru.mephi.shared.appContext
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.repository.CallsRepository
import ru.mephi.voip.R
import ru.mephi.voip.call.ui.CallActivity


class SwipeToDeleteCallback(private var adapter: CallHistoryAdapter) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val callRepository: CallsRepository by inject(CallsRepository::class.java)

    private var icon: Drawable = ContextCompat.getDrawable(
        adapter.context,
        R.drawable.ic_baseline_delete_24
    )!!

    private var background: ColorDrawable = ColorDrawable(Color.RED)

    private fun changeDirection(dX: Float) {
        if (dX < 0) {
            icon = ContextCompat.getDrawable(
                adapter.context,
                R.drawable.ic_baseline_call_24
            )!!
            background = ColorDrawable(Color.GREEN)
        } else {
            icon = ContextCompat.getDrawable(
                adapter.context,
                R.drawable.ic_baseline_delete_24
            )!!
            background = ColorDrawable(Color.RED)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        RecyclerViewSwipeDecorator.Builder(
            c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive
        )
            .addBackgroundColor(appContext.getColor(R.color.colorAccent))
            .addActionIcon(R.drawable.ic_baseline_delete_24)
            .addSwipeLeftBackgroundColor(appContext.getColor(R.color.colorGreen))
            .addSwipeLeftActionIcon(R.drawable.ic_baseline_call_24)
            .create()
            .decorate()
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    private lateinit var deletedRecord: CallRecord

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition

        when (direction) {
            ItemTouchHelper.LEFT -> {
                CallActivity.create(
                    viewHolder.itemView.context, adapter.allRecords[position]
                        .sipNumber, false
                )
            }
            ItemTouchHelper.RIGHT -> {
                deletedRecord = adapter.deleteItem(position)
                Snackbar.make(
                    viewHolder.itemView,
                    "Запись ${deletedRecord.sipNumber}",
                    Snackbar.LENGTH_LONG
                ).setAction("Вернуть") {
                    adapter.allRecords.add(position, deletedRecord)
                    callRepository.addRecord(deletedRecord)
                    adapter.notifyItemInserted(position)
                }.show()
                callRepository.deleteRecord(deletedRecord)
            }
        }
    }
}