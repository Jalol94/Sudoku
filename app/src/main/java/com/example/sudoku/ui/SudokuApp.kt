package com.example.sudoku.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.sudoku.ui.screens.GameScreen
import com.example.sudoku.ui.screens.HomeScreen
import com.example.sudoku.ui.theme.SudokuTheme

@Composable
fun SudokuApp(viewModel: SudokuViewModel) {
    val state by viewModel.uiState.collectAsState()

    SudokuTheme {
        when (state.screen) {
            Screen.Home -> HomeScreen(
                hasSavedGame = state.hasSavedGame,
                bestTimes = state.bestTimes,
                onResume = viewModel::resumeGame,
                onStartNewGame = viewModel::startNewGame,
            )
            Screen.Game -> GameScreen(
                isGenerating = state.isGenerating,
                board = state.board,
                boardVersion = state.boardVersion,
                selected = state.selected,
                notesMode = state.notesMode,
                elapsedSeconds = state.elapsedSeconds,
                justWon = state.justWon,
                onBack = viewModel::goHome,
                onCellClick = viewModel::selectCell,
                onDigit = viewModel::enterDigit,
                onErase = viewModel::eraseSelected,
                onUndo = viewModel::undo,
                onToggleNotes = viewModel::toggleNotesMode,
                onHint = viewModel::useHint,
                onNewGame = { viewModel.startNewGame(state.difficulty) },
                onDismissWin = viewModel::dismissWinDialog,
                onPlayAgain = { viewModel.startNewGame(state.difficulty) },
            )
        }
    }
}
