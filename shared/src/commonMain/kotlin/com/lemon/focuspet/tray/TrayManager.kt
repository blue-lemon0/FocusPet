package com.lemon.focuspet.tray

/**
 * System tray integration for show/hide and exit.
 * Platform-specific implementations use AWT SystemTray on JVM.
 */
expect object TrayManager {
    fun setup(appName: String, onToggleVisibility: () -> Unit, onExit: () -> Unit)
    fun remove()
}
