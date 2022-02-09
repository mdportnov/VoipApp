package ru.mephi.voip.ui.caller

import android.Manifest
import android.animation.ObjectAnimator
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Insets
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.animation.OvershootInterpolator
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.*
import androidx.core.view.marginRight
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.shared.vm.CallerViewModel
import ru.mephi.voip.R
import ru.mephi.voip.ui.call.CallActivity
import ru.mephi.voip.databinding.FragmentCallsBinding
import ru.mephi.voip.databinding.ToolbarCallerBinding
import ru.mephi.voip.ui.caller.adapter.CallHistoryAdapter
import ru.mephi.voip.ui.caller.adapter.SwipeToDeleteCallback
import ru.mephi.voip.utils.toast
import timber.log.Timber
import com.squareup.sqldelight.runtime.coroutines.asFlow
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.utils.slideDown
import ru.mephi.voip.utils.slideUp
import androidx.core.graphics.drawable.DrawableCompat

import androidx.appcompat.content.res.AppCompatResources
import ru.mephi.shared.appContext


class CallerFragment : Fragment() {
    private val viewModel: CallerViewModel by inject()
    private val accountStatusRepository: AccountStatusRepository by inject()

    private lateinit var historyAdapter: CallHistoryAdapter

    private lateinit var binding: FragmentCallsBinding
    private lateinit var toolbarBinding: ToolbarCallerBinding

    private var isPermissionGranted = true
    var isNumPadUp = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCallsBinding.inflate(inflater, container, false)
        toolbarBinding = binding.toolbarCaller
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermissions()
        setupToolbar()
        initViews()
        initStatusObserver()
    }

    private fun initStatusObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                accountStatusRepository.status.collect { status ->
                    Timber.d("Getting status from SharedViewModel ${status.status}")

                    toolbarBinding.statusText.text = status.status
                    val unwrappedDrawable =
                        AppCompatResources.getDrawable(context!!, R.drawable.shape_circle)!!
                    val wrappedDrawable: Drawable = DrawableCompat.wrap(unwrappedDrawable)
                    DrawableCompat.setTint(
                        wrappedDrawable, when (status) {
                            AccountStatus.REGISTERED ->
                                getColor(appContext, R.color.colorGreen)
                            AccountStatus.NO_CONNECTION, AccountStatus.CHANGING, AccountStatus.LOADING ->
                                getColor(appContext, R.color.colorGray)
                            AccountStatus.UNREGISTERED, AccountStatus.REGISTRATION_FAILED ->
                                getColor(appContext, R.color.colorRed)
                        }
                    )
                    toolbarBinding.statusCircle.setBackgroundDrawable(wrappedDrawable)
                }
            }
        }
    }

    private fun checkPermissions() {
        val permissions = ArrayList<String>()
        if (checkSelfPermission(
                requireContext(),
                Manifest.permission.USE_SIP
            ) != PackageManager.PERMISSION_GRANTED
        )
            permissions.add(Manifest.permission.USE_SIP)
        if (checkSelfPermission(
                requireContext().applicationContext,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        )
            permissions.add(Manifest.permission.RECORD_AUDIO)

        isPermissionGranted = permissions.size <= 0
        if (permissions.size > 0)
            requestPermissions(
                permissions.toTypedArray(), 1
            )
    }

    private fun initViews() {
        val args: CallerFragmentArgs by navArgs()

        // Убрать нумпад по кнопке назад, если он отображается
        requireActivity().onBackPressedDispatcher.addCallback(this) {
            if (isNumPadUp)
                changeNumPadVisibility()
            else
                requireActivity().finish()
        }

        binding.cardView.visibility = View.GONE

        binding.fabOpenNumpad.setOnClickListener {
            changeNumPadVisibility()
        }

        binding.numPad.setOnCancelButtonClickListener {
            findNavController().navigateUp()
            changeNumPadVisibility()
        }

        if (!args.callerNumber.isNullOrEmpty()) {
            toolbarBinding.logoLeftImage.visibility = View.GONE
            toolbarBinding.textView.visibility = View.GONE
            binding.numPad.setNumber(args.callerNumber!!)
            toast(args.callerName)
            changeNumPadVisibility()
        } else {
            toolbarBinding.textView.visibility = View.VISIBLE
            toolbarBinding.logoLeftImage.visibility = View.VISIBLE
        }

        if (isPermissionGranted)
            binding.fabOpenNumpad.backgroundTintList =
                ColorStateList.valueOf(getColor(requireContext(), R.color.colorGreen))
        else
            binding.fabOpenNumpad.backgroundTintList = ColorStateList.valueOf(
                getColor(requireContext(), R.color.colorGray)
            )

        binding.rvCallRecords.layoutManager =
            LinearLayoutManager(
                context, LinearLayoutManager.VERTICAL, false
            )

        historyAdapter = CallHistoryAdapter(requireContext())

        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(historyAdapter))
        itemTouchHelper.attachToRecyclerView(binding.rvCallRecords)

        binding.rvCallRecords.adapter = historyAdapter

        viewModel.callHistory.asFlow().asLiveData().observe(this) { callHistory ->
            historyAdapter.setRecords(callHistory.executeAsList())
        }
    }

    private fun getScreenWidth(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowMetrics = requireActivity().windowManager.currentWindowMetrics
            val insets: Insets = windowMetrics.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            windowMetrics.bounds.width() - insets.left - insets.right
        } else {
            val displayMetrics = DisplayMetrics()
            requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
            displayMetrics.widthPixels
        }
    }

    private fun changeNumPadVisibility() {
        val screenW = getScreenWidth()
        val margin = binding.fabOpenNumpad.marginRight
        val fabWidth = binding.fabOpenNumpad.width
        val mid = (screenW / 2 - margin).toFloat() - fabWidth / 5.5f

        if (isNumPadUp) {
            binding.cardView.slideDown()
            binding.fabOpenNumpad.setOnClickListener {
                changeNumPadVisibility()
            }
            binding.numPad.setOnCancelButtonClickListener {}

            binding.fabOpenNumpad.expand()
            binding.fabOpenNumpad.setIconActionButton(R.drawable.ic_baseline_dialpad_24)

            val buttonAnimator =
                ObjectAnimator.ofFloat(binding.fabOpenNumpad, "translationX", -mid, 0f)
            buttonAnimator.duration = 500
            buttonAnimator.interpolator = OvershootInterpolator()
            buttonAnimator.start()
        } else {
            binding.fabOpenNumpad.setIconActionButton(R.drawable.ic_baseline_dialer_sip_24_white)
            binding.fabOpenNumpad.collapse()

            val buttonAnimator =
                ObjectAnimator.ofFloat(binding.fabOpenNumpad, "translationX", 0f, -mid)
            buttonAnimator.duration = 500
            buttonAnimator.interpolator = OvershootInterpolator()
            buttonAnimator.start()

            binding.fabOpenNumpad.setOnClickListener {
                if (binding.numPad.getInputNumber().length > 3)
                    CallActivity.create(requireContext(), binding.numPad.getInputNumber(), false)
                else
                    toast(getString(R.string.no_name_error))
            }

            binding.numPad.setOnCancelButtonClickListener {
                findNavController().navigateUp()
                changeNumPadVisibility()
            }

            binding.cardView.slideUp()
        }

        isNumPadUp = !isNumPadUp
    }

    override fun onPause() {
        super.onPause()
        binding.numPad.clear()
    }

    private fun setupToolbar() {
        val navController = findNavController()
        val appBarConfig = AppBarConfiguration(navController.graph)
        val navHostFragment = NavHostFragment.findNavController(this)
        NavigationUI.setupWithNavController(toolbarBinding.toolbar, navHostFragment, appBarConfig)
        (activity as AppCompatActivity).setSupportActionBar(toolbarBinding.toolbar)
//        (activity as AppCompatActivity).supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_backspace_24);
    }
}