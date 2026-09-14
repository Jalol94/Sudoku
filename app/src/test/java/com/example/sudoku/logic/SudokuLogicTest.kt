package com.example.sudoku.logic

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

private fun isValidCompleteBoard(board: Board): Boolean {
    for (i in 0 until SIZE) {
        val rowVals = board[i].toList()
        val colVals = (0 until SIZE).map { r -> board[r][i] }
        if (rowVals.sorted() != (1..9).toList()) return false
        if (colVals.sorted() != (1..9).toList()) return false
    }
    var boxR = 0
    while (boxR < SIZE) {
        var boxC = 0
        while (boxC < SIZE) {
            val boxVals = mutableListOf<Int>()
            for (r in boxR until boxR + BOX) for (c in boxC until boxC + BOX) boxVals.add(board[r][c])
            if (boxVals.sorted() != (1..9).toList()) return false
            boxC += BOX
        }
        boxR += BOX
    }
    return true
}

private fun countClues(board: Board): Int = board.map { row -> row.count { it != 0 } }.sum()

class SolverTest {
    @Test
    fun `isValid rejects row duplicate`() {
        val board = emptyBoard()
        board[0][0] = 5
        assertFalse(isValid(board, 0, 3, 5))
    }

    @Test
    fun `isValid rejects box duplicate`() {
        val board = emptyBoard()
        board[0][0] = 7
        assertFalse(isValid(board, 1, 1, 7))
    }

    @Test
    fun `solveOne produces a valid complete board`() {
        val gen = generatePuzzle(Difficulty.EASY, Random(1))
        val solved = solveOne(gen.puzzle)
        assertNotNull(solved)
        assertTrue(isValidCompleteBoard(solved!!))
    }

    @Test
    fun `contradictory givens return no solution instead of hanging`() {
        val board = emptyBoard()
        board[0][0] = 5
        board[0][1] = 5 // immediately contradictory
        assertNull(solveOne(board))
    }
}

class GeneratorTest {
    @Test
    fun `every difficulty produces a uniquely solvable puzzle`() {
        for (difficulty in Difficulty.values()) {
            val gen = generatePuzzle(difficulty, Random(difficulty.ordinal.toLong()))
            assertTrue("$difficulty puzzle should have a unique solution", hasUniqueSolution(gen.puzzle))
            assertTrue(isValidCompleteBoard(gen.solution))
        }
    }

    @Test
    fun `clue count lands at or just above target`() {
        for (difficulty in Difficulty.values()) {
            val gen = generatePuzzle(difficulty, Random(difficulty.ordinal.toLong() + 100))
            val clues = countClues(gen.puzzle)
            val target = CLUES_BY_DIFFICULTY.getValue(difficulty)
            assertTrue(
                "expected $target..${target + 3} clues for $difficulty, got $clues",
                clues in target..(target + 3)
            )
        }
    }

    @Test
    fun `puzzle cells match solution where filled`() {
        val gen = generatePuzzle(Difficulty.MEDIUM, Random(7))
        for (r in 0 until SIZE) {
            for (c in 0 until SIZE) {
                if (gen.puzzle[r][c] != 0) {
                    assertEquals(gen.solution[r][c], gen.puzzle[r][c])
                }
            }
        }
    }
}

class GameBoardTest {
    private fun makeBoard(seed: Long = 0): Triple<GameBoard, Board, Board> {
        val gen = generatePuzzle(Difficulty.EASY, Random(seed))
        return Triple(GameBoard(gen.puzzle, gen.solution), gen.puzzle, gen.solution)
    }

    private fun firstEmpty(puzzle: Board): Pair<Int, Int> =
        (0 until SIZE).flatMap { r -> (0 until SIZE).map { c -> r to c } }
            .first { (r, c) -> puzzle[r][c] == 0 }

    private fun firstGiven(puzzle: Board): Pair<Int, Int> =
        (0 until SIZE).flatMap { r -> (0 until SIZE).map { c -> r to c } }
            .first { (r, c) -> puzzle[r][c] != 0 }

    @Test
    fun `cannot overwrite a given cell`() {
        val (board, puzzle, _) = makeBoard()
        val (r, c) = firstGiven(puzzle)
        assertFalse(board.setValue(r, c, 1))
    }

    @Test
    fun `wrong value increments mistakes`() {
        val (board, puzzle, solution) = makeBoard()
        val (r, c) = firstEmpty(puzzle)
        val wrong = (1..9).first { it != solution[r][c] }
        board.setValue(r, c, wrong)
        assertEquals(1, board.mistakes)
    }

    @Test
    fun `undo reverts the last move`() {
        val (board, puzzle, solution) = makeBoard()
        val (r, c) = firstEmpty(puzzle)
        board.setValue(r, c, solution[r][c])
        assertEquals(solution[r][c], board.values[r][c])
        board.undo()
        assertEquals(0, board.values[r][c])
    }

    @Test
    fun `hint never counts as a mistake`() {
        val (board, puzzle, solution) = makeBoard()
        val (r, c) = firstEmpty(puzzle)
        board.hint(r, c)
        assertEquals(solution[r][c], board.values[r][c])
        assertEquals(0, board.mistakes)
    }

    @Test
    fun `isSolved is true only once every cell matches the solution`() {
        val (board, puzzle, solution) = makeBoard()
        assertFalse(board.isSolved())
        for (r in 0 until SIZE) for (c in 0 until SIZE) if (puzzle[r][c] == 0) board.setValue(r, c, solution[r][c])
        assertTrue(board.isSolved())
    }

    @Test
    fun `snapshot roundtrip preserves board state`() {
        val (board, puzzle, solution) = makeBoard()
        val (r, c) = firstEmpty(puzzle)
        board.setValue(r, c, solution[r][c])

        val snapshot = board.toSnapshot(Difficulty.EASY, elapsedSeconds = 42)
        val restored = GameBoard.fromSnapshot(snapshot)

        for (row in 0 until SIZE) {
            assertArrayEquals(board.values[row], restored.values[row])
            assertArrayEquals(board.given[row], restored.given[row])
            assertArrayEquals(board.solution[row], restored.solution[row])
        }
    }
}
