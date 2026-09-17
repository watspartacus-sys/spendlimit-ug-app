package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Autorenew
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.UgandaFlagBadge
import com.example.ui.screens.AccountsScreen
import com.example.ui.screens.AddLimitDialog
import com.example.ui.screens.AddRuleDialog
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.RulesScreen
import com.example.ui.screens.SimulateExpenseDialog
import com.example.ui.screens.TransactionReceiptDialog
import com.example.ui.screens.WithdrawalPromptDialog
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.UgandaGold
import com.example.ui.theme.UgandaGreenPrimary
import com.example.ui.viewmodel.SpendLimitViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: SpendLimitViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val limits by viewModel.limits.collectAsStateWithLifecycle()
                val rules by viewModel.rules.collectAsStateWithLifecycle()
                val accounts by viewModel.accounts.collectAsStateWithLifecycle()
                val transactions by viewModel.transactions.collectAsStateWithLifecycle()
                val isAutomationActive by viewModel.isAutomationActive.collectAsStateWithLifecycle()

                val withdrawalAlert by viewModel.withdrawalAlert.collectAsStateWithLifecycle()
                val showAddLimitDialog by viewModel.showAddLimitDialog.collectAsStateWithLifecycle()
                val showAddRuleDialog by viewModel.showAddRuleDialog.collectAsStateWithLifecycle()
                val showSimulateExpenseDialog by viewModel.showSimulateExpenseDialog.collectAsStateWithLifecycle()
                val selectedLimitForExpense by viewModel.selectedLimitForExpense.collectAsStateWithLifecycle()
                val selectedTransaction by viewModel.selectedTransaction.collectAsStateWithLifecycle()

                var selectedTab by rememberSaveable { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UgandaFlagBadge()
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "SpendLimit UG",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            },
                            actions = {
                                FilterChip(
                                    selected = isAutomationActive,
                                    onClick = { viewModel.toggleAutomationActive() },
                                    label = {
                                        Text(
                                            text = if (isAutomationActive) "AUTO ON" else "PAUSED",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 10.sp
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PowerSettingsNew,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = UgandaGreenPrimary
                                    ),
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 6.dp
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = {
                                    Icon(
                                        if (selectedTab == 0) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                                        contentDescription = "Limits"
                                    )
                                },
                                label = { Text("Limits", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_tab_dashboard")
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = {
                                    Icon(
                                        if (selectedTab == 1) Icons.Default.Autorenew else Icons.Outlined.Autorenew,
                                        contentDescription = "Auto-Rules"
                                    )
                                },
                                label = { Text("Auto-Rules", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_tab_rules")
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = {
                                    Icon(
                                        if (selectedTab == 2) Icons.Default.AccountBalance else Icons.Outlined.AccountBalance,
                                        contentDescription = "SIMs & Banks"
                                    )
                                },
                                label = { Text("SIMs & Bank", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_tab_accounts")
                            )
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = {
                                    Icon(
                                        if (selectedTab == 3) Icons.Default.History else Icons.Outlined.History,
                                        contentDescription = "Activity"
                                    )
                                },
                                label = { Text("Activity", fontSize = 11.sp) },
                                modifier = Modifier.testTag("nav_tab_history")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (selectedTab) {
                            0 -> DashboardScreen(
                                limits = limits,
                                rules = rules,
                                accounts = accounts,
                                isAutomationActive = isAutomationActive,
                                onToggleAutomation = { viewModel.toggleAutomationActive() },
                                onOpenAddLimit = { viewModel.openAddLimitDialog() },
                                onOpenExpenseDialog = { viewModel.openExpenseDialog(it) },
                                onResetAllSpending = { viewModel.resetAllLimits() },
                                onDeleteLimit = { viewModel.deleteLimit(it) }
                            )
                            1 -> RulesScreen(
                                rules = rules,
                                limits = limits,
                                accounts = accounts,
                                onToggleRule = { id, enabled -> viewModel.toggleRule(id, enabled) },
                                onTestRule = { viewModel.executeRuleTest(it) },
                                onDeleteRule = { viewModel.deleteRule(it) },
                                onOpenAddRule = { viewModel.openAddRuleDialog() }
                            )
                            2 -> AccountsScreen(
                                accounts = accounts,
                                onDialUssd = { ctx, code, prov -> viewModel.dialUgandaUssd(ctx, code, prov) }
                            )
                            3 -> HistoryScreen(
                                transactions = transactions,
                                onSelectTransaction = { viewModel.selectTransaction(it) },
                                onClearHistory = { viewModel.clearAllHistory() }
                            )
                        }

                        // Dialogs
                        if (showAddLimitDialog) {
                            AddLimitDialog(
                                onDismiss = { viewModel.closeAddLimitDialog() },
                                onSave = { title, cat, period, amount, warn, trigger ->
                                    viewModel.createLimit(title, cat, period, amount, warn, trigger)
                                }
                            )
                        }

                        if (showAddRuleDialog) {
                            AddRuleDialog(
                                limits = limits,
                                accounts = accounts,
                                onDismiss = { viewModel.closeAddRuleDialog() },
                                onSave = { name, lId, lTitle, trig, sType, sId, sName, dType, dName, aType, amt, method ->
                                    viewModel.createRule(name, lId, lTitle, trig, sType, sId, sName, dType, dName, aType, amt, method)
                                }
                            )
                        }

                        if (showSimulateExpenseDialog) {
                            SimulateExpenseDialog(
                                limits = limits,
                                accounts = accounts,
                                preselectedLimit = selectedLimitForExpense,
                                onDismiss = { viewModel.closeExpenseDialog() },
                                onSubmit = { lId, amt, title, src ->
                                    viewModel.recordExpense(lId, amt, title, src)
                                    viewModel.closeExpenseDialog()
                                }
                            )
                        }

                        // Alert when an automated withdrawal triggers from spending limits
                        withdrawalAlert?.let { alertEvent ->
                            WithdrawalPromptDialog(
                                event = alertEvent,
                                onDismiss = { viewModel.dismissWithdrawalAlert() }
                            )
                        }

                        // Transaction Voucher Receipt
                        selectedTransaction?.let { tx ->
                            TransactionReceiptDialog(
                                transaction = tx,
                                onDismiss = { viewModel.selectTransaction(null) }
                            )
                        }
                    }
                }
            }
        }
    }
}
