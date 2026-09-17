package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.SpendingLimitEntity
import com.example.data.model.UgandaAccountEntity

@Composable
fun AddRuleDialog(
    limits: List<SpendingLimitEntity>,
    accounts: List<UgandaAccountEntity>,
    onDismiss: () -> Unit,
    onSave: (
        ruleName: String,
        linkedLimitId: Long,
        limitTitle: String,
        triggerType: String,
        sourceType: String,
        sourceId: String,
        sourceName: String,
        destinationType: String,
        destinationName: String,
        withdrawalAmountType: String,
        amountUgx: Double,
        method: String
    ) -> Unit
) {
    var ruleName by remember { mutableStateOf("") }
    var selectedLimitId by remember { mutableStateOf(limits.firstOrNull()?.id ?: 0L) }
    var triggerType by remember { mutableStateOf("ON_LIMIT_EXCEEDED") }
    var selectedSourceId by remember { mutableStateOf(accounts.firstOrNull { it.accountType != "VAULT" }?.id ?: "bank_stanbic") }
    var destinationType by remember { mutableStateOf("LOCKED_SAVINGS_VAULT") }
    var amountType by remember { mutableStateOf("FIXED_AMOUNT") }
    var amountText by remember { mutableStateOf("25000") }
    var selectedMethod by remember { mutableStateOf("STK_PUSH_MOMO") }

    val quickAmounts = listOf("10000", "20000", "50000", "100000")

    val selectedLimit = limits.find { it.id == selectedLimitId }
    val selectedSource = accounts.find { it.id == selectedSourceId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Auto-Withdraw Rule",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
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
                    text = "Automatically withdraw funds from your bank account or Uganda SIM card when spending thresholds are breached.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = ruleName,
                    onValueChange = { ruleName = it },
                    label = { Text("Rule Name") },
                    placeholder = { Text("e.g. Stanbic Auto-Withdraw on Lunch Cap") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rule_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "1. Trigger When This Limit is Breached:",
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
                            label = { Text("${limit.title} (${limit.period})") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "2. Withdraw From (Source):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Column(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    accounts.filter { it.accountType != "VAULT" }.forEach { acc ->
                        FilterChip(
                            selected = selectedSourceId == acc.id,
                            onClick = {
                                selectedSourceId = acc.id
                                selectedMethod = if (acc.accountType == "SIM_CARD") "STK_PUSH_MOMO" else "BANK_DIRECT_DEBIT"
                            },
                            label = { Text("${acc.providerName} (${if (acc.accountType == "SIM_CARD") "SIM ${acc.simSlot + 1}" else "Bank"})") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "3. Deposit Into (Destination):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = destinationType == "LOCKED_SAVINGS_VAULT",
                        onClick = { destinationType = "LOCKED_SAVINGS_VAULT" },
                        label = { Text("Locked Vault") }
                    )
                    FilterChip(
                        selected = destinationType == "BANK_ACCOUNT",
                        onClick = { destinationType = "BANK_ACCOUNT" },
                        label = { Text("Bank") }
                    )
                    FilterChip(
                        selected = destinationType == "SIM_CARD",
                        onClick = { destinationType = "SIM_CARD" },
                        label = { Text("SIM Card") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "4. Amount to Automatically Withdraw:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = amountType == "FIXED_AMOUNT",
                        onClick = { amountType = "FIXED_AMOUNT" },
                        label = { Text("Fixed UGX") }
                    )
                    FilterChip(
                        selected = amountType == "EXACT_OVERSPENT_AMOUNT",
                        onClick = { amountType = "EXACT_OVERSPENT_AMOUNT" },
                        label = { Text("Exact Overspent") }
                    )
                }

                if (amountType == "FIXED_AMOUNT") {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { if (it.all { char -> char.isDigit() }) amountText = it },
                        label = { Text("Withdrawal Amount (UGX)") },
                        prefix = { Text("UGX ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("rule_amount_input"),
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
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "5. Uganda Telecom / Bank Protocol:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedMethod == "STK_PUSH_MOMO",
                        onClick = { selectedMethod = "STK_PUSH_MOMO" },
                        label = { Text("MoMo Push") }
                    )
                    FilterChip(
                        selected = selectedMethod == "BANK_DIRECT_DEBIT",
                        onClick = { selectedMethod = "BANK_DIRECT_DEBIT" },
                        label = { Text("Direct Debit") }
                    )
                    FilterChip(
                        selected = selectedMethod == "USSD_PROMPT",
                        onClick = { selectedMethod = "USSD_PROMPT" },
                        label = { Text("USSD *165#") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 25000.0
                    val sName = selectedSource?.providerName ?: "Uganda Bank"
                    val sType = selectedSource?.accountType ?: "BANK_ACCOUNT"
                    val dName = when (destinationType) {
                        "LOCKED_SAVINGS_VAULT" -> "Locked Savings Vault"
                        "BANK_ACCOUNT" -> "Stanbic Bank UG"
                        else -> "MTN MoMo (SIM 1)"
                    }
                    val finalRuleName = ruleName.ifBlank {
                        "Auto-withdraw $sName to $dName"
                    }
                    onSave(
                        finalRuleName,
                        selectedLimitId,
                        selectedLimit?.title ?: "All Limits",
                        triggerType,
                        sType,
                        selectedSourceId,
                        sName,
                        destinationType,
                        dName,
                        amountType,
                        amount,
                        selectedMethod
                    )
                },
                modifier = Modifier.testTag("save_rule_button")
            ) {
                Text("Create Rule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
