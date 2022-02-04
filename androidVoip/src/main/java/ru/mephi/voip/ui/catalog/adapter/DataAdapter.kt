package ru.mephi.voip.ui.catalog.adapter

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import coil.load
import coil.transform.RoundedCornersTransformation
import org.koin.java.KoinJavaComponent.inject
import ru.mephi.shared.data.database.CatalogDao
import ru.mephi.shared.data.model.Appointment
import ru.mephi.shared.data.model.UnitM
import ru.mephi.shared.data.network.KtorClientBuilder
import ru.mephi.voip.R
import ru.mephi.voip.databinding.ItemUnitBinding
import ru.mephi.voip.databinding.ItemUserBinding
import ru.mephi.voip.ui.catalog.CatalogViewModel
import ru.mephi.voip.ui.utils.*


class DataAdapter(
    var context: Context,
    private var viewModel: CatalogViewModel,
) : RecyclerView.Adapter<BaseViewHolder<*>>() {
    private var unitsList: ArrayList<Any>? = ArrayList()
    private val catalogDao: CatalogDao by inject(CatalogDao::class.java)

    private var onAttach = true

    fun getCurrentScrollPos() =
        (rv.layoutManager as LinearLayoutManager?)!!.findFirstCompletelyVisibleItemPosition()

    companion object {
        private const val UNIT_VIEW_TYPE = 1
        private const val APPOINTMENT_VIEW_TYPE = 0
    }

    lateinit var rv: RecyclerView

    fun changeData(unit: UnitM) {
        this.unitsList!!.apply {
            clear()
            unit.appointments?.let {
                addAll(it)
            }
            unit.children?.let {
                addAll(it)
            }
            notifyDataSetChanged()
        }
    }

    // Добавление в стэк viewModel нового списка юнитов, обновление его в адаптере
    private fun goNext(code_str: String, currScrollPos: Int) {
        viewModel.catalogStack.last().scrollPosition = currScrollPos
        viewModel.goNext(code_str, currScrollPos)
        (rv.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(0, 0)
    }

    inner class UnitsItemViewHolder(private val binding: ItemUnitBinding) :
        BaseViewHolder<UnitM>(binding.root) {
        override fun bind(item: UnitM) {
            binding.name.textSize = 20F
            binding.name.text = item.name

            when {
                item.name.length > 280 ->
                    binding.name.textSize = 8F
                item.name.length > 200 ->
                    binding.name.textSize = 15F
                item.name.length > 80 ->
                    binding.name.textSize = 18F
            }

            binding.name.setTextColor(Color.GRAY)
            binding.parentName.visibility = View.GONE

            if (!item.parent_name.isNullOrEmpty()) {
                binding.parentName.text = item.parent_name
                binding.parentName.visibility = View.VISIBLE
                binding.parentName.setOnClickListener {
                    goNext(item.parent_code!!, getCurrentScrollPos())
                }
            }

            if (!isOnline(context))
                binding.name.setTextColor(
                    if (catalogDao.checkByCodeStr(item.code_str))
                        Color.GRAY
                    else
                        Color.LTGRAY
                )

//            if (item.child_count.isEmpty)
//                binding.name.setTextColor(Color.LTGRAY)

            binding.name.setOnClickListener {
                goNext(item.code_str, getCurrentScrollPos())
//                when {
////                    isOnline(context) ||
////                    catalogDao.checkByCodeStr(item.code_str) -> {
////                    goNext(item.code_str, getCurrentScrollPos())
////                    }
//                    else -> Toast.makeText(context, "Сеть недоступна", Toast.LENGTH_SHORT)
//                        .show()
//                }
            }
        }
    }

    inner class AppointmentItemViewHolder(private val binding: ItemUserBinding) :
        BaseViewHolder<Appointment>(binding.root) {

        override fun bind(item: Appointment) {
            binding.apply {
                binding.hiddenInfo.visibility = View.GONE

                Animation.toggleArrow(viewMoreBtn, false)

                binding.name.text = item.fullName

                // Должность
                if (item.appointment.isNullOrEmpty()) {
                    binding.appointmentNameText.visibility = View.GONE
                    binding.appointmentNameTitle.visibility = View.GONE
                } else {
                    binding.appointmentNameTitle.visibility = View.VISIBLE
                    binding.appointmentNameText.setHasTransientState(true)

                    if (item.positions == null) {
                        binding.appointmentNameText.apply {
                            visibility = View.VISIBLE
                            text = item.appointment?.split(",")?.let {
                                if (it.size == 1) it.joinToString()
                                else it.mapIndexed { index, s ->
                                    "${index + 1}. $s\n"
                                }.joinToString("")
                            }
                        }
                    } else {
                        binding.appointmentNameText.visibility = View.GONE
                        item.positions?.forEach { positionInfo ->
                            val appointmentName = TextView(context)
                            appointmentName.setHasTransientState(true)
                            val params = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                            )

                            params.setMargins(0, 0, 0, 10)

                            appointmentName.layoutParams = params
                            appointmentName.text = positionInfo.appointmentName

                            val unitName = TextView(context)
                            val unitNameParams = LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT,
                                .8f
                            )
                            unitName.setHasTransientState(true)
                            unitName.layoutParams = unitNameParams
                            unitName.text = positionInfo.unitName

                            unitName.setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.colorAccent
                                )
                            )

                            val arrowImage = ImageView(context).apply {
                                layoutParams = LinearLayout.LayoutParams(30, 30)
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT,
                                    .2f
                                )
                                setHasTransientState(true)
                                setImageDrawable(
                                    ContextCompat.getDrawable(context, R.drawable.ic_arrow_right)
                                )
                            }

                            val outValue = TypedValue()
                            context.theme.resolveAttribute(
                                android.R.attr.selectableItemBackground,
                                outValue,
                                true
                            )

                            val unitRefLayout = LinearLayout(context).apply {
                                orientation = LinearLayout.HORIZONTAL
                                layoutParams = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.WRAP_CONTENT
                                )
                                setHasTransientState(true)
                                addView(unitName)
//                                addView(arrowImage)
                                isClickable = true
                                isFocusable = true
                                setBackgroundResource(outValue.resourceId)
                                setOnClickListener {
                                    goNext(positionInfo.unitCodeStr, getCurrentScrollPos())
                                }
                            }

                            binding.appointmentsLayout.addView(unitRefLayout)
                            binding.appointmentsLayout.addView(appointmentName, params)
                        }
                    }
                }

                // Проверка на отображение кнопки "Развернуть"
                if (item.email.isNullOrEmpty()
                    && item.room.isNullOrEmpty()
                    && item.lineShown.isNullOrEmpty()
                )
                    binding.viewMoreBtn.visibility = View.GONE
                else
                    binding.viewMoreBtn.visibility = View.VISIBLE

                // Почта
                if (item.email.isNullOrEmpty()) {
                    binding.layoutEmail.visibility = View.GONE
                } else {
                    binding.layoutEmail.visibility = View.VISIBLE
                    binding.textEmail.text = item.email
                    binding.layoutEmail.setOnClickListener { context.launchMailClientIntent(binding.textEmail.text.toString()) }
                }

                // SIP
                if (item.lineShown == null) {
                    binding.layoutSip.visibility = View.GONE
                    binding.layoutPhone.visibility = View.GONE
                } else {
                    binding.textSip.text = item.lineShown
                    binding.textFirstPhone.text = context.getString(R.string.call_via_phone)
                    binding.layoutSip.visibility = View.VISIBLE
                    binding.layoutPhone.visibility = View.VISIBLE
                }

                // Помещение
                if (item.room == null)
                    binding.roomLayout.visibility = View.GONE
                else {
                    binding.roomLayout.visibility = View.VISIBLE
                    binding.textRoom.text = item.room
                }

                binding.layoutSip.setOnClickListener { view ->
                    if (binding.textSip.text.isNotEmpty())
                        view.findNavController().navigate(
                            R.id.callFromCatalogAction,
                            bundleOf(
                                "caller_number" to binding.textSip.text,
                                "caller_name" to binding.name.text
                            )
                        )
                }

                binding.phoneLayout.setOnClickListener {
                    context.launchDialer(context.getString(R.string.mephi_number) + "," + item.lineShown)
                }

                binding.viewMoreBtn.setOnClickListener {
                    if (binding.hiddenInfo.visibility == View.GONE) {
                        binding.hiddenInfo.visibility = View.VISIBLE
                        Animation.toggleArrow(binding.viewMoreBtn, true)
                        TransitionManager.beginDelayedTransition(
                            binding.root,
                            AutoTransition()
                        )
                    } else {
                        binding.hiddenInfo.visibility = View.GONE
                        Animation.toggleArrow(binding.viewMoreBtn, false)
                        TransitionManager.beginDelayedTransition(
                            binding.root,
                            AutoTransition()
                        )
                    }
                }

                binding.photo.load(
                    if (item.line.isNullOrEmpty())
                        KtorClientBuilder.PHOTO_REQUEST_URL_BY_GUID + item.EmpGUID
                    else
                        KtorClientBuilder.PHOTO_REQUEST_URL_BY_PHONE + item.line
                ) {
                    transformations(RoundedCornersTransformation(25f))
                    size(200, 300)
                    error(R.drawable.nophoto)
                }
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        rv = recyclerView
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                onAttach = false
                super.onScrollStateChanged(recyclerView, newState)
            }
        })
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        return when (viewType) {
            UNIT_VIEW_TYPE -> {
                val binding = ItemUnitBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                UnitsItemViewHolder(binding)
            }
            APPOINTMENT_VIEW_TYPE -> {
                val binding = ItemUserBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                AppointmentItemViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val element = unitsList?.get(position)
        when (holder) {
            is AppointmentItemViewHolder -> {
                holder.bind(element as Appointment)
                setFromLeftToRightAnimation(holder.itemView, position, onAttach)
            }
            is UnitsItemViewHolder -> {
                holder.apply {
                    bind(element as UnitM)
                    setFadeInAnimation(holder.itemView, position, onAttach)
                }
            }
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int = when (unitsList?.get(position)) {
        is UnitM -> UNIT_VIEW_TYPE
        is Appointment -> APPOINTMENT_VIEW_TYPE
        else -> throw IllegalArgumentException("Unknown item type at: $position")
    }

    override fun getItemCount(): Int = unitsList?.size ?: 0
}