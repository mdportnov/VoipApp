package ru.mephi.voip.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import coil.annotation.ExperimentalCoilApi
import org.koin.androidx.viewmodel.ext.android.viewModel

@ExperimentalComposeUiApi
@ExperimentalCoilApi
@ExperimentalMaterialApi
class ProfileFragment : Fragment() {
    private val viewModel: ProfileViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                ProfileScreen(findNavController())
            }
        }
    }
}

