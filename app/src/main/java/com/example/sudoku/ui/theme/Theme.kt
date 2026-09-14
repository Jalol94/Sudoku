package com.example.sudoku.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = SudokuBlue,
    onPrimary = Color.White,
    secondary = SudokuBlueLight,
    background = SurfaceLight,
    surface = SurfaceLight,
    onBackground = GivenTextDark,
    onSurface = GivenTextDark,
    error = ConflictRed,
)

@Composable
fun SudokuTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = SudokuTypography,
        content = content,
    )
}
