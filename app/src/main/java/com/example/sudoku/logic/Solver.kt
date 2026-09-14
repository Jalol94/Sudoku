package com.example.sudoku.logic

/**
 * Sudoku solving engine.
 *
 * Uses backtracking search with a minimum-remaining-values (MRV)
 * heuristic: at each step it fills the empty cell with the fewest
 * legal candidates first, rather than scanning in fixed row-major
 * order. This prunes the search tree dramatically for near-minimal
 * (hard/expert) puzzles, where uniqueness has to be checked many
 * times during generation.
 */

const val SIZE = 9
const val BOX = 3

/** A 9x9 board; 0 means empty. */
typealias Board = Array<IntArray>

fun Board.deepCopy(): Board = Array(SIZE) { r -> this[r].copyOf() }

fun emptyBoard(): Board = Array(SIZE) { IntArray(SIZE) }

/** Check whether `value` can legally be placed at (row, col). */
fun isValid(board: Board, row: Int, col: Int, value: Int): Boolean {
    for (c in 0 until SIZE) if (board[row][c] == value) return false
    for (r in 0 until SIZE) if (board[r][col] == value) return false

    val boxR = BOX * (row / BOX)
    val boxC = BOX * (col / BOX)
    for (r in boxR until boxR + BOX) {
        for (c in boxC until boxC + BOX) {
            if (board[r][c] == value) return false
        }
    }
    return true
}

/** Legal values (1-9) for an empty cell, given the board's current state. */
fun candidates(board: Board, row: Int, col: Int): List<Int> =
    (1..9).filter { isValid(board, row, col, it) }

data class ConstrainedCell(val row: Int, val col: Int, val candidates: List<Int>)

/**
 * Return the empty cell with the fewest legal candidates, plus that
 * candidate list — or null if the board is full. Ties resolve to
 * whichever constrained cell is found first, and a 0/1-candidate
 * cell short-circuits the scan since nothing can beat it.
 */
fun findMostConstrained(board: Board): ConstrainedCell? {
    var best: ConstrainedCell? = null
    for (r in 0 until SIZE) {
        for (c in 0 until SIZE) {
            if (board[r][c] != 0) continue
            val cands = candidates(board, r, c)
            val current = best
            if (current == null || cands.size < current.candidates.size) {
                val next = ConstrainedCell(r, c, cands)
                best = next
                if (cands.size <= 1) return next
            }
        }
    }
    return best
}

/**
 * Verify the board's pre-filled cells don't already violate Sudoku
 * rules among themselves (duplicate in a row/col/box). isValid() only
 * guards new placements going forward, so a contradictory starting
 * board would otherwise search forever for a solution that can't exist.
 */
fun givensAreConsistent(board: Board): Boolean {
    fun hasDuplicates(values: List<Int>): Boolean {
        val filled = values.filter { it != 0 }
        return filled.size != filled.toSet().size
    }

    for (i in 0 until SIZE) {
        if (hasDuplicates(board[i].toList())) return false
        if (hasDuplicates((0 until SIZE).map { r -> board[r][i] })) return false
    }

    var boxR = 0
    while (boxR < SIZE) {
        var boxC = 0
        while (boxC < SIZE) {
            val boxVals = mutableListOf<Int>()
            for (r in boxR until boxR + BOX) {
                for (c in boxC until boxC + BOX) {
                    boxVals.add(board[r][c])
                }
            }
            if (hasDuplicates(boxVals)) return false
            boxC += BOX
        }
        boxR += BOX
    }
    return true
}

/**
 * Find up to [limit] solutions for [board] via backtracking.
 *
 * Returning multiple solutions (rather than stopping at the first)
 * lets the generator confirm a puzzle has a UNIQUE solution: if
 * solve(board, limit=2) finds 2, the puzzle is ambiguous.
 */
fun solve(board: Board, limit: Int = 2): List<Board> {
    if (!givensAreConsistent(board)) return emptyList()

    val solutions = mutableListOf<Board>()
    val work = board.deepCopy()

    fun backtrack(): Boolean {
        if (solutions.size >= limit) return true // stop early, we have enough

        val spot = findMostConstrained(work)
        if (spot == null) {
            solutions.add(work.deepCopy())
            return solutions.size >= limit
        }

        if (spot.candidates.isEmpty()) return false // dead end

        for (value in spot.candidates) {
            work[spot.row][spot.col] = value
            if (backtrack()) return true
            work[spot.row][spot.col] = 0
        }
        return false
    }

    backtrack()
    return solutions
}

fun hasUniqueSolution(board: Board): Boolean = solve(board, limit = 2).size == 1

/** Convenience wrapper: return a single solved board, or null. */
fun solveOne(board: Board): Board? = solve(board, limit = 1).firstOrNull()
