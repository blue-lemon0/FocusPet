package com.lemon.focuspet.util

/**
 * Platform-independent abstraction for desktop window operations.
 * Implemented per-platform (JVM uses AWT under the hood).
 */
interface DesktopEnv {
    val windowX: Int
    val windowY: Int
    fun setWindowPosition(x: Int, y: Int)
    val mouseScreenX: Int
    val mouseScreenY: Int
}
