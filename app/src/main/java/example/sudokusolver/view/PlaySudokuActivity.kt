package example.sudokusolver.view

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import example.sudokusolver.R
import example.sudokusolver.game.Cell
import example.sudokusolver.view.custom.SudokuBoardView
import example.sudokusolver.viewmodel.PlaySudokuViewModel
import kotlinx.android.synthetic.main.activity_play_sudoku.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

// https://github.com/patrickfeltes/sudoku-android-kotlin
class PlaySudokuActivity : AppCompatActivity(), SudokuBoardView.OnTouchListener {
    private lateinit var viewModel: PlaySudokuViewModel
    private lateinit var numberButtons: List<TextView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_sudoku)

        sudokuBoardView.registerListener(this)

        viewModel = ViewModelProviders.of(this).get(PlaySudokuViewModel::class.java)
        viewModel.sudokuGame.selectedCellLiveData.observe(
            this,
            Observer { updateSelectedCellUI(it) }
        )
        viewModel.sudokuGame.cellsLiveData.observe(
            this,
            Observer { updateCells(it) }
        )
        viewModel.sudokuGame.isTakingNotesLiveData.observe(
            this,
            Observer { updateNoteTakingUI(it) }
        )
        viewModel.sudokuGame.highlightedKeysLiveData.observe(
            this,
            Observer { updateHighlightedKeys(it) }
        )

        numberButtons = listOf(
            oneButton, twoButton, threeButton, fourButton, fiveButton, sixButton,
            sevenButton, eightButton, nineButton
        )

        numberButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                //viewModel.sudokuGame.handleInput(index + 1)
                Toast.makeText(this, "This is just a gimmick! Coming soon!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        btnNewGame.setOnClickListener {
            tvTimer.reset()
            viewModel.sudokuGame.generate()
        }
        btnSolve.setOnClickListener { solveSudoku() }
//        notesButton.setOnClickListener { viewModel.sudokuGame.changeNoteTakingState() }
//        deleteButton.setOnClickListener { viewModel.sudokuGame.delete() }
    }

    private fun solveSudoku() {
        tvTimer.start()

        GlobalScope.launch {
            val solved = viewModel.sudokuGame.solve()
            tvTimer.stop()

            if (solved) return@launch

            runOnUiThread {
                Toast.makeText(
                    this@PlaySudokuActivity,
                    "This board cannot be solved",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateCells(cells: List<Cell>?) = cells?.let {
        sudokuBoardView.updateCells(cells)
    }

    private fun updateSelectedCellUI(cell: Pair<Int, Int>?) = cell?.let {
        sudokuBoardView.updateSelectedCellUI(cell.first, cell.second)
    }

    private fun updateNoteTakingUI(isNoteTaking: Boolean?) = isNoteTaking?.let {
        val color = if (it) ContextCompat.getColor(this, R.color.colorPrimary) else Color.LTGRAY
//        notesButton.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
    }

    private fun updateHighlightedKeys(set: Set<Int>?) = set?.let {
        numberButtons.forEachIndexed { index, button ->
            val color = if (set.contains(index + 1)) ContextCompat.getColor(
                this,
                R.color.colorPrimary
            ) else Color.LTGRAY
            button.background.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
        }
    }

    override fun onCellTouched(row: Int, col: Int) {
        viewModel.sudokuGame.updateSelectedCell(row, col)
    }
}
