package com.lemon.focuspet.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.lemon.focuspet.model.PetState
import com.lemon.focuspet.util.DataStore
import kotlinx.coroutines.*

class PomodoroViewModel {

    // ── Timer state ──
    var remainingSeconds by mutableStateOf(FOCUS_TIME)
        private set
    var isRunning by mutableStateOf(false)
        private set
    var isBreak by mutableStateOf(false)
        private set

    // ── Pet state ──
    var petState by mutableStateOf(PetState.HAPPY)
        private set

    // ── Focus tracking ──
    var focusLostSince by mutableStateOf<Long?>(null)
        private set

    // ── Statistics ──
    var totalMinutes by mutableStateOf(0)
        private set

    private var timerJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        const val FOCUS_TIME = 25 * 60
        const val BREAK_TIME = 5 * 60
        private const val ANGRY_THRESHOLD_MS = 5_000L
    }

    // ── Public API ──

    fun loadTotalMinutes() {
        totalMinutes = DataStore.load()
    }

    fun start() {
        if (isRunning) return
        isRunning = true

        timerJob = scope.launch {
            while (isRunning) {
                while (remainingSeconds > 0 && isRunning) {
                    delay(1000)
                    remainingSeconds--
                    updatePetState()
                }

                if (!isRunning) break

                if (!isBreak) {
                    totalMinutes += 25
                    DataStore.save(totalMinutes)
                    isBreak = true
                    remainingSeconds = BREAK_TIME
                    petState = PetState.RESTING
                } else {
                    isBreak = false
                    remainingSeconds = FOCUS_TIME
                    petState = PetState.HAPPY
                    isRunning = false
                    break
                }
            }
        }
    }

    fun pause() {
        isRunning = false
        timerJob?.cancel()
    }

    fun reset() {
        timerJob?.cancel()
        isRunning = false
        isBreak = false
        remainingSeconds = FOCUS_TIME
        petState = PetState.HAPPY
        focusLostSince = null
    }

    // ── Focus callbacks ──

    fun onFocusGained() {
        focusLostSince = null
        if (isRunning) updatePetState()
    }

    fun onFocusLost() {
        if (isRunning) {
            focusLostSince = currentTimeMillis()
        }
    }

    // ── Internal ──

    private fun updatePetState() {
        val lostSince = focusLostSince
        petState = when {
            isBreak -> PetState.RESTING
            !isRunning -> PetState.HAPPY
            lostSince != null
                    && currentTimeMillis() - lostSince > ANGRY_THRESHOLD_MS -> PetState.ANGRY
            remainingSeconds < 60 -> PetState.PLEADING
            else -> PetState.FOCUSING
        }
    }

    fun dispose() {
        scope.cancel()
    }

    private fun currentTimeMillis() = System.currentTimeMillis()
}
