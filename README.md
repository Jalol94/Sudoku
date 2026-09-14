# Sudoku (Kotlin / Jetpack Compose)

A native Android Sudoku app, built from scratch in Kotlin.

## The idea

A Sudoku puzzle game for Android, built with a from-scratch generator and
solver (no puzzle library) and a native Jetpack Compose UI.

### Planned features

- Puzzle generator that produces a fresh, random grid and removes cells
  down to a target clue count per difficulty, while guaranteeing every
  generated puzzle has a **unique solution**.
- Four difficulties: Easy, Medium, Hard, Expert.
- Backtracking solver, reused by the generator (uniqueness checks) and
  by an in-game hint system.
- Full game UI: pencil-mark notes, undo, mistake tracking, conflict
  highlighting, row/column/box highlighting, live timer.
- Save/resume for an in-progress game, plus best-time tracking per
  difficulty.

### Planned tech stack

- **Kotlin**, no Java.
- **Jetpack Compose** + Material3 for the UI (no XML layouts).
- Game logic (solver/generator/board state) written as a plain Kotlin
  package with zero Android dependencies, so it can be unit-tested with
  plain JUnit.
- Local persistence via SharedPreferences (JSON-encoded) — no database
  needed for this scope.

### Planned structure

```
app/src/main/java/com/example/sudoku/
├── logic/     # Pure Kotlin: solver, generator, game state
├── data/      # Save/resume persistence
└── ui/        # Jetpack Compose screens and components
```

## Status

🟡 Logic layer done and tested. UI not yet implemented — the app doesn't
build/run yet, but the puzzle engine is real, working code.

### What exists so far

- `logic/` — the solver (backtracking + minimum-remaining-values
  heuristic), the generator (produces puzzles with a guaranteed unique
  solution), and `GameBoard` (mutable in-progress game state: notes,
  undo, hints, mistake tracking). Zero Android dependencies — plain
  Kotlin.
- `data/GameStorage.kt` — SharedPreferences + JSON save/resume and
  best-time tracking, ready for the UI layer to call into.
- `app/src/test/.../SudokuLogicTest.kt` — JUnit suite covering the
  solver, generator (uniqueness + clue-count checks across all
  difficulties), and game state. Run with:

  ```bash
  ./gradlew testDebugUnitTest
  ```

### Coming next

- Jetpack Compose UI: home screen (difficulty picker), game screen
  (grid, number pad, toolbar, timer), and the ViewModel wiring it to
  `logic/` and `data/`.


## License

MIT — see `LICENSE`.
