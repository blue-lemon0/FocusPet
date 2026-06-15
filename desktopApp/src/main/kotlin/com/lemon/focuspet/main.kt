package com.lemon.focuspet

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.lemon.focuspet.tray.TrayManager
import com.lemon.focuspet.ui.PetScreen
import com.lemon.focuspet.util.DesktopEnvJvm
import com.lemon.focuspet.viewmodel.PomodoroViewModel
import java.awt.Toolkit

fun main() = application {
    val viewModel = remember { PomodoroViewModel() }
    var isWindowVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) { viewModel.loadTotalMinutes() }

    DisposableEffect(Unit) {
        onDispose { viewModel.dispose(); TrayManager.remove() }
    }

    val windowState = rememberWindowState(width = 200.dp, height = 200.dp)

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
        val env = remember { DesktopEnvJvm(awtWindow) }

        LaunchedEffect(Unit) {
            // Position at screen bottom-right
            val gc = awtWindow.graphicsConfiguration
            val bounds = gc.bounds
            val insets = Toolkit.getDefaultToolkit().getScreenInsets(gc)
            val x = (bounds.x + insets.left + bounds.width - insets.left - insets.right - 230).coerceAtLeast(0)
            val y = (bounds.y + insets.top + bounds.height - insets.top - insets.bottom - 230).coerceAtLeast(0)
            awtWindow.setLocation(x, y)

            // Focus tracking
            awtWindow.addWindowFocusListener(object : java.awt.event.WindowFocusListener {
                override fun windowGainedFocus(e: java.awt.event.WindowEvent) { viewModel.onFocusGained() }
                override fun windowLostFocus(e: java.awt.event.WindowEvent) { viewModel.onFocusLost() }
            })
        }

        PetScreen(viewModel, env)
    }
}
