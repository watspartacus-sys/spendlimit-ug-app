package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SpendLimitRepository
import com.example.ui.theme.BrandAirtelRed
import com.example.ui.theme.BrandCentenaryGreen
import com.example.ui.theme.BrandMtnYellow
import com.example.ui.theme.BrandStanbicBlue
import com.example.ui.theme.BrandVaultPurple
import com.example.ui.theme.UgandaGold
import com.example.ui.theme.UgandaGreenPrimary
import com.example.ui.theme.UgandaRuby

@Composable
fun UgandaFlagBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(0.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Uganda Flag 6 stripes: Black, Yellow, Red, Black, Yellow, Red
        val stripeColors = listOf(
            Color(0xFF111111),
            Color(0xFFFFCD00),
            Color(0xFFD90000),
            Color(0xFF111111),
            Color(0xFFFFCD00),
            Color(0xFFD90000)
        )
        Column(modifier = Modifier.size(width = 18.dp, height = 14.dp)) {
            stripeColors.forEach { color ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun ProviderLogoBadge(
    providerName: String,
    modifier: Modifier = Modifier,
    size: Int = 36
) {
    val (bgColor, textColor, icon) = when {
        providerName.contains("MTN", ignoreCase = true) ->
            Triple(BrandMtnYellow, Color.Black, Icons.Default.SimCard)
        providerName.contains("Airtel", ignoreCase = true) ->
            Triple(BrandAirtelRed, Color.White, Icons.Default.SimCard)
        providerName.contains("Stanbic", ignoreCase = true) ->
            Triple(BrandStanbicBlue, Color.White, Icons.Default.AccountBalance)
        providerName.contains("Centenary", ignoreCase = true) ->
            Triple(BrandCentenaryGreen, Color.White, Icons.Default.AccountBalance)
        providerName.contains("Vault", ignoreCase = true) ->
            Triple(BrandVaultPurple, Color.White, Icons.Default.Lock)
        else ->
            Triple(UgandaGreenPrimary, Color.White, Icons.Default.AccountBalance)
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = providerName,
            tint = textColor,
            modifier = Modifier.size((size * 0.55).dp)
        )
    }
}

@Composable
fun CategoryIcon(category: String, modifier: Modifier = Modifier) {
    val icon = when (category.lowercase()) {
        "dining", "food" -> Icons.Default.Fastfood
        "transport", "fuel", "boda" -> Icons.Default.DirectionsCar
        "shopping" -> Icons.Default.ShoppingBag
        "utilities", "airtime", "yaka" -> Icons.Default.Wifi
        "nightlife", "entertainment" -> Icons.Default.Receipt
        else -> Icons.Default.Security
    }

    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = category,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun LimitProgressBar(
    currentSpent: Double,
    limitAmount: Double,
    warningPercent: Int,
    triggerPercent: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (limitAmount > 0) (currentSpent / limitAmount).toFloat() else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "progress"
    )

    val progressColor by animateColorAsState(
        targetValue = when {
            progress >= (triggerPercent / 100f) -> UgandaRuby
            progress >= (warningPercent / 100f) -> UgandaGold
            else -> UgandaGreenPrimary
        },
        animationSpec = tween(durationMillis = 400),
        label = "progressColor"
    )

    Column(modifier = modifier) {
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = progressColor,
            trackColor = progressColor.copy(alpha = 0.18f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${(progress * 100).toInt()}% spent",
                style = MaterialTheme.typography.labelSmall,
                color = progressColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Limit: ${SpendLimitRepository.formatUgx(limitAmount)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun TransferFlowBadge(
    sourceName: String,
    destinationName: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = sourceName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 6.dp)
                    .size(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = destinationName,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
