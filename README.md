# Sudoku (Kotlin / Jetpack Compose)

A native Android Sudoku app, written from scratch in Kotlin with Jetpack
Compose.

## Features

- **Puzzle generator** that fills a random valid grid and removes cells
  (symmetrically, like published puzzles) while guaranteeing the result
  keeps a **unique solution** — no guess-required puzzles.
- **Four difficulties** (Easy / Medium / Hard / Expert), tuned by clue count.
- **Backtracking solver** with a minimum-remaining-values (MRV) heuristic:
  it fills the most-constrained empty cell first rather than scanning in
  fixed order, which matters a lot for how many times the generator has
  to check uniqueness during puzzle generation.
- Full game UI: pencil-mark notes, undo, mistake tracking, per-digit
  remaining-count badges, row/column/box highlighting, conflict
  highlighting, live timer.
- **Save/resume** via SharedPreferences (JSON-encoded), plus best-time
  tracking per difficulty.
- Puzzle generation runs on a background coroutine (`Dispatchers.Default`)
  so the UI thread never blocks.

## Project structure

```
app/src/main/java/com/example/sudoku/
├── MainActivity.kt
├── logic/                  # Pure Kotlin, zero Android/Compose dependencies
│   ├── Solver.kt           # Backtracking solver + MRV heuristic + uniqueness check
│   ├── Generator.kt        # Puzzle generation by difficulty
│   └── GameBoard.kt        # Mutable game state (notes, undo, hints, mistakes)
├── data/
│   └── GameStorage.kt      # SharedPreferences + org.json save/resume, best times
└── ui/
    ├── SudokuViewModel.kt  # StateFlow-based UI state, coroutine timer
    ├── SudokuApp.kt        # Root composable, wires ViewModel to screens
    ├── screens/
    │   ├── HomeScreen.kt   # Difficulty picker, resume, best times
    │   └── GameScreen.kt   # Grid, toolbar, timer, win dialog
    ├── components/
    │   ├── SudokuGrid.kt   # Custom 9x9 grid with box borders + highlighting
    │   └── NumberPad.kt    # 1-9 input row with remaining-count badges
    └── theme/              # Material3 color scheme + typography

app/src/test/java/com/example/sudoku/logic/
└── SudokuLogicTest.kt      # JUnit suite for solver/generator/board
```

`logic/` has no Android imports at all — it's plain Kotlin, which is what
makes it possible to unit-test with plain JUnit (no emulator, no
Robolectric).

## What's actually been verified vs. what hasn't

Being upfront about this because it matters for a portfolio piece:

- **`logic/` is real, tested code.** Compiled directly with the Kotlin
  compiler (this package has no Android dependencies, so it doesn't need
  the Android SDK) and run against a real JUnit suite — all 13 tests
  pass. This caught a real bug during development: an early version of
  the solver could hang forever on a board with contradictory pre-filled
  cells, since it only validated new placements rather than the starting
  state. That's fixed (`givensAreConsistent`) and covered by a test.
- **`ui/` and `data/` have not been compiled or run.** Building and
  running the Compose UI requires the Android SDK and Android Studio.
  They're written using standard, well-established Compose/ViewModel/
  coroutines patterns, but expect to fix small issues (an import, a
  parameter name) on first build, the way any hand-written Compose code
  needs a first-compile pass.

## Opening the project

1. Open this folder in **Android Studio** (Koala or newer recommended).
2. Android Studio will offer to regenerate the Gradle wrapper if it's
   missing — accept that, or run `gradle wrapper` once if you have a
   local Gradle install.
3. Let Gradle sync (it will pull the Compose BOM, Material3, and
   lifecycle/coroutines dependencies listed in `app/build.gradle.kts`).
4. Run on an emulator or device (minSdk 24 / Android 7.0+).

## Running the unit tests

Once open in Android Studio: right-click `app/src/test/.../SudokuLogicTest.kt`
→ **Run**. Or from the command line once the wrapper is set up:

```bash
./gradlew testDebugUnitTest
```

## Dependency versions

Pinned in `app/build.gradle.kts`: AGP 8.5.2, Kotlin 1.9.24, Compose BOM
2024.06.00, compileSdk/targetSdk 34, minSdk 24. Android Studio may prompt
you to update these — fine to accept.

## Possible next steps

- Persist multiple in-progress games instead of one
- Daily-challenge mode with a seeded puzzle
- Settings screen (dark theme, larger text, solution-based vs.
  rules-based mistake checking)
- Instrumented Compose UI tests (`androidTest/`) exercising cell
  selection, digit entry, and the win dialog
- App icon polish — the current one is a simple generated placeholder
  (a grid motif), fine to swap via Android Studio's Image Asset tool

## License

MIT — see `LICENSE`.
