package com.lemon.focuspet.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lemon.focuspet.model.PetState
import com.lemon.focuspet.util.DesktopEnv
import com.lemon.focuspet.viewmodel.PomodoroViewModel
import java.util.Calendar
import kotlin.random.Random
import kotlinx.coroutines.delay

private fun stateColor(state: PetState): Color = when (state) {
    PetState.HAPPY    -> Color(0xFFE8F5E9)
    PetState.FOCUSING -> Color(0xFFE3F2FD)
    PetState.ANGRY    -> Color(0xFFFFEBEE)
    PetState.PLEADING -> Color(0xFFFCE4EC)
    PetState.RESTING  -> Color(0xFFEDE7F6)
}

private fun accentColor(state: PetState): Color = when (state) {
    PetState.HAPPY    -> Color(0xFF43A047)
    PetState.FOCUSING -> Color(0xFF1E88E5)
    PetState.ANGRY    -> Color(0xFFE53935)
    PetState.PLEADING -> Color(0xFFD81B60)
    PetState.RESTING  -> Color(0xFF8E24AA)
}

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

    // ── Interactive state ──
    var eyeDx by remember { mutableStateOf(0f) }
    var eyeDy by remember { mutableStateOf(0f) }
    var blink by remember { mutableStateOf(false) }
    var actionText by remember { mutableStateOf<String?>(null) }
    var reaction by remember { mutableStateOf<String?>(null) }
    var reactionTick by remember { mutableStateOf(0) }
    var bubble by remember { mutableStateOf<String?>(null) }

    // Eye tracking
    LaunchedEffect(Unit) {
        while (true) {
            delay(80)
            val cx = env.windowX + 100
            val cy = env.windowY + 100
            val dx = (env.mouseScreenX - cx).coerceIn(-6, 6)
            val dy = (env.mouseScreenY - cy).coerceIn(-6, 6)
            eyeDx = dx / 6f
            eyeDy = dy / 6f
        }
    }

    // Blink
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(2000, 5000))
            blink = true
            delay(120)
            blink = false
        }
    }

    // Idle action
    LaunchedEffect(Unit) {
        while (true) {
            delay(Random.nextLong(8000, 20000))
            val actions = listOf("跳跃!", "翻滚~", "伸懒腰", "歪头?")
            actionText = actions.random()
            delay(2300)
            actionText = null
        }
    }

    // Reaction timeout
    LaunchedEffect(reactionTick) {
        if (reaction != null) {
            delay(1000)
            reaction = null
        }
    }

    // Speech bubble
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
            modifier = Modifier.size(200.dp),
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
                // ── Top row ──
                if (onHideRequest != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        HideButton(accent = accentColor(viewModel.petState), onClick = onHideRequest)
                    }
                }

                // ── Cat face ──
                Box(
                    modifier = Modifier.height(56.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    // Speech bubble
                    if (bubble != null) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-16).dp),
                            shape = MaterialTheme.shapes.small,
                            color = accentColor(viewModel.petState).copy(alpha = 0.12f),
                            shadowElevation = 1.dp,
                        ) {
                            Text(
                                text = bubble!!,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                color = accentColor(viewModel.petState),
                                fontWeight = FontWeight.Medium,
                            )
                        }
                    }

                    CatFace(
                        state = viewModel.petState,
                        eyeDx = eyeDx,
                        eyeDy = eyeDy,
                        blink = blink,
                        modifier = Modifier
                            .size(84.dp)
                            .align(Alignment.Center)
                            .offset(y = if (reaction != null) 2.dp else 0.dp)
                            .clip(CircleShape)
                            .clickable {
                                reaction = listOf("💥", "😆", "❤️", "✨").random()
                                reactionTick++
                            },
                    )

                    // Action / reaction overlay
                    if (reaction != null) {
                        Text(
                            text = reaction!!,
                            fontSize = 28.sp,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    } else if (actionText != null) {
                        Text(
                            text = actionText!!,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor(viewModel.petState),
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .offset(y = 4.dp),
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))

                // ── Timer ──
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
                    color = accentColor(viewModel.petState),
                    fontWeight = FontWeight.Medium,
                )

                Spacer(Modifier.height(6.dp))

                // ── Controls ──
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

                Spacer(Modifier.height(4.dp))

                // ── Stats ──
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
