package com.example.sudoku.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudoku.logic.GameBoard
import com.example.sudoku.logic.SIZE
import com.example.sudoku.ui.theme.ConflictRed
import com.example.sudoku.ui.theme.EnteredTextBlue
import com.example.sudoku.ui.theme.GivenTextDark
import com.example.sudoku.ui.theme.PeerHighlight
import com.example.sudoku.ui.theme.SameValueHighlight
import com.example.sudoku.ui.theme.SelectedCell

/**
 * The 9x9 board. Deliberately takes a plain [GameBoard] (mutable) plus a
 * [version] counter — GameBoard isn't a Compose-observable data class, so
 * the caller bumps [version] on every mutation to force recomposition
 * rather than this composable trying to track individual cell state.
 */
@Composable
fun SudokuGrid(
    board: GameBoard,
    version: Int,
    selected: Pair<Int, Int>?,
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // `version` is read but unused directly — its only job is to be a key
    // that changes so Compose knows to recompute cell contents below.
    val _touch = version

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .border(2.dp, GivenTextDark)
    ) {
        Column {
            for (r in 0 until SIZE) {
                Row(modifier = Modifier.weight(1f)) {
                    for (c in 0 until SIZE) {
                        val isSelected = selected == r to c
                        val selectedValue = selected?.let { (sr, sc) ->
                            board.values[sr][sc].takeIf { it != 0 }
                        }
                        val isPeer = selected != null && !isSelected && run {
                            val (sr, sc) = selected
                            r == sr || c == sc || (r / 3 == sr / 3 && c / 3 == sc / 3)
                        }
                        val isSameValue = selectedValue != null &&
                            board.values[r][c] == selectedValue && !isSelected

                        SudokuCell(
                            row = r,
                            col = c,
                            value = board.values[r][c],
                            notes = board.notes[r][c],
                            isGiven = board.isFixed(r, c),
                            isConflict = board.conflicts(r, c),
                            isSelected = isSelected,
                            isPeer = isPeer,
                            isSameValue = isSameValue,
                            onClick = { onCellClick(r, c) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }
        BoxBorderOverlay()
    }
}

@Composable
private fun SudokuCell(
    row: Int,
    col: Int,
    value: Int,
    notes: Set<Int>,
    isGiven: Boolean,
    isConflict: Boolean,
    isSelected: Boolean,
    isPeer: Boolean,
    isSameValue: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = when {
        isSelected -> SelectedCell
        isSameValue -> SameValueHighlight
        isPeer -> PeerHighlight
        else -> Color.White
    }
    val textColor = when {
        isConflict -> ConflictRed
        isGiven -> GivenTextDark
        else -> EnteredTextBlue
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .background(background)
            .border(0.5.dp, Color(0xFFBBBBBB))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (value != 0) {
            Text(
                text = value.toString(),
                color = textColor,
                fontSize = 20.sp,
                fontWeight = if (isGiven) FontWeight.Medium else FontWeight.Normal,
            )
        } else if (notes.isNotEmpty()) {
            NotesGrid(notes)
        }
    }
}

@Composable
private fun NotesGrid(notes: Set<Int>) {
    Column {
        for (rowStart in listOf(1, 4, 7)) {
            Row {
                for (n in rowStart until rowStart + 3) {
                    Box(
                        modifier = Modifier.aspectRatio(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = if (n in notes) n.toString() else "",
                            fontSize = 8.sp,
                            color = Color(0xFF8A8A93),
                        )
                    }
                }
            }
        }
    }
}

/** Draws the bold 3x3 box-separator lines on top of the plain cell grid. */
@Composable
private fun BoxBorderOverlay() {
    Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
        val cell = size.width / SIZE
        val thickStroke = 2.5.dp.toPx()
        for (i in 0..SIZE) {
            if (i % 3 == 0) {
                drawLine(
                    color = Color(0xFF1F1F26),
                    start = Offset(i * cell, 0f),
                    end = Offset(i * cell, size.height),
                    strokeWidth = thickStroke,
                    cap = StrokeCap.Square,
                )
                drawLine(
                    color = Color(0xFF1F1F26),
                    start = Offset(0f, i * cell),
                    end = Offset(size.width, i * cell),
                    strokeWidth = thickStroke,
                    cap = StrokeCap.Square,
                )
            }
        }
    }
}
