package com.nexus.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NexusScaffold(
    modifier: Modifier = Modifier,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold { innerPadding ->
        Surface(
            modifier = modifier,
            color = MaterialTheme.colorScheme.background,
        ) {
            content(innerPadding)
        }
    }
}
