package com.lemon.focuspet

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform