package com.lemon.focuspet.util

import java.awt.MouseInfo
import java.awt.Window

class DesktopEnvJvm(private val window: Window) : DesktopEnv {
    override val windowX: Int get() = window.x
    override val windowY: Int get() = window.y
    override fun setWindowPosition(x: Int, y: Int) = window.setLocation(x, y)
    override val mouseScreenX: Int get() = MouseInfo.getPointerInfo().location.x
    override val mouseScreenY: Int get() = MouseInfo.getPointerInfo().location.y
}
