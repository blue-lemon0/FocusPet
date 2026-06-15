package com.lemon.focuspet

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.lemon.focuspet.tray.TrayManager
import com.lemon.focuspet.ui.PetScreen
import com.lemon.focuspet.viewmodel.PomodoroViewModel
import java.awt.Toolkit

fun main() = application {
    val viewModel = remember { PomodoroViewModel() }
    var isWindowVisible by remember { mutableStateOf(true) }

    // Load persisted data
    LaunchedEffect(Unit) {
        viewModel.loadTotalMinutes()
    }

    // Cleanup on exit
    DisposableEffect(Unit) {
        onDispose {
            viewModel.dispose()
            TrayManager.remove()
        }
    }

    val windowState = rememberWindowState(width = 200.dp, height = 200.dp)

    // System tray setup
    LaunchedEffect(Unit) {
        TrayManager.setup(
            appName = "FocusPet",
            onToggleVisibility = { isWindowVisible = !isWindowVisible },
            onExit = ::exitApplication
        )
    }

    Window(
        onCloseRequest = { isWindowVisible = false },
        visible = isWindowVisible,
        state = windowState,
        undecorated = true,
        transparent = true,
        alwaysOnTop = true,
        resizable = false,
        title = "FocusPet"
    ) {
        val awtWindow = this.window

        LaunchedEffect(Unit) {
            // Position window at screen bottom-right (pixel coordinates)
            val gc = awtWindow.graphicsConfiguration
            val bounds = gc.bounds
            val insets = Toolkit.getDefaultToolkit().getScreenInsets(gc)

            val usableX = bounds.x + insets.left
            val usableY = bounds.y + insets.top
            val usableW = bounds.width - insets.left - insets.right
            val usableH = bounds.height - insets.top - insets.bottom

            val xPx = (usableX + usableW - 230).coerceAtLeast(0)
            val yPx = (usableY + usableH - 230).coerceAtLeast(0)

            awtWindow.setLocation(xPx, yPx)

            // Track window focus for pomodoro distraction detection
            awtWindow.addWindowFocusListener(object : java.awt.event.WindowFocusListener {
                override fun windowGainedFocus(e: java.awt.event.WindowEvent) {
                    viewModel.onFocusGained()
                }

                override fun windowLostFocus(e: java.awt.event.WindowEvent) {
                    viewModel.onFocusLost()
                }
            })
        }

        PetScreen(viewModel, awtWindow)
    }
}
