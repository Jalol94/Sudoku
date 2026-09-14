package com.example.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.sudoku.ui.SudokuApp
import com.example.sudoku.ui.SudokuViewModel
import com.example.sudoku.ui.SudokuViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: SudokuViewModel by viewModels {
        SudokuViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SudokuApp(viewModel)
        }
    }
}
