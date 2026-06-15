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
import com.lemon.focuspet.model.PetState
import com.lemon.focuspet.viewmodel.PomodoroViewModel

// Pet state → subtle card tint (Material 50 shades)
// Pet mood → card tint (Material 50 shades)
private fun stateColor(state: PetState): Color = when (state) {
    PetState.HAPPY    -> Color(0xFFE8F5E9)  // fresh green 50
    PetState.FOCUSING -> Color(0xFFE3F2FD)  // calm blue 50
    PetState.ANGRY    -> Color(0xFFFFEBEE)  // alert red 50
    PetState.PLEADING -> Color(0xFFFCE4EC)  // soft pink 50
    PetState.RESTING  -> Color(0xFFEDE7F6)  // restful purple 50
}

// Accent colors for controls & labels
private fun accentColor(state: PetState): Color = when (state) {
    PetState.HAPPY    -> Color(0xFF43A047)  // green 600
    PetState.FOCUSING -> Color(0xFF1E88E5)  // blue 600
    PetState.ANGRY    -> Color(0xFFE53935)  // red 600
    PetState.PLEADING -> Color(0xFFD81B60)  // pink 600
    PetState.RESTING  -> Color(0xFF8E24AA)  // purple 600
}

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
            color = stateColor(viewModel.petState).copy(alpha = 0.95f),
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = viewModel.petState.emoji,
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center,
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = formatTime(viewModel.remainingSeconds),
                    fontSize = 28.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = if (viewModel.isBreak) "休息" else "专注中",
                    fontSize = 10.sp,
                    color = accentColor(viewModel.petState),
                    fontWeight = FontWeight.Medium,
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (viewModel.isRunning) {
                        SmallButton(accent = accentColor(viewModel.petState), onClick = { viewModel.pause() }) {
                            Text("⏸", fontSize = 16.sp)
                        }
                    } else {
                        SmallButton(accent = accentColor(viewModel.petState), onClick = { viewModel.start() }) {
                            Text("▶", fontSize = 16.sp)
                        }
                    }
                    SmallButton(accent = accentColor(viewModel.petState), onClick = { viewModel.reset() }) {
                        Text("↺", fontSize = 16.sp)
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "🔥 ${viewModel.totalMinutes} 分钟",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun SmallButton(accent: Color, onClick: () -> Unit, content: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = accent.copy(alpha = 0.12f),
        contentColor = accent,
        modifier = Modifier.size(34.dp),
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
