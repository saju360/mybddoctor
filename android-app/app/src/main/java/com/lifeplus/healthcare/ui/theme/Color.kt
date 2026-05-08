package com.lifeplus.healthcare.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Brand Colors (Premium Healthcare Palette) ─────────────────────────────
val Primary      = Color(0xFF0056D2)   
val PrimaryLight = Color(0xFFE7F1FF)
val PrimaryDark  = Color(0xFF003B95)

val Secondary     = Color(0xFF00B09B)   
val SecondaryLight = Color(0xFFE0F2F1)

val Accent       = Color(0xFFFF4B55)   
val AccentLight  = Color(0xFFFFF1F2)

// ── Background & Surfaces (Soft & Modern) ──────────────────────────────────
val BackgroundLight = Color(0xFFF8FAFC)  // Soft Gray-Blue
val SurfaceLight    = Color(0xFFFFFFFF)  
val Surface2Light   = Color(0xFFF1F5F9)  

// ── Text & Content (High Contrast Slate) ───────────────────────────────────
val OnPrimary    = Color(0xFFFFFFFF)
val TextPrimary  = Color(0xFF0F172A)   
val TextSecondary = Color(0xFF64748B)  
val TextHint     = Color(0xFF94A3B8)  

// ── Semantic ───────────────────────────────────────────────────────────────
val SuccessColor  = Color(0xFF10B981)  
val WarningColor  = Color(0xFFF59E0B)  
val ErrorColor    = Color(0xFFEF4444)  
val InfoColor     = Color(0xFF3B82F6)  

// ── Premium Gradients ──────────────────────────────────────────────────────
val PremiumBlueGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF0056D2), Color(0xFF3B82F6))
)

val PremiumGlassGradient = Brush.verticalGradient(
    colors = listOf(Color.White.copy(alpha = 0.9f), Color.White.copy(alpha = 0.6f))
)

val EmergencyGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFFEF4444), Color(0xFFFF4B55))
)

val SuccessGradient = Brush.linearGradient(
    colors = listOf(Color(0xFF10B981), Color(0xFF34D399))
)

// Backward-compatible aliases used by older UI components
val Surface2Dark = Surface2Light
val Surface3Dark = Color(0xFFE2E8F0)
val Gradient1 = PremiumBlueGradient
val GradientPrimary = PremiumBlueGradient

// ── UI Components Constants ────────────────────────────────────────────────
val GlassWhite     = Color(0xB3FFFFFF)
val GlassBorder    = Color(0x33FFFFFF)
val ShadowColor    = Color(0x1A000000)
