package ru.mephi.voip.ui.caller.adapter

import androidx.recyclerview.widget.DiffUtil
import ru.mephi.shared.data.model.CallRecord

class CallRecordsDiffUtilCallback(
    private val oldList: List<CallRecord>,
    private val newList: List<CallRecord>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldProduct = oldList[oldItemPosition]
        val newProduct = newList[newItemPosition]
        return oldProduct.id === newProduct.id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldProduct = oldList[oldItemPosition]
        val newProduct = newList[newItemPosition]
        return (oldProduct.time == newProduct.time)
    }
}