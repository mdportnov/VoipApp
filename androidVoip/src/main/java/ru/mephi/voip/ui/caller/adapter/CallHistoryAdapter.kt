package ru.mephi.voip.ui.caller.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import coil.load
import coil.transform.RoundedCornersTransformation
import org.abtollc.sdk.AbtoPhone
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ru.mephi.shared.data.model.CallRecord
import ru.mephi.shared.data.model.CallStatus
import ru.mephi.shared.data.network.KtorClientBuilder
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.voip.R
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.ui.call.CallActivity
import ru.mephi.voip.databinding.ItemCallRecordBinding
import ru.mephi.voip.ui.MainActivity
import ru.mephi.voip.ui.catalog.adapter.BaseViewHolder
import ru.mephi.voip.utils.Animation
import ru.mephi.voip.utils.showSnackBar
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter


class CallHistoryAdapter internal constructor(var context: Context) :
    RecyclerView.Adapter<CallHistoryAdapter.CallHistoryViewHolder>(), KoinComponent {

    private val accountStatusRepository: AccountStatusRepository by inject()
    var allRecords = mutableListOf<CallRecord>()
    lateinit var binding: ItemCallRecordBinding

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CallHistoryViewHolder {
        binding = ItemCallRecordBinding.inflate(
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

        fun toggleMore() {
            if (binding.additionalCallInfo.visibility == View.GONE) {
                binding.additionalCallInfo.visibility = View.VISIBLE
                Animation.toggleArrow(binding.viewMoreBtn, true)
                TransitionManager.beginDelayedTransition(
                    binding.root,
                    AutoTransition()
                )
            } else {
                binding.additionalCallInfo.visibility = View.GONE
                Animation.toggleArrow(binding.viewMoreBtn, false)
                TransitionManager.beginDelayedTransition(
                    binding.root,
                    AutoTransition()
                )
            }
        }

        override fun bind(item: CallRecord) {
            Animation.toggleArrow(binding.viewMoreBtn, false)

            binding.viewMoreBtn.setOnClickListener {
                toggleMore()
            }

            binding.root.setOnClickListener {
                toggleMore()
            }

            if (item.sipName.isNullOrEmpty())
                binding.sipName.visibility = View.GONE
            else {
                binding.sipName.visibility = View.VISIBLE
                binding.sipName.text = item.sipName
            }

            binding.callerPhoto.load(
                KtorClientBuilder.PHOTO_REQUEST_URL_BY_PHONE + item.sipNumber
            ) {
                transformations(RoundedCornersTransformation(25f))
                size(200, 300)
                error(R.drawable.nophoto)
            }

            binding.callStatusText.text = item.status.text

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
                        else -> R.drawable.ic_baseline_error_24
                    }
                )

                call.setOnClickListener {
                    if ((context as MainActivity).hasPermissions()) {
                        if (accountStatusRepository.status.value == AccountStatus.REGISTERED) {
                            CallActivity.create(context, binding.sipNumber.text.toString(), false)
                        } else {
                            showSnackBar(
                                binding.root,
                                "Нет активного аккаунта для совершения звонка"
                            )
                        }
                    } else
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
    val dateTime = LocalDateTime.ofEpochSecond(time, 0, ZoneOffset.of("+03:00"))
    return dateTime.format(dtfCustom)
}