package com.sanket.runapp.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.sanket.runapp.R
import com.sanket.runapp.ui.view_models.StatsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatsFragment : Fragment(R.layout.fragment_stats) {

    private val viewModel: StatsViewModel by viewModels()
}