package com.lemon.focuspet.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.lemon.focuspet.model.PetState
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

internal fun stateColor(state: PetState): Color = when (state) {
    PetState.NEUTRAL  -> Color(0xFFF5F5F5)
    PetState.HAPPY    -> Color(0xFFE8F5E9)
    PetState.FOCUSING -> Color(0xFFE3F2FD)
    PetState.ANGRY    -> Color(0xFFFFEBEE)
    PetState.PLEADING -> Color(0xFFFCE4EC)
    PetState.WINK     -> Color(0xFFE3F2FD)
    PetState.RESTING  -> Color(0xFFEDE7F6)
}

internal fun accentColor(state: PetState): Color = when (state) {
    PetState.NEUTRAL  -> Color(0xFF9E9E9E)
    PetState.HAPPY    -> Color(0xFF43A047)
    PetState.FOCUSING -> Color(0xFF1E88E5)
    PetState.ANGRY    -> Color(0xFFE53935)
    PetState.PLEADING -> Color(0xFFD81B60)
    PetState.WINK     -> Color(0xFF1E88E5)
    PetState.RESTING  -> Color(0xFF8E24AA)
}

@Composable
fun PetAvatar(
    state: PetState,
    modifier: Modifier = Modifier,
    overlay: @Composable () -> Unit = {},
) {
    var ready by remember { mutableStateOf(false) }
    var frameData by remember { mutableStateOf(emptyMap<PetState, List<ImageBitmap?>>()) }
    val queue = remember { FrameQueue() }

    LaunchedEffect(Unit) {
        val loaded = withContext(Dispatchers.Default) {
            PetState.entries.associateWith { SpriteLoader.load(it) }
        }
        frameData = loaded.mapValues { (_, sheet) -> sheet?.frames ?: emptyList() }
        ready = true
    }

    LaunchedEffect(ready) {
        if (ready) {
            queue.initialize(frameData, start = PetState.HAPPY)
        }
    }

    LaunchedEffect(ready) {
        if (ready) {
            while (true) {
                delay(300)
                queue.advance()
            }
        }
    }

    LaunchedEffect(state, ready) {
        if (ready) {
            queue.scheduleTransition(state)
        }
    }

    Box(
        modifier = modifier.height(150.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (ready) {
                overlay()
            }
        }

        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape),
        ) {
            val entry = queue.currentFrame.value
            Canvas(modifier = Modifier.fillMaxSize()) {
                if (entry != null) {
                    val img = entry.bitmap
                    if (img != null) {
                        val scale = minOf(size.width / img.width, size.height / img.height)
                        val w = img.width * scale
                        val h = img.height * scale
                        val x = (size.width - w) / 2f
                        val y = (size.height - h) / 2f
                        drawImage(
                            image = img,
                            srcOffset = IntOffset.Zero,
                            srcSize = IntSize(img.width, img.height),
                            dstOffset = IntOffset(x.roundToInt(), y.roundToInt()),
                            dstSize = IntSize(w.roundToInt(), h.roundToInt()),
                        )
                    }
                }
            }
        }
    }
}
