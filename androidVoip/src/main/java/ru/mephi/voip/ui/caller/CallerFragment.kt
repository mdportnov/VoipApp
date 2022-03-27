package ru.mephi.voip.ui.caller

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ru.mephi.voip.databinding.FragmentCallerBinding

class CallerFragment : Fragment() {
    private lateinit var binding: FragmentCallerBinding
    private var isPermissionGranted by mutableStateOf(true)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCallerBinding.inflate(inflater, container, false)
        val args: CallerFragmentArgs by navArgs()

        binding.composeCallerScreen.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                CallerScreen(isPermissionGranted, findNavController(), args)
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermissions()
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
}
