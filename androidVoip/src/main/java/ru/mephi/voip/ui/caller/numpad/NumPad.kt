package ru.mephi.voip.ui.caller.numpad

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TableLayout
import android.widget.Toast
import ru.mephi.voip.R
import ru.mephi.voip.databinding.NumpadLayoutBinding

class NumPad(context: Context, attrs: AttributeSet?) : TableLayout(context, attrs) {
    lateinit var binding: NumpadLayoutBinding
    private var listener: NumPadClick? = null
    private var llistener: NumPadLongClick? = null
    private var sb = StringBuilder()

    fun clear() {
        sb.clear()
        binding.inputNumber.setText("")
    }

    fun setOnCancelButtonClickListener(l: OnClickListener) {
        binding.numPadCancel.setOnClickListener(l)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        numbers.clear()
    }

    override fun onViewRemoved(child: View?) {
        super.onViewRemoved(child)
    }

    fun getInputNumber(): String = sb.toString()

    private fun inflateNumPad(context: Context) {
        binding = NumpadLayoutBinding.inflate(
            LayoutInflater.from(context), this, true
        )
        initListeners()
    }

    fun setNumber(number: String) {
        sb.clear()
        sb.append(number)
        binding.inputNumber.setText(sb.toString())
    }

    private fun initListeners() {
        this.listener = NumPadClick(object : NumPadClickListener {
            override fun onNumClicked(nums: StringBuilder, view: View) {
                if (nums.isNotExceed())
                    binding.inputNumber.setText(nums.toString()).also { sb = nums }
                else {
                    nums.deleteCharAt(nums.lastIndex)
                    Toast.makeText(view.context, "Превышен размер номера", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        })

        this.llistener = NumPadLongClick(
            object : NumPadLongClickListener {
                override fun onNumLongClicked(nums: StringBuilder, view: View) {
                    if (view.id == R.id.input_delete) {
                        sb = nums.clear()
                        binding.inputNumber.setText(sb.toString())
                    }
                }
            })

        binding.btnNumpad0.setOnClickListener(listener)
        binding.btnNumpad1.setOnClickListener(listener)
        binding.btnNumpad2.setOnClickListener(listener)
        binding.btnNumpad3.setOnClickListener(listener)
        binding.btnNumpad4.setOnClickListener(listener)
        binding.btnNumpad5.setOnClickListener(listener)
        binding.btnNumpad6.setOnClickListener(listener)
        binding.btnNumpad7.setOnClickListener(listener)
        binding.btnNumpad8.setOnClickListener(listener)
        binding.btnNumpad9.setOnClickListener(listener)
        binding.btnNumpadPound.setOnClickListener(listener)
        binding.btnNumpadStar.setOnClickListener(listener)
        binding.inputDelete.setOnClickListener(listener)
        binding.inputDelete.setOnLongClickListener(llistener)
    }

    init {
        inflateNumPad(context)
    }
}