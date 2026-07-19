package com.lemon.focuspet.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import java.util.Calendar
import kotlin.random.Random
import kotlinx.coroutines.delay

private fun timeAmbientColor(): Color {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 5..8  -> Color(0x18FF9800)
        in 9..17 -> Color(0x1003A9F4)
        in 18..20 -> Color(0x18E91E63)
        else     -> Color(0x1839409D)
    }
}

@Composable
fun PetScreen(viewModel: PomodoroViewModel, env: DesktopEnv, onHideRequest: (() -> Unit)? = null) {
    var dragOriginMouseX by remember { mutableIntStateOf(0) }
    var dragOriginMouseY by remember { mutableIntStateOf(0) }
    var dragOriginWindowX by remember { mutableIntStateOf(0) }
    var dragOriginWindowY by remember { mutableIntStateOf(0) }

    var bubble by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(30000, 55000))
            val phrases = listOf(
                "加油~", "你好呀", "在干嘛呢", "今天天气不错",
                "摸鱼中...", "继续继续", "该休息啦"
            )
            bubble = phrases.random()
            delay(4000)
            bubble = null
        }
    }

    val accent: Color by animateColorAsState(
        targetValue = accentColor(viewModel.petState),
        animationSpec = tween(500),
        label = "accent",
    )
    val surfaceBg: Color by animateColorAsState(
        targetValue = stateColor(viewModel.petState).copy(alpha = 0.95f),
        animationSpec = tween(500),
        label = "bgColor",
    )

    val ambientColor = remember { derivedStateOf { timeAmbientColor() } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ambientColor.value)
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
            modifier = Modifier.width(240.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = surfaceBg,
            tonalElevation = 2.dp,
            shadowElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (onHideRequest != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        HideButton(accent = accent, onClick = onHideRequest)
                    }
                }

                PetAvatar(
                    state = viewModel.petState,
                    overlay = {
                        if (bubble != null) {
                            PetBubble(text = bubble!!, accent = accent)
                        }
                    },
                )

                Spacer(Modifier.height(2.dp))

                Text(
                    text = formatTime(viewModel.remainingSeconds),
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = if (viewModel.isBreak) "休息" else "专注中",
                    fontSize = 10.sp,
                    color = accent,
                    fontWeight = FontWeight.Medium,
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (viewModel.isRunning) {
                        SmallButton(accent = accent, onClick = { viewModel.pause() }) {
                            Text("⏸", fontSize = 16.sp)
                        }
                    } else {
                        SmallButton(accent = accent, onClick = { viewModel.start() }) {
                            Text("▶", fontSize = 16.sp)
                        }
                    }
                    SmallButton(accent = accent, onClick = { viewModel.reset() }) {
                        Text("↺", fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun HideButton(accent: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = accent.copy(alpha = 0.08f),
        contentColor = accent.copy(alpha = 0.6f),
        modifier = Modifier.size(20.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("─", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
