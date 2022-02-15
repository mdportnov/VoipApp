package ru.mephi.voip.ui.caller

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Insets
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.core.content.ContextCompat.getColor
import androidx.core.graphics.drawable.DrawableCompat
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
import com.squareup.sqldelight.runtime.coroutines.asFlow
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import ru.mephi.shared.appContext
import ru.mephi.shared.data.sip.AccountStatus
import ru.mephi.shared.vm.CallerViewModel
import ru.mephi.voip.R
import ru.mephi.voip.data.AccountStatusRepository
import ru.mephi.voip.databinding.FragmentCallerBinding
import ru.mephi.voip.databinding.ToolbarCallerBinding
import ru.mephi.voip.ui.call.CallActivity
import ru.mephi.voip.ui.caller.adapter.CallHistoryAdapter
import ru.mephi.voip.ui.caller.adapter.SwipeToDeleteCallback
import ru.mephi.voip.utils.showSnackBar
import ru.mephi.voip.utils.toast
import timber.log.Timber

@ExperimentalAnimationApi
class CallerFragment : Fragment() {
    private val viewModel: CallerViewModel by inject()
    private val accountStatusRepository: AccountStatusRepository by inject()

    private lateinit var historyAdapter: CallHistoryAdapter

    private lateinit var binding: FragmentCallerBinding
    private lateinit var toolbarBinding: ToolbarCallerBinding

    private var isPermissionGranted by mutableStateOf(true)

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        historyAdapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCallerBinding.inflate(inflater, container, false)
        toolbarBinding = binding.toolbarCaller

        binding.numPadCompose.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                NumPad(
                    5, mutableInputState, mutableNumPadState, isPermissionGranted,
                    onTapWhenUp = { line ->
                        CallActivity.create(requireContext(), line, false)
                    },
                    onLimitExceeded = {
                        showSnackBar(this, "Превышен размер номера")
                    })
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermissions()
        setupToolbar()
        initViews()
        initStatusObserver()
    }

    @SuppressLint("UnsafeRepeatOnLifecycleDetector")
    private fun initStatusObserver() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                accountStatusRepository.status.collect { status ->
                    Timber.d("Getting status from SharedViewModel ${status.status}")

                    toolbarBinding.statusText.text = status.status
                    val unwrappedDrawable =
                        AppCompatResources.getDrawable(appContext, R.drawable.shape_circle)!!
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
            requestPermissions(permissions.toTypedArray(), 1)
    }

    private var mutableInputState by mutableStateOf("")
    private var mutableNumPadState by mutableStateOf(false)

    private fun initViews() {
        val args: CallerFragmentArgs by navArgs()

        if (!args.callerNumber.isNullOrEmpty()) {
            toolbarBinding.logoLeftImage.visibility = View.GONE
            toolbarBinding.textView.visibility = View.GONE
            mutableInputState = args.callerNumber!!
            mutableNumPadState = true
            toast(args.callerName)
        } else {
            toolbarBinding.textView.visibility = View.VISIBLE
            toolbarBinding.logoLeftImage.visibility = View.VISIBLE
        }

        binding.rvCallRecords.layoutManager =
            LinearLayoutManager(
                context, LinearLayoutManager.VERTICAL, false
            )

        historyAdapter = CallHistoryAdapter(requireContext())

        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(historyAdapter))
        itemTouchHelper.attachToRecyclerView(binding.rvCallRecords)

        binding.rvCallRecords.adapter = historyAdapter

        viewModel.callHistory.asFlow().asLiveData().observe(viewLifecycleOwner) { callHistory ->
            historyAdapter.setRecords(callHistory.executeAsList())
        }
    }

    private fun setupToolbar() {
        val navController = findNavController()
        val appBarConfig = AppBarConfiguration(navController.graph)
        val navHostFragment = NavHostFragment.findNavController(this)
        NavigationUI.setupWithNavController(toolbarBinding.toolbar, navHostFragment, appBarConfig)
        (activity as AppCompatActivity).setSupportActionBar(toolbarBinding.toolbar)
    }
}