package com.lemon.focuspet.ui

import androidx.compose.ui.graphics.ImageBitmap
import com.lemon.focuspet.model.PetState

data class SpriteSheet(
    val frames: List<ImageBitmap>,
    val fps: Int,
    val loop: Boolean = true,
) {
    val frameDurationMs: Long get() = 1000L / fps
}

expect object SpriteLoader {
    fun load(
        state: PetState,
        fps: Int = 5,
        loop: Boolean = true,
        maxFrames: Int = 24,
        maxSize: Int = 512,
    ): SpriteSheet?
}
