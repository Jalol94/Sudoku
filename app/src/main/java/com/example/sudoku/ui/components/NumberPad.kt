package com.example.sudoku.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudoku.logic.GameBoard
import com.example.sudoku.logic.SIZE
import com.example.sudoku.ui.theme.SudokuBlueLight

@Composable
fun NumberPad(
    board: GameBoard,
    version: Int,
    onDigit: (Int) -> Unit,
    onErase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val _touch = version // force recomposition on board mutation; see SudokuGrid

    val remaining = remember(version) {
        val counts = (1..9).associateWith { 9 }.toMutableMap()
        for (r in 0 until SIZE) {
            for (c in 0 until SIZE) {
                val v = board.values[r][c]
                if (v != 0) counts[v] = (counts[v] ?: 0) - 1
            }
        }
        counts
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (digit in 1..9) {
            PadButton(
                label = digit.toString(),
                subLabel = remaining[digit]?.takeIf { it > 0 }?.toString() ?: "",
                onClick = { onDigit(digit) },
                modifier = Modifier.weight(1f),
            )
        }
        PadButton(
            label = "Del",
            subLabel = "",
            onClick = onErase,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PadButton(
    label: String,
    subLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .aspectRatio(0.7f)
            .background(SudokuBlueLight, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(text = label, fontSize = 18.sp, color = Color(0xFF1A1A22))
        }
        Text(text = subLabel, fontSize = 9.sp, color = Color(0xFF7A7A85))
    }
}
