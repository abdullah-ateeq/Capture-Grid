package com.example.capturegrid.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Sans = FontFamily.SansSerif

val Typography = Typography(
    displayLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Light, fontSize = 40.sp, letterSpacing = 8.sp),
    displayMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Light, fontSize = 32.sp, letterSpacing = 6.sp),
    headlineMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 26.sp, letterSpacing = 3.sp),
    headlineSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 20.sp, letterSpacing = 2.sp),
    titleLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 20.sp, letterSpacing = 1.sp),
    titleMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 16.sp, letterSpacing = 0.5.sp),
    titleSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 14.sp, letterSpacing = 0.5.sp),
    bodyLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 21.sp),
    bodySmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
    labelLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, letterSpacing = 2.5.sp),
    labelMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, letterSpacing = 2.sp),
    labelSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Medium, fontSize = 11.sp, letterSpacing = 1.5.sp)
)
