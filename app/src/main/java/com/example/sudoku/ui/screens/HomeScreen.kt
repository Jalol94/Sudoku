package com.example.sudoku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudoku.logic.Difficulty
import com.example.sudoku.ui.theme.SudokuBlue
import com.example.sudoku.ui.theme.SudokuBlueLight

@Composable
fun HomeScreen(
    hasSavedGame: Boolean,
    bestTimes: Map<Difficulty, Int?>,
    onResume: () -> Unit,
    onStartNewGame: (Difficulty) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = "Sudoku",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 24.dp, bottom = 32.dp),
        )

        if (hasSavedGame) {
            ResumeButton(onClick = onResume)
            Spacer(24.dp)
        }

        Text(
            text = "New Game",
            fontSize = 14.sp,
            color = Color(0xFF6B6B75),
            modifier = Modifier.padding(bottom = 8.dp),
        )

        for (difficulty in Difficulty.values()) {
            DifficultyRow(
                difficulty = difficulty,
                bestSeconds = bestTimes[difficulty],
                onClick = { onStartNewGame(difficulty) },
            )
            Spacer(10.dp)
        }
    }
}

@Composable
private fun ResumeButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(SudokuBlue, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = "Resume Game", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun DifficultyRow(difficulty: Difficulty, bestSeconds: Int?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(SudokuBlueLight, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = difficulty.label, fontSize = 16.sp, textAlign = TextAlign.Start)
        if (bestSeconds != null) {
            val m = bestSeconds / 60
            val s = bestSeconds % 60
            Text(
                text = "best %d:%02d".format(m, s),
                fontSize = 12.sp,
                color = Color(0xFF6B6B75),
            )
        }
    }
}

@Composable
private fun Spacer(heightDp: Int) {
    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(heightDp.dp))
}
