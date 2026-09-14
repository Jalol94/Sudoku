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

🟡 Concept stage — game logic and UI not yet implemented.

## License

MIT — see `LICENSE`.
