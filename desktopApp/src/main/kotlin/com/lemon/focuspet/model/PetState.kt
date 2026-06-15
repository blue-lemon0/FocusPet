package com.lemon.focuspet.model

enum class PetState(val emoji: String, val label: String) {
    HAPPY("😊", "开心"),
    FOCUSING("🧘", "专注中"),
    ANGRY("😠", "生气"),
    PLEADING("🥺", "撒娇"),
    RESTING("😴", "休息")
}
