package ru.mephi.voip.ui.caller.adapter

import android.graphics.Canvas
import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator
import org.koin.java.KoinJavaComponent.inject
import ru.mephi.shared.appContext
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.repository.CallsRepository
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.R
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.call.CallActivity

class SwipeToDeleteCallback(private var adapter: CallHistoryAdapter) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

    private val callRepository: CallsRepository by inject(CallsRepository::class.java)
    private val accountStatusRepository: AccountStatusRepository by inject(AccountStatusRepository::class.java)

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
            .addSwipeRightBackgroundColor(appContext.getColor(R.color.colorAccent))
            .addSwipeRightActionIcon(R.drawable.ic_baseline_delete_24)
            .setSwipeRightActionIconTint(appContext.getColor(R.color.colorPrimary))
            .setSwipeLeftActionIconTint(appContext.getColor(R.color.colorPrimary))
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
                adapter.notifyDataSetChanged()

                if (accountStatusRepository.status.value == AccountStatus.REGISTERED) {
                    CallActivity.create(
                        viewHolder.itemView.context, adapter.allRecords[position]
                            .sipNumber, false
                    )
                } else {
                    Toast.makeText(
                        appContext,
                        "Нет активного аккаунта для совершения звонка",
                        Toast.LENGTH_SHORT
                    ).show()
                }
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