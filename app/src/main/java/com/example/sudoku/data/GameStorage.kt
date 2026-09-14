package com.example.sudoku.data

import android.content.Context
import com.example.sudoku.logic.Difficulty
import com.example.sudoku.logic.GameSnapshot
import com.example.sudoku.logic.SIZE
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local save/resume + best-time persistence, backed by SharedPreferences.
 *
 * Uses org.json (built into Android, no extra Gradle dependency) rather
 * than a serialization library, since the data shape here is small and
 * simple enough that a library would be pure overhead.
 */
class GameStorage(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getBestTime(difficulty: Difficulty): Int? {
        val key = bestTimeKey(difficulty)
        return if (prefs.contains(key)) prefs.getInt(key, 0) else null
    }

    fun setBestTime(difficulty: Difficulty, seconds: Int) {
        val key = bestTimeKey(difficulty)
        val current = getBestTime(difficulty)
        if (current == null || seconds < current) {
            prefs.edit().putInt(key, seconds).apply()
        }
    }

    fun saveGame(snapshot: GameSnapshot) {
        prefs.edit().putString(KEY_IN_PROGRESS, snapshotToJson(snapshot).toString()).apply()
    }

    fun loadGame(): GameSnapshot? {
        val raw = prefs.getString(KEY_IN_PROGRESS, null) ?: return null
        return try {
            snapshotFromJson(JSONObject(raw))
        } catch (e: Exception) {
            // Corrupt or outdated save data — treat as "nothing to resume"
            // rather than crashing the app on launch.
            null
        }
    }

    fun clearGame() {
        prefs.edit().remove(KEY_IN_PROGRESS).apply()
    }

    private fun bestTimeKey(difficulty: Difficulty) = "best_time_${difficulty.name}"

    companion object {
        private const val PREFS_NAME = "sudoku_save"
        private const val KEY_IN_PROGRESS = "in_progress"

        private fun boardToJson(board: Array<IntArray>): JSONArray {
            val rows = JSONArray()
            for (row in board) {
                val jsonRow = JSONArray()
                for (v in row) jsonRow.put(v)
                rows.put(jsonRow)
            }
            return rows
        }

        private fun boardFromJson(json: JSONArray): Array<IntArray> = Array(SIZE) { r ->
            val jsonRow = json.getJSONArray(r)
            IntArray(SIZE) { c -> jsonRow.getInt(c) }
        }

        private fun notesToJson(notes: List<List<Set<Int>>>): JSONArray {
            val rows = JSONArray()
            for (row in notes) {
                val jsonRow = JSONArray()
                for (cellNotes in row) {
                    val arr = JSONArray()
                    for (n in cellNotes.sorted()) arr.put(n)
                    jsonRow.put(arr)
                }
                rows.put(jsonRow)
            }
            return rows
        }

        private fun notesFromJson(json: JSONArray): List<List<Set<Int>>> = (0 until SIZE).map { r ->
            val jsonRow = json.getJSONArray(r)
            (0 until SIZE).map { c ->
                val arr = jsonRow.getJSONArray(c)
                (0 until arr.length()).map { i -> arr.getInt(i) }.toSet()
            }
        }

        fun snapshotToJson(snapshot: GameSnapshot): JSONObject = JSONObject().apply {
            put("difficulty", snapshot.difficulty.name)
            put("given", boardToJson(snapshot.given))
            put("values", boardToJson(snapshot.values))
            put("solution", boardToJson(snapshot.solution))
            put("notes", notesToJson(snapshot.notes))
            put("mistakes", snapshot.mistakes)
            put("elapsedSeconds", snapshot.elapsedSeconds)
        }

        fun snapshotFromJson(json: JSONObject): GameSnapshot = GameSnapshot(
            difficulty = Difficulty.valueOf(json.getString("difficulty")),
            given = boardFromJson(json.getJSONArray("given")),
            values = boardFromJson(json.getJSONArray("values")),
            solution = boardFromJson(json.getJSONArray("solution")),
            notes = notesFromJson(json.getJSONArray("notes")),
            mistakes = json.getInt("mistakes"),
            elapsedSeconds = json.getInt("elapsedSeconds")
        )
    }
}
