package com.example.sudoku.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sudoku.data.GameStorage
import com.example.sudoku.logic.Difficulty
import com.example.sudoku.logic.GameBoard
import com.example.sudoku.logic.generatePuzzle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class Screen {
    object Home : Screen()
    object Game : Screen()
}

data class UiState(
    val screen: Screen = Screen.Home,
    val isGenerating: Boolean = false,
    val difficulty: Difficulty = Difficulty.MEDIUM,
    val board: GameBoard? = null,
    val selected: Pair<Int, Int>? = null,
    val notesMode: Boolean = false,
    val elapsedSeconds: Int = 0,
    val hasSavedGame: Boolean = false,
    val bestTimes: Map<Difficulty, Int?> = emptyMap(),
    val justWon: Boolean = false,
    /** Bumped on every mutation so Compose recomposes even though GameBoard is mutable. */
    val boardVersion: Int = 0,
)

class SudokuViewModel(private val storage: GameStorage) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private var timerJob: kotlinx.coroutines.Job? = null

    init {
        refreshHomeData()
    }

    private fun refreshHomeData() {
        val bestTimes = Difficulty.values().associateWith { storage.getBestTime(it) }
        _uiState.update {
            it.copy(hasSavedGame = storage.loadGame() != null, bestTimes = bestTimes)
        }
    }

    fun goHome() {
        stopTimer()
        persistIfUnfinished()
        _uiState.update { it.copy(screen = Screen.Home, justWon = false) }
        refreshHomeData()
    }

    fun startNewGame(difficulty: Difficulty) {
        _uiState.update {
            it.copy(
                screen = Screen.Game,
                isGenerating = true,
                difficulty = difficulty,
                board = null,
                selected = null,
                elapsedSeconds = 0,
                justWon = false,
            )
        }
        viewModelScope.launch {
            val gen = withContext(Dispatchers.Default) { generatePuzzle(difficulty) }
            val board = GameBoard(gen.puzzle, gen.solution)
            _uiState.update {
                it.copy(isGenerating = false, board = board, boardVersion = it.boardVersion + 1)
            }
            startTimer()
        }
    }

    fun resumeGame() {
        val snapshot = storage.loadGame() ?: return
        val board = GameBoard.fromSnapshot(snapshot)
        _uiState.update {
            it.copy(
                screen = Screen.Game,
                isGenerating = false,
                difficulty = snapshot.difficulty,
                board = board,
                selected = null,
                elapsedSeconds = snapshot.elapsedSeconds,
                boardVersion = it.boardVersion + 1,
            )
        }
        startTimer()
    }

    fun selectCell(row: Int, col: Int) {
        _uiState.update { it.copy(selected = row to col) }
    }

    fun toggleNotesMode() {
        _uiState.update { it.copy(notesMode = !it.notesMode) }
    }

    fun enterDigit(digit: Int) {
        val state = _uiState.value
        val (row, col) = state.selected ?: return
        val board = state.board ?: return
        if (state.notesMode) {
            board.toggleNote(row, col, digit)
        } else {
            board.setValue(row, col, digit)
        }
        bumpBoard()
        checkWin()
    }

    fun eraseSelected() {
        val state = _uiState.value
        val (row, col) = state.selected ?: return
        state.board?.setValue(row, col, 0)
        bumpBoard()
    }

    fun undo() {
        _uiState.value.board?.undo()
        bumpBoard()
    }

    fun useHint() {
        val state = _uiState.value
        val (row, col) = state.selected ?: return
        state.board?.hint(row, col)
        bumpBoard()
        checkWin()
    }

    fun dismissWinDialog() {
        _uiState.update { it.copy(justWon = false) }
    }

    private fun bumpBoard() {
        _uiState.update { it.copy(boardVersion = it.boardVersion + 1) }
    }

    private fun checkWin() {
        val state = _uiState.value
        val board = state.board ?: return
        if (board.isSolved()) {
            stopTimer()
            storage.clearGame()
            storage.setBestTime(state.difficulty, state.elapsedSeconds)
            _uiState.update { it.copy(justWon = true) }
        }
    }

    private fun startTimer() {
        stopTimer()
        timerJob = viewModelScope.launch {
            while (isActive) {
                kotlinx.coroutines.delay(1000)
                _uiState.update { it.copy(elapsedSeconds = it.elapsedSeconds + 1) }
                val state = _uiState.value
                if (state.elapsedSeconds % 5 == 0) persistIfUnfinished()
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun persistIfUnfinished() {
        val state = _uiState.value
        val board = state.board ?: return
        if (!board.isSolved()) {
            storage.saveGame(board.toSnapshot(state.difficulty, state.elapsedSeconds))
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopTimer()
    }
}
