package com.lemon.focuspet.tray

import java.awt.*
import java.awt.image.BufferedImage

object TrayManager {

    private var trayIcon: TrayIcon? = null

    fun setup(appName: String, onToggleVisibility: () -> Unit, onExit: () -> Unit) {
        if (!SystemTray.isSupported()) return

        val popup = PopupMenu()
        val toggleItem = MenuItem("显示/隐藏")
        val exitItem = MenuItem("退出")

        toggleItem.addActionListener { onToggleVisibility() }
        exitItem.addActionListener { onExit() }

        popup.add(toggleItem)
        popup.addSeparator()
        popup.add(exitItem)

        // Create a simple orange circle as tray icon
        val image = BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)
        val g = image.createGraphics()
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.color = Color(0x22, 0x88, 0x22)
        g.fillOval(0, 0, 16, 16)
        g.dispose()

        val icon = TrayIcon(image, appName, popup)
        icon.isImageAutoSize = true

        try {
            SystemTray.getSystemTray().add(icon)
            trayIcon = icon
        } catch (_: Exception) {
        }
    }

    fun remove() {
        try {
            trayIcon?.let { SystemTray.getSystemTray().remove(it) }
        } catch (_: Exception) {
        }
    }
}
