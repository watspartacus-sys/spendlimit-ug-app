package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.SpendingLimitEntity
import com.example.data.model.UgandaAccountEntity
import com.example.data.repository.SpendLimitRepository
import com.example.ui.theme.UgandaGold
import com.example.ui.theme.UgandaRuby

@Composable
fun SimulateExpenseDialog(
    limits: List<SpendingLimitEntity>,
    accounts: List<UgandaAccountEntity>,
    preselectedLimit: SpendingLimitEntity?,
    onDismiss: () -> Unit,
    onSubmit: (limitId: Long, amountUgx: Double, title: String, sourceAccount: String) -> Unit
) {
    var selectedLimitId by remember {
        mutableStateOf(preselectedLimit?.id ?: limits.firstOrNull()?.id ?: 0L)
    }
    var expenseTitle by remember { mutableStateOf("Boda-boda ride / Groceries") }
    var amountText by remember { mutableStateOf("20000") }
    var selectedSourceAccount by remember {
        mutableStateOf(accounts.firstOrNull { it.accountType != "VAULT" }?.providerName ?: "MTN Mobile Money")
    }

    val currentLimit = limits.find { it.id == selectedLimitId }
    val quickAmounts = listOf("10000", "20000", "35000", "50000")

    val enteredAmount = amountText.toDoubleOrNull() ?: 0.0
    val projectedSpent = (currentLimit?.currentSpentUgx ?: 0.0) + enteredAmount
    val limitAmount = currentLimit?.limitAmountUgx ?: 1.0
    val projectedPercent = ((projectedSpent / limitAmount) * 100).toInt()
    val willExceed = projectedPercent >= (currentLimit?.autoWithdrawTriggerPercent ?: 100)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Log Spending Event",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Record an actual or simulated transaction. If this causes your limit to be breached, your automated withdrawal rule will immediately trigger!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Charge Against Spending Limit:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    limits.forEach { limit ->
                        FilterChip(
                            selected = selectedLimitId == limit.id,
                            onClick = { selectedLimitId = limit.id },
                            label = { Text("${limit.title} (${SpendLimitRepository.formatCompactUgx(limit.limitAmountUgx)})") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = expenseTitle,
                    onValueChange = { expenseTitle = it },
                    label = { Text("Expense Description") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) amountText = it },
                    label = { Text("Amount (UGX)") },
                    prefix = { Text("UGX ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("expense_amount_input"),
                    singleLine = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    quickAmounts.forEach { q ->
                        SuggestionChip(
                            onClick = { amountText = q },
                            label = { Text(q, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Payment Method in Uganda:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    accounts.filter { it.accountType != "VAULT" }.take(3).forEach { acc ->
                        FilterChip(
                            selected = selectedSourceAccount == acc.providerName,
                            onClick = { selectedSourceAccount = acc.providerName },
                            label = { Text(acc.providerName.take(12), style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                // Projection Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (willExceed) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (willExceed) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = UgandaRuby,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Auto-Withdrawal Will Trigger!",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = UgandaRuby
                                )
                            } else {
                                Text(
                                    text = "Within Spending Safe Zone",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Projected Spend: ${SpendLimitRepository.formatUgx(projectedSpent)} / ${SpendLimitRepository.formatUgx(limitAmount)} ($projectedPercent%)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enteredAmount > 0) {
                        onSubmit(selectedLimitId, enteredAmount, expenseTitle, selectedSourceAccount)
                    }
                },
                modifier = Modifier.testTag("submit_expense_button")
            ) {
                Text("Process Spending")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
