package com.lemon.focuspet

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.kdroid.composetray.menu.api.*
import com.kdroid.composetray.tray.api.Tray
import com.lemon.focuspet.ui.PetScreen
import com.lemon.focuspet.util.DesktopEnvJvm
import com.lemon.focuspet.viewmodel.PomodoroViewModel
import java.awt.Toolkit
import org.jetbrains.skia.Image

fun main() = application {
    val viewModel = remember { PomodoroViewModel() }
    var isWindowVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) { viewModel.loadTotalMinutes() }
    DisposableEffect(Unit) {
        onDispose { viewModel.dispose() }
    }

    val appIcon = remember {
        val bytes = object {}.javaClass.getResource("/icons/tray.png")?.readBytes()
            ?: error("tray.png not found")
        BitmapPainter(Image.makeFromEncoded(bytes).toComposeImageBitmap())
    }

    // ── System tray ──
    Tray(
        icon = appIcon,
        tooltip = "FocusPet",
        primaryAction = { isWindowVisible = !isWindowVisible },
        menuContent = {
            Item(label = "显示/隐藏") { isWindowVisible = !isWindowVisible }
            Item(label = "退出") { kotlin.system.exitProcess(0) }
        },
    )

    // ── Pet window ──
    val windowState = rememberWindowState(width = 260.dp, height = 310.dp)
    Window(
        onCloseRequest = { isWindowVisible = false },
        visible = isWindowVisible,
        state = windowState,
        undecorated = true,
        transparent = true,
        alwaysOnTop = true,
        resizable = false,
        title = "FocusPet",
        icon = appIcon,
    ) {
        val awtWindow = this.window
        val env = remember { DesktopEnvJvm(awtWindow) }

        // On first show: position to screen bottom-right, and register Esc → hide
        LaunchedEffect(Unit) {
            val gc = awtWindow.graphicsConfiguration
            val bounds = gc.bounds
            val insets = Toolkit.getDefaultToolkit().getScreenInsets(gc)
            val x = (bounds.x + insets.left + bounds.width - insets.left - insets.right - 230).coerceAtLeast(0)
            val y = (bounds.y + insets.top + bounds.height - insets.top - insets.bottom - 230).coerceAtLeast(0)
            awtWindow.setLocation(x, y)
        }

        PetScreen(viewModel, env, onHideRequest = { isWindowVisible = false })
    }
}
