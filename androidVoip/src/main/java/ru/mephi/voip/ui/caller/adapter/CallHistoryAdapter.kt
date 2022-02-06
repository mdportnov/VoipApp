package ru.mephi.voip.ui.caller.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.model.CallStatus
import ru.mephi.voip.R
import ru.mephi.voip.ui.call.CallActivity
import ru.mephi.voip.databinding.ItemCallRecordBinding
import ru.mephi.voip.ui.MainActivity
import ru.mephi.voip.ui.catalog.adapter.BaseViewHolder
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


class CallHistoryAdapter internal constructor(var context: Context) :
    RecyclerView.Adapter<CallHistoryAdapter.CallHistoryViewHolder>() {

    var allRecords = mutableListOf<CallRecord>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CallHistoryViewHolder {
        val binding = ItemCallRecordBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CallHistoryViewHolder(binding)
    }

    override fun getItemCount(): Int = allRecords.size

    override fun onBindViewHolder(
        holder: CallHistoryViewHolder,
        position: Int
    ) {
        holder.bind(allRecords[position])
    }

    internal fun setRecords(newRecords: List<CallRecord>) {
        val recordsDiffUtilCallback = CallRecordsDiffUtilCallback(allRecords, newRecords)
        val recordsDiffResult = DiffUtil.calculateDiff(recordsDiffUtilCallback)
        allRecords.apply {
            clear()
            addAll(newRecords)
        }
        recordsDiffResult.dispatchUpdatesTo(this)
    }

    private lateinit var recentlyDeletedItem: CallRecord
    private var recentlyDeletedItemPosition: Int = 0

    fun deleteItem(position: Int): CallRecord {
        recentlyDeletedItem = allRecords[position]
        recentlyDeletedItemPosition = position
        notifyItemRemoved(position)
        return allRecords.removeAt(position)
    }

    inner class CallHistoryViewHolder(private val binding: ItemCallRecordBinding) :
        BaseViewHolder<CallRecord>(binding.root) {

        override fun bind(item: CallRecord) {
            if (item.sipName.isNullOrEmpty())
                binding.sipName.visibility = View.GONE
            else {
                binding.sipName.visibility = View.VISIBLE
                binding.sipName.text = item.sipName
            }

            binding.apply {
                isIncoming.setImageResource(
                    when (item.status) {
                        CallStatus.INCOMING ->
                            R.drawable.ic_baseline_call_received_24
                        CallStatus.OUTCOMING ->
                            R.drawable.ic_baseline_call_made_24
                        CallStatus.MISSED ->
                            R.drawable.ic_baseline_call_missed_24
                        CallStatus.DECLINED_FROM_SIDE ->
                            R.drawable.ic_baseline_call_declined_24
                        CallStatus.DECLINED_FROM_YOU ->
                            R.drawable.ic_baseline_call_declined_from_side_24
                    }
                )

                call.setOnClickListener {
                    if ((context as MainActivity).hasPermissions())
                        CallActivity.create(
                            this.root.context,
                            binding.sipNumber.text.toString(),
                            false
                        )
                    else
                        (context as MainActivity).requestPermissions()
                }

                sipNumber.text = item.sipNumber
                time.text = stringFromDate(item.time)
            }
        }
    }
}

fun stringFromDate(time: Long): String {
    val dtfCustom: DateTimeFormatter =
        DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
    val dateTime = LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.UTC)
    return dateTime.format(dtfCustom)
}