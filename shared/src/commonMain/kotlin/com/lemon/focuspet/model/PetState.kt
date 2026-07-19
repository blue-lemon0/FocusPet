package com.lemon.focuspet.model

enum class PetState(val emoji: String, val label: String) {
    NEUTRAL("😐", "平静"),
    HAPPY("😊", "开心"),
    FOCUSING("🧘", "专注中"),
    ANGRY("😠", "生气"),
    PLEADING("🥺", "撒娇"),
    WINK("😉", "眨眼"),
    RESTING("😴", "休息")
}
