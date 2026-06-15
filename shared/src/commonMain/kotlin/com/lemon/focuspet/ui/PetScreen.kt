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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.focuspet.util.DesktopEnv
import com.lemon.focuspet.viewmodel.PomodoroViewModel

@Composable
fun PetScreen(viewModel: PomodoroViewModel, env: DesktopEnv) {
    // Use absolute mouse coordinates to avoid delta accumulation jitter
    var dragOriginMouseX by remember { mutableIntStateOf(0) }
    var dragOriginMouseY by remember { mutableIntStateOf(0) }
    var dragOriginWindowX by remember { mutableIntStateOf(0) }
    var dragOriginWindowY by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        dragOriginMouseX = env.mouseScreenX
                        dragOriginMouseY = env.mouseScreenY
                        dragOriginWindowX = env.windowX
                        dragOriginWindowY = env.windowY
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val dx = env.mouseScreenX - dragOriginMouseX
                        val dy = env.mouseScreenY - dragOriginMouseY
                        env.setWindowPosition(
                            dragOriginWindowX + dx,
                            dragOriginWindowY + dy
                        )
                    },
                    onDragEnd = { },
                    onDragCancel = { }
                )
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
