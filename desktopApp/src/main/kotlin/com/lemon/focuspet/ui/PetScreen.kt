package com.lemon.focuspet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.WindowState
import com.lemon.focuspet.viewmodel.PomodoroViewModel

@Composable
fun PetScreen(viewModel: PomodoroViewModel, windowState: WindowState) {
    // Track window position for dragging (Dp because WindowPosition.Absolute uses Dp)
    var windowX by remember { mutableStateOf(0.dp) }
    var windowY by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    LaunchedEffect(Unit) {
        val pos = windowState.position
        if (pos is WindowPosition.Absolute) {
            windowX = pos.x
            windowY = pos.y
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    with(density) {
                        windowX += dragAmount.x.toDp()
                        windowY += dragAmount.y.toDp()
                    }
                    windowState.position = WindowPosition.Absolute(windowX, windowY)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier.size(180.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // ── Pet emoji ──
                Text(
                    text = viewModel.petState.emoji,
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                // ── Timer ──
                val sessionLabel = if (viewModel.isBreak) "休息" else "专注"
                Text(
                    text = formatTime(viewModel.remainingSeconds),
                    fontSize = 26.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = sessionLabel,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(Modifier.height(8.dp))

                // ── Controls ──
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (viewModel.isRunning) {
                        SmallButton(onClick = { viewModel.pause() }) {
                            Text("⏸", fontSize = 16.sp)
                        }
                    } else {
                        SmallButton(onClick = { viewModel.start() }) {
                            Text("▶", fontSize = 16.sp)
                        }
                    }
                    SmallButton(onClick = { viewModel.reset() }) {
                        Text("↺", fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(6.dp))

                // ── Statistics ──
                Text(
                    text = "🔥 ${viewModel.totalMinutes} 分钟",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SmallButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.size(34.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            content()
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
