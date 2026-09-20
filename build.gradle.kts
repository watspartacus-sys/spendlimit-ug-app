plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)   <- DELETE THIS LINE
    alias(libs.plugins.devtools.ksp)
}