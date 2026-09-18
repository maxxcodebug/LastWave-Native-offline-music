// Copyright (C) 2026 Anshuman X (maxxcodebug)

package com.lastwave.app.ui.localmusic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun OfflineModeTransitionOverlay(
    goingOffline: Boolean,
    onSwitch: () -> Unit,
    onFinished: () -> Unit,
) {
    var closing by remember(goingOffline) { mutableStateOf(false) }
    val contentScale by animateFloatAsState(
        targetValue = if (closing) 0.78f else 1f,
        animationSpec = spring(
            dampingRatio = 0.58f,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "offlineTransitionScale",
    )

    LaunchedEffect(goingOffline) {
        delay(520)
        onSwitch()
        delay(520)
        closing = true
        delay(300)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .zIndex(100f),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = !closing,
            enter = fadeIn(tween(180)) + scaleIn(
                animationSpec = spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMediumLow),
                initialScale = 0.72f,
            ),
            exit = fadeOut(tween(180)) + scaleOut(
                animationSpec = spring(dampingRatio = 0.52f, stiffness = Spring.StiffnessMediumLow),
                targetScale = 0.82f,
            ),
        ) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 8.dp,
                shadowElevation = 14.dp,
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 32.dp, vertical = 34.dp)
                        .scale(contentScale),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = if (goingOffline) Icons.Filled.MusicNote else Icons.Filled.Home,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.size(20.dp))
                    Text(
                        text = if (goingOffline) "You're going offline" else "You're leaving Offline Mode",
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = if (goingOffline) {
                            "Online music, YouTube and other internet content will disappear."
                        } else {
                            "The official player and online music experience are coming back."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
