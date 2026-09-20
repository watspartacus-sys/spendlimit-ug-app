package com.example.spendlimitug

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.SpendLimitUGTheme
import com.example.viewmodel.SpendLimitViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpendLimitUGTheme {
                val vm: SpendLimitViewModel = viewModel()
                val state by vm.uiState.collectAsState()
                Scaffold { padding ->
                    Column(Modifier.padding(padding).padding(16.dp)) {
                        Text("SpendLimit UG", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(16.dp))
                        Text("Daily Limit: UGX ${state.dailyLimit}")
                        Text("Today Spent: UGX ${state.todaySpent}")
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { vm.updateLimit(100000) }) {
                            Text("Set 100k Limit")
                        }
                    }
                }
            }
        }
    }
}