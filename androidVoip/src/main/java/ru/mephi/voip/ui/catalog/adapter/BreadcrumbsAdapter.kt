package ru.mephi.voip.ui.catalog.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.mephi.shared.data.model.UnitM
import ru.mephi.voip.databinding.ItemRecyclerBreadcrumbBinding
import ru.mephi.voip.ui.catalog.CatalogViewModel
import java.util.*

fun <T> Stack<T>.popFromStackTill(el: T) {
    while (this.peek() != el)
        this.pop()
}

class BreadcrumbsAdapter(
    var context: Context?,
    private var viewModel: CatalogViewModel,
) : RecyclerView.Adapter<BreadcrumbsAdapter.ViewHolder>() {

    var units = listOf<UnitM>()

    inner class ViewHolder(private val binding: ItemRecyclerBreadcrumbBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindView(position: Int) {
            val unit = units[position]
            binding.nameTextView.text = unit.shortname
            binding.root.setOnClickListener {
                viewModel.dismissProgressBar()
                viewModel.breadcrumbStack.popFromStackTill(unit)
                viewModel.breadcrumbLiveData.postValue(viewModel.breadcrumbStack)
                viewModel.catalogStack.popFromStackTill(unit)
                viewModel.catalogLiveData.postValue(viewModel.catalogStack)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRecyclerBreadcrumbBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindView(position)
    }

    override fun getItemCount() = units.size

    fun changeData(units: List<UnitM>) {
        this.units = units
        notifyDataSetChanged()
    }
}