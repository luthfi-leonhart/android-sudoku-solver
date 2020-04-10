package example.sudokusolver.viewmodel

import androidx.lifecycle.ViewModel
import example.sudokusolver.game.SudokuGame

class PlaySudokuViewModel : ViewModel() {
    val sudokuGame = SudokuGame()
}