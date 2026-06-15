package com.lemon.focuspet.util

/**
 * Persists cumulative focus minutes to local storage.
 * Platform-specific implementations store to ~/.focuspet/data.json on JVM.
 */
expect object DataStore {
    fun load(): Int
    fun save(totalMinutes: Int)
}
