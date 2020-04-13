package example.sudokusolver.view

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
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
            btnOne, btnTwo, btnThree, btnFour, btnFive, btnSix,
            btnSeven, btnEight, btnNine
        )

        numberButtons.forEachIndexed { index, button ->
            button.setOnClickListener {
                viewModel.sudokuGame.handleInput(index + 1)
            }
        }

        btnClear.setOnClickListener {
            tvTimer.stop()
            tvTimer.reset()
            viewModel.sudokuGame.reset()
        }
        btnRandomGame.setOnClickListener {
            tvTimer.stop()
            tvTimer.reset()
            viewModel.sudokuGame.generate()
        }
        btnErase.setOnClickListener {
            viewModel.sudokuGame.delete()
        }
        btnSolve.setOnClickListener { solveSudoku() }
//        notesButton.setOnClickListener { viewModel.sudokuGame.changeNoteTakingState() }
    }

    private fun solveSudoku() {
        val totalFilledCells = viewModel.sudokuGame.totalFilledCells
        val minimumNumberOfClues = viewModel.sudokuGame.minimumNumberOfClues

        if (totalFilledCells < minimumNumberOfClues) {
            val cluesLeft = minimumNumberOfClues - totalFilledCells
            showDialog("The minimum clues to solve is $minimumNumberOfClues. You need $cluesLeft more clue(s).")
            return
        }

        if (viewModel.sudokuGame.hasConflictingCells) {
            showDialog("You have conflicting cells. Resolve it before proceeding further.")
            return
        }

        tvTimer.reset()
        tvTimer.start()
        btnSolve.isEnabled = false

        GlobalScope.launch {
            val solved = viewModel.sudokuGame.solve()
            tvTimer.stop()

            runOnUiThread {
                btnSolve.isEnabled = true
                if (!solved) showDialog("This board cannot be solved.")
            }
        }
    }

    private fun showDialog(message: String) {
        MaterialDialog(this, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
            message(text = message)
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
