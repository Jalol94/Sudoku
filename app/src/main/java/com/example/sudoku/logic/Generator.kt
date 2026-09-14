package com.example.sudoku.logic

import kotlin.random.Random

/**
 * Sudoku puzzle generator.
 *
 * Strategy:
 * 1. Fill an empty board completely using randomized backtracking
 *    (this gives a valid, random solved grid every time).
 * 2. Punch holes in the solved grid, removing cells in symmetric
 *    pairs, only when doing so keeps the puzzle at a UNIQUE solution.
 * 3. Stop once we've hit the clue count for the requested difficulty.
 */

enum class Difficulty(val label: String) {
    EASY("Easy"),
    MEDIUM("Medium"),
    HARD("Hard"),
    EXPERT("Expert");
}

// Approximate number of filled clues left on the board (81 cells total).
// Lower clue count -> harder puzzle.
val CLUES_BY_DIFFICULTY: Map<Difficulty, Int> = mapOf(
    Difficulty.EASY to 40,
    Difficulty.MEDIUM to 32,
    Difficulty.HARD to 27,
    Difficulty.EXPERT to 23
)

data class GeneratedPuzzle(val puzzle: Board, val solution: Board)

private fun fillBoard(random: Random): Board {
    val board = emptyBoard()

    fun backtrack(pos: Int): Boolean {
        if (pos == SIZE * SIZE) return true
        val row = pos / SIZE
        val col = pos % SIZE

        val values = (1..9).shuffled(random)
        for (value in values) {
            if (isValid(board, row, col, value)) {
                board[row][col] = value
                if (backtrack(pos + 1)) return true
                board[row][col] = 0
            }
        }
        return false
    }

    backtrack(0)
    return board
}

/** All cell coordinates paired with their 180-degree rotational twin. */
private fun symmetricCellPairs(): List<Pair<Pair<Int, Int>, Pair<Int, Int>>> {
    val seen = mutableSetOf<Pair<Int, Int>>()
    val pairs = mutableListOf<Pair<Pair<Int, Int>, Pair<Int, Int>>>()
    for (r in 0 until SIZE) {
        for (c in 0 until SIZE) {
            val cell = r to c
            if (cell in seen) continue
            val twin = (SIZE - 1 - r) to (SIZE - 1 - c)
            seen.add(cell)
            seen.add(twin)
            pairs.add(cell to twin)
        }
    }
    return pairs
}

/**
 * Generate a (puzzle, solution) pair. `puzzle` has 0s for empty cells;
 * `solution` is the fully solved board. Removal is symmetric (visually
 * pleasing, like published puzzles) and every removal is checked to
 * preserve a unique solution.
 */
fun generatePuzzle(difficulty: Difficulty = Difficulty.MEDIUM, random: Random = Random.Default): GeneratedPuzzle {
    val solution = fillBoard(random)
    val puzzle = solution.deepCopy()

    val targetClues = CLUES_BY_DIFFICULTY.getValue(difficulty)
    val pairs = symmetricCellPairs().shuffled(random)

    var cluesRemaining = SIZE * SIZE

    for ((first, second) in pairs) {
        val budget = cluesRemaining - targetClues
        if (budget <= 0) break

        var candidateCells = if (first != second) listOf(first, second) else listOf(first)
        candidateCells = candidateCells.filter { (r, c) -> puzzle[r][c] != 0 }
        if (candidateCells.isEmpty()) continue
        // Don't remove more cells than we still need — otherwise a
        // 2-cell pair can overshoot past the target by one clue.
        if (candidateCells.size > budget) {
            candidateCells = candidateCells.take(budget)
        }

        val removed = candidateCells.map { (r, c) -> Triple(r, c, puzzle[r][c]) }
        for ((r, c, _) in removed) puzzle[r][c] = 0

        if (hasUniqueSolution(puzzle)) {
            cluesRemaining -= removed.size
        } else {
            for ((r, c, value) in removed) puzzle[r][c] = value
        }
    }

    // Symmetric removal often stalls before reaching the target for
    // harder difficulties (every remaining pair breaks uniqueness).
    // Finish with single-cell (non-symmetric) removal to close the gap.
    if (cluesRemaining > targetClues) {
        val singles = mutableListOf<Pair<Int, Int>>()
        for (r in 0 until SIZE) for (c in 0 until SIZE) if (puzzle[r][c] != 0) singles.add(r to c)
        singles.shuffle(random)

        for ((r, c) in singles) {
            if (cluesRemaining <= targetClues) break
            val value = puzzle[r][c]
            puzzle[r][c] = 0
            if (hasUniqueSolution(puzzle)) {
                cluesRemaining--
            } else {
                puzzle[r][c] = value
            }
        }
    }

    return GeneratedPuzzle(puzzle, solution)
}
