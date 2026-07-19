package com.lemon.focuspet.ui

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import com.lemon.focuspet.model.PetState
import kotlin.random.Random

data class FrameEntry(
    val bitmap: ImageBitmap?,
    val state: PetState,
    val index: Int,
)

/**
 * 类型队列，交替模式：
 * 随机 → NEUTRAL → 随机 → NEUTRAL → 随机 → ...
 * currentState 的所有帧播完后自动切换 nextState。
 * 外部状态切换通过 scheduleTransition 干预（NEUTRAL → target → 恢复交替）。
 */
class FrameQueue {
    private val sourceFrames = mutableMapOf<PetState, List<ImageBitmap?>>()
    private val idlePool = mutableListOf<PetState>()

    private var currentState = PetState.HAPPY
    private var nextState = PetState.NEUTRAL
    private var frameIndex = 0

    private val _currentFrame = mutableStateOf<FrameEntry?>(null)
    val currentFrame: State<FrameEntry?> = _currentFrame

    fun initialize(data: Map<PetState, List<ImageBitmap?>>, start: PetState) {
        sourceFrames.putAll(data)

        idlePool.clear()
        idlePool.addAll(data.entries
            .filter { it.key != PetState.NEUTRAL && it.value.isNotEmpty() }
            .map { it.key })

        currentState = start
        nextState = PetState.NEUTRAL
        frameIndex = 0
        _currentFrame.value = currentEntry()
    }

    /** 推进一帧。当前 state 播完自动切换 nextState，并保持 NEUTRAL 交替。 */
    fun advance() {
        val frames = currentFrames()
        if (frames.isEmpty()) return

        frameIndex++
        if (frameIndex >= frames.size) {
            val was = currentState
            currentState = nextState
            nextState = if (currentState == PetState.NEUTRAL) pickRandom() else PetState.NEUTRAL
            frameIndex = 0
        }
        _currentFrame.value = currentEntry()
    }

    /** 外部切换：NEUTRAL 桥接 → target → 恢复交替。 */
    fun scheduleTransition(target: PetState) {
        if (target == currentState && nextState == PetState.NEUTRAL) return
        currentState = PetState.NEUTRAL
        nextState = target
        frameIndex = 0
        _currentFrame.value = currentEntry()
    }

    // ── internal ──

    private fun currentFrames(): List<ImageBitmap?> =
        sourceFrames[currentState].orEmpty()

    private fun currentEntry(): FrameEntry? {
        val frames = currentFrames()
        if (frames.isEmpty()) return null
        val idx = frameIndex.coerceAtMost(frames.size - 1)
        return FrameEntry(frames[idx], currentState, idx)
    }

    private fun pickRandom(): PetState {
        if (idlePool.isEmpty()) return PetState.NEUTRAL
        val candidates = idlePool.filter { it != currentState }
        if (candidates.isEmpty()) return idlePool.first()
        return candidates[Random.nextInt(candidates.size)]
    }
}
