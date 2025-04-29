package com.example.entriqs

data class StatusCategory(
    val name: String,
    val count: Int,
    val percentage: Float,
    val progressColor: Int,
    val iconRes: Int // New field for the icon resource ID
)