package com.example.sudoku.logic

/**
 * GameBoard: mutable state for a puzzle in progress.
 *
 * Separates "what the player has entered" from "what was given",
 * tracks pencil-mark notes per cell, supports undo, and can check a
 * placed digit against the known solution for mistake counting, or
 * check pure Sudoku legality for conflict highlighting.
 */

data class Move(
    val row: Int,
    val col: Int,
    val previousValue: Int,
    val newValue: Int,
    val previousNotes: Set<Int>
)

class GameBoard(
    puzzle: Board,
    solution: Board
) {
    val given: Board = puzzle.deepCopy()
    var values: Board = puzzle.deepCopy()
        private set
    val solution: Board = solution.deepCopy()
    val notes: Array<Array<MutableSet<Int>>> =
        Array(SIZE) { Array(SIZE) { mutableSetOf<Int>() } }

    private val history = mutableListOf<Move>()
    var mistakes: Int = 0
        private set

    fun isFixed(row: Int, col: Int): Boolean = given[row][col] != 0

    /**
     * Place [value] (1-9, or 0 to clear) at (row, col).
     * Returns true if the move was applied, false if the cell is fixed.
     * Tracks a mistake if the value conflicts with the solution.
     */
    fun setValue(row: Int, col: Int, value: Int): Boolean {
        if (isFixed(row, col)) return false

        val prevValue = values[row][col]
        val prevNotes = notes[row][col].toSet()

        values[row][col] = value
        if (value != 0) {
            notes[row][col].clear()
            if (value != solution[row][col]) mistakes++
        }

        history.add(Move(row, col, prevValue, value, prevNotes))
        return true
    }

    fun toggleNote(row: Int, col: Int, digit: Int): Boolean {
        if (isFixed(row, col) || values[row][col] != 0) return false
        val cellNotes = notes[row][col]
        if (digit in cellNotes) cellNotes.remove(digit) else cellNotes.add(digit)
        return true
    }

    fun undo(): Boolean {
        if (history.isEmpty()) return false
        val move = history.removeAt(history.size - 1)
        values[move.row][move.col] = move.previousValue
        notes[move.row][move.col] = move.previousNotes.toMutableSet()
        return true
    }

    fun isComplete(): Boolean =
        (0 until SIZE).all { r -> (0 until SIZE).all { c -> values[r][c] != 0 } }

    fun isSolved(): Boolean =
        isComplete() && (0 until SIZE).all { r -> values[r].contentEquals(solution[r]) }

    /** True if the current (non-zero) value at this cell breaks Sudoku rules. */
    fun conflicts(row: Int, col: Int): Boolean {
        val value = values[row][col]
        if (value == 0) return false
        val snapshot = values.deepCopy()
        snapshot[row][col] = 0
        return !isValid(snapshot, row, col, value)
    }

    /** Reveal the correct digit for a cell without counting it as a mistake. */
    fun hint(row: Int, col: Int): Int? {
        if (isFixed(row, col)) return null
        val value = solution[row][col]
        setValue(row, col, value) // always matches solution, so no mistake added
        return value
    }

    fun findConflicts(): List<Pair<Int, Int>> =
        (0 until SIZE).flatMap { r ->
            (0 until SIZE).filter { c -> conflicts(r, c) }.map { c -> r to c }
        }

    /** Serialize enough state to resume this game later. */
    fun toSnapshot(difficulty: Difficulty, elapsedSeconds: Int): GameSnapshot = GameSnapshot(
        difficulty = difficulty,
        given = given,
        values = values,
        solution = solution,
        notes = notes.map { row -> row.map { it.toSet() } },
        mistakes = mistakes,
        elapsedSeconds = elapsedSeconds
    )

    companion object {
        fun fromSnapshot(snapshot: GameSnapshot): GameBoard {
            val gb = GameBoard(snapshot.given, snapshot.solution)
            gb.values = snapshot.values.deepCopy()
            for (r in 0 until SIZE) {
                for (c in 0 until SIZE) {
                    gb.notes[r][c] = snapshot.notes[r][c].toMutableSet()
                }
            }
            gb.mistakes = snapshot.mistakes
            return gb
        }
    }
}

/** Plain-data snapshot of a GameBoard, suitable for JSON serialization. */
data class GameSnapshot(
    val difficulty: Difficulty,
    val given: Board,
    val values: Board,
    val solution: Board,
    val notes: List<List<Set<Int>>>,
    val mistakes: Int,
    val elapsedSeconds: Int
)
