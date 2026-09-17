package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.SpendingLimitEntity
import com.example.data.model.UgandaAccountEntity
import com.example.data.model.WithdrawalRuleEntity
import com.example.ui.screens.DashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        Scaffold { padding ->
          Box(modifier = Modifier.padding(padding)) {
            DashboardScreen(
              limits = listOf(
                SpendingLimitEntity(
                  id = 1,
                  title = "Daily Total Spend",
                  category = "General",
                  period = "DAILY",
                  limitAmountUgx = 60000.0,
                  currentSpentUgx = 45000.0,
                  warningThresholdPercent = 80,
                  autoWithdrawTriggerPercent = 100,
                  isActive = true
                )
              ),
              rules = listOf(
                WithdrawalRuleEntity(
                  id = 1,
                  ruleName = "Stanbic Auto-Sweep on Daily Limit",
                  linkedLimitId = 1,
                  limitTitle = "Daily Total Spend",
                  triggerType = "ON_LIMIT_EXCEEDED",
                  sourceType = "BANK_ACCOUNT",
                  sourceId = "bank_stanbic",
                  sourceName = "Stanbic Bank UG (*2910)",
                  destinationType = "LOCKED_SAVINGS_VAULT",
                  destinationName = "Locked Savings Vault",
                  withdrawalAmountType = "FIXED_AMOUNT",
                  amountUgx = 20000.0,
                  isEnabled = true
                )
              ),
              accounts = listOf(
                UgandaAccountEntity(
                  id = "sim_mtn",
                  accountType = "SIM_CARD",
                  providerName = "MTN Mobile Money",
                  accountNumber = "+256 772 459 812",
                  balanceUgx = 185000.0,
                  ussdCode = "*165#",
                  simSlot = 0
                ),
                UgandaAccountEntity(
                  id = "vault_safe",
                  accountType = "VAULT",
                  providerName = "Locked Savings Vault",
                  accountNumber = "UGX-SAFE-VAULT-01",
                  balanceUgx = 420000.0
                )
              ),
              isAutomationActive = true,
              onToggleAutomation = {},
              onOpenAddLimit = {},
              onOpenExpenseDialog = {},
              onResetAllSpending = {},
              onDeleteLimit = {}
            )
          }
        }
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
