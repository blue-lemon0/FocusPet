package com.lemon.focuspet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import com.kdroid.composetray.menu.api.*
import com.kdroid.composetray.tray.api.Tray
import com.lemon.focuspet.ui.PetScreen
import com.lemon.focuspet.util.DesktopEnvJvm
import com.lemon.focuspet.viewmodel.PomodoroViewModel
import java.awt.Toolkit
import java.awt.event.WindowEvent
import java.awt.event.WindowFocusListener

fun main() = application {
    val viewModel = remember { PomodoroViewModel() }
    var isWindowVisible by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) { viewModel.loadTotalMinutes() }
    DisposableEffect(Unit) {
        onDispose { viewModel.dispose() }
    }

    // ── System tray (ComposeNativeTray) ──
    val icon = remember {
        object : Painter() {
            override val intrinsicSize = Size(16f, 16f)
            override fun DrawScope.onDraw() {
                drawCircle(Color(0x22, 0x88, 0x22))
            }
        }
    }

    Tray(
        icon = icon,
        tooltip = "FocusPet",
        primaryAction = { isWindowVisible = !isWindowVisible },
        menuContent = {
            Item(label = "显示/隐藏") { isWindowVisible = !isWindowVisible }
            Item(label = "退出") { kotlin.system.exitProcess(0) }
        },
    )

    // ── Pet window ──
    val windowState = rememberWindowState(width = 200.dp, height = 200.dp)
    Window(
        onCloseRequest = { isWindowVisible = false },
        visible = isWindowVisible,
        state = windowState,
        undecorated = true,
        transparent = true,
        alwaysOnTop = true,
        resizable = false,
        title = "FocusPet",
    ) {
        val awtWindow = this.window
        val env = remember { DesktopEnvJvm(awtWindow) }

        LaunchedEffect(Unit) {
            val gc = awtWindow.graphicsConfiguration
            val bounds = gc.bounds
            val insets = Toolkit.getDefaultToolkit().getScreenInsets(gc)
            val x = (bounds.x + insets.left + bounds.width - insets.left - insets.right - 230).coerceAtLeast(0)
            val y = (bounds.y + insets.top + bounds.height - insets.top - insets.bottom - 230).coerceAtLeast(0)
            awtWindow.setLocation(x, y)

            awtWindow.addWindowFocusListener(object : WindowFocusListener {
                override fun windowGainedFocus(e: WindowEvent?) { viewModel.onFocusGained() }
                override fun windowLostFocus(e: WindowEvent?) { viewModel.onFocusLost() }
            })
        }

        Box(
            Modifier.fillMaxSize().onPreviewKeyEvent {
                if (it.key == Key.Escape && it.type == KeyEventType.KeyUp) {
                    isWindowVisible = false; true
                } else false
            }
        ) {
            PetScreen(viewModel, env)
        }
    }
}
