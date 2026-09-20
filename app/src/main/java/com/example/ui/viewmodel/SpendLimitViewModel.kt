package com.example.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class SpendLimitUiState(
    val dailyLimit: Int = 50000,
    val todaySpent: Int = 0
)

class SpendLimitViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SpendLimitUiState())
    val uiState: StateFlow<SpendLimitUiState> = _uiState

    fun updateLimit(newLimit: Int) {
        _uiState.value = _uiState.value.copy(dailyLimit = newLimit)
    }
}