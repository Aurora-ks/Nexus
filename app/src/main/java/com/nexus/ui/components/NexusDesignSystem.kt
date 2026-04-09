package com.nexus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexus.ui.theme.AccentOnPrimary
import com.nexus.ui.theme.AccentPrimary
import com.nexus.ui.theme.BackgroundWarm
import com.nexus.ui.theme.BorderSubtle
import com.nexus.ui.theme.DestructiveOnPrimary
import com.nexus.ui.theme.DestructivePrimary
import com.nexus.ui.theme.ErrorBackground
import com.nexus.ui.theme.ErrorForeground
import com.nexus.ui.theme.SuccessBackground
import com.nexus.ui.theme.SuccessForeground
import com.nexus.ui.theme.SurfaceCard
import com.nexus.ui.theme.SurfaceInput
import com.nexus.ui.theme.TextMuted
import com.nexus.ui.theme.TextPrimary
import com.nexus.ui.theme.TextSecondary
import com.nexus.ui.theme.WarningBackground
import com.nexus.ui.theme.WarningForeground

enum class NexusStatusTone {
    Success,
    Warning,
    Error,
}

data class NexusBottomBarItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun NexusPage(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .background(BackgroundWarm)
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        content = content,
    )
}

@Composable
fun NexusPanel(
    modifier: Modifier = Modifier,
    containerColor: Color = SurfaceCard,
    borderColor: Color = BorderSubtle,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
    }
}

@Composable
fun NexusPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    NexusActionButton(
        label = label,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        enabled = enabled,
        containerColor = AccentPrimary,
        contentColor = AccentOnPrimary,
    )
}

@Composable
fun NexusSecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    NexusActionButton(
        label = label,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        enabled = enabled,
        containerColor = SurfaceInput,
        contentColor = TextPrimary,
    )
}

@Composable
fun NexusDestructiveButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    NexusActionButton(
        label = label,
        onClick = onClick,
        modifier = modifier,
        icon = icon,
        enabled = enabled,
        containerColor = DestructivePrimary,
        contentColor = DestructiveOnPrimary,
    )
}

@Composable
private fun NexusActionButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier,
    icon: ImageVector?,
    enabled: Boolean,
    containerColor: Color,
    contentColor: Color,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        enabled = enabled,
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.55f),
            disabledContentColor = contentColor.copy(alpha = 0.7f),
        ),
        contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
fun NexusStatusChip(
    text: String,
    tone: NexusStatusTone,
    modifier: Modifier = Modifier,
) {
    val containerColor = when (tone) {
        NexusStatusTone.Success -> SuccessBackground
        NexusStatusTone.Warning -> WarningBackground
        NexusStatusTone.Error -> ErrorBackground
    }
    val contentColor = when (tone) {
        NexusStatusTone.Success -> SuccessForeground
        NexusStatusTone.Warning -> WarningForeground
        NexusStatusTone.Error -> ErrorForeground
    }
    Surface(
        modifier = modifier,
        color = containerColor,
        shape = RoundedCornerShape(999.dp),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}

@Composable
fun NexusEmptyStateCard(
    title: String,
    description: String? = null,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    NexusPanel(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = BackgroundWarm,
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
            )
            if (!description.isNullOrBlank()) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun NexusLabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = TextPrimary),
            singleLine = true,
            cursorBrush = SolidColor(TextPrimary),
            decorationBox = { innerTextField ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = SurfaceInput,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        if (value.isEmpty() && !placeholder.isNullOrBlank()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextMuted,
                            )
                        }
                        innerTextField()
                    }
                }
            },
        )
    }
}

@Composable
fun NexusBottomBar(
    items: List<NexusBottomBarItem>,
    currentRoute: String?,
    onSelected: (NexusBottomBarItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(BackgroundWarm)
            .padding(top = 12.dp, start = 21.dp, end = 21.dp, bottom = 21.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(36.dp),
            color = BackgroundWarm,
            border = BorderStroke(1.dp, BorderSubtle),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                items.forEach { item ->
                    val selected = item.route == currentRoute
                    BottomBarItem(
                        item = item,
                        selected = selected,
                        onClick = { onSelected(item) },
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.BottomBarItem(
    item: NexusBottomBarItem,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(26.dp))
            .background(if (selected) AccentPrimary else Color.Transparent)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .height(54.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                modifier = Modifier.size(18.dp),
                tint = if (selected) AccentOnPrimary else TextMuted,
            )
            Text(
                text = item.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = if (selected) AccentOnPrimary else TextMuted,
            )
        }
    }
}

@Composable
fun NexusAvatar(
    label: String,
    modifier: Modifier = Modifier,
    overlay: @Composable BoxScope.() -> Unit = {},
) {
    Surface(
        modifier = modifier.size(40.dp),
        shape = CircleShape,
        color = SurfaceInput,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = label.take(1),
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
            )
            overlay()
        }
    }
}
