package com.sanket.runapp.ui.view_models

import android.view.View
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sanket.runapp.db.Run
import com.sanket.runapp.repositories.MainRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    val mainRepository: MainRepo
): ViewModel() {

    fun insertRun(run : Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
}