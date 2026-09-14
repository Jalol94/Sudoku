package com.example.sudoku.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sudoku.logic.GameBoard
import com.example.sudoku.ui.components.NumberPad
import com.example.sudoku.ui.components.SudokuGrid
import com.example.sudoku.ui.theme.SudokuBlueLight

@Composable
fun GameScreen(
    isGenerating: Boolean,
    board: GameBoard?,
    boardVersion: Int,
    selected: Pair<Int, Int>?,
    notesMode: Boolean,
    elapsedSeconds: Int,
    justWon: Boolean,
    onBack: () -> Unit,
    onCellClick: (Int, Int) -> Unit,
    onDigit: (Int) -> Unit,
    onErase: () -> Unit,
    onUndo: () -> Unit,
    onToggleNotes: () -> Unit,
    onHint: () -> Unit,
    onNewGame: () -> Unit,
    onDismissWin: () -> Unit,
    onPlayAgain: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
        TopBar(
            elapsedSeconds = elapsedSeconds,
            mistakes = board?.mistakes ?: 0,
            onBack = onBack,
        )

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isGenerating || board == null) {
                LoadingView()
            } else {
                Column {
                    SudokuGrid(
                        board = board,
                        version = boardVersion,
                        selected = selected,
                        onCellClick = onCellClick,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )

                    Toolbar(
                        notesMode = notesMode,
                        onUndo = onUndo,
                        onToggleNotes = onToggleNotes,
                        onHint = onHint,
                        onNewGame = onNewGame,
                    )

                    NumberPad(
                        board = board,
                        version = boardVersion,
                        onDigit = onDigit,
                        onErase = onErase,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
    }

    if (justWon) {
        WinDialog(
            elapsedSeconds = elapsedSeconds,
            mistakes = board?.mistakes ?: 0,
            onPlayAgain = {
                onDismissWin()
                onPlayAgain()
            },
            onHome = {
                onDismissWin()
                onBack()
            },
        )
    }
}

@Composable
private fun TopBar(elapsedSeconds: Int, mistakes: Int, onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = "< Back",
            fontSize = 14.sp,
            modifier = Modifier.clickable(onClick = onBack),
        )
        val m = elapsedSeconds / 60
        val s = elapsedSeconds % 60
        Text(text = "%d:%02d".format(m, s), fontSize = 14.sp)
        Text(text = "Mistakes: $mistakes", fontSize = 14.sp)
    }
}

@Composable
private fun Toolbar(
    notesMode: Boolean,
    onUndo: () -> Unit,
    onToggleNotes: () -> Unit,
    onHint: () -> Unit,
    onNewGame: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ToolbarButton("Undo", Modifier.weight(1f), onUndo)
        ToolbarButton(if (notesMode) "Notes: On" else "Notes: Off", Modifier.weight(1f), onToggleNotes)
        ToolbarButton("Hint", Modifier.weight(1f), onHint)
        ToolbarButton("New", Modifier.weight(1f), onNewGame)
    }
}

@Composable
private fun ToolbarButton(label: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(44.dp)
            .background(SudokuBlueLight, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF1A1A22))
    }
}

@Composable
private fun LoadingView() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Text(text = "Generating puzzle...", modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun WinDialog(elapsedSeconds: Int, mistakes: Int, onPlayAgain: () -> Unit, onHome: () -> Unit) {
    val m = elapsedSeconds / 60
    val s = elapsedSeconds % 60
    AlertDialog(
        onDismissRequest = onHome,
        title = { Text("Solved!") },
        text = { Text("Nice work — %d:%02d, %d mistakes.".format(m, s, mistakes)) },
        confirmButton = {
            TextButton(onClick = onPlayAgain) { Text("Play Again") }
        },
        dismissButton = {
            TextButton(onClick = onHome) { Text("Home") }
        },
    )
}
