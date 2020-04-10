package example.sudokusolver.game

import androidx.lifecycle.MutableLiveData
import example.sudokusolver.GRID_SIZE

class SudokuGame {
    var selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    var cellsLiveData = MutableLiveData<List<Cell>>()
    val isTakingNotesLiveData = MutableLiveData<Boolean>()
    val highlightedKeysLiveData = MutableLiveData<Set<Int>>()

    private var selectedRow = -1
    private var selectedCol = -1
    private var isTakingNotes = false

    private lateinit var board: Board

    init {
        reset()
    }

    private fun reset() {
        // Default value 0 as empty cell
        val cells = List(9 * 9) { i -> Cell(i / 9, i % 9, 0) }
//        val cells = List(9 * 9) { i -> Cell(i / 9, i % 9, i % 9) }
//        cells[0].notes = mutableSetOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
        board = Board(9, cells)

        selectedCellLiveData.postValue(Pair(selectedRow, selectedCol))
        cellsLiveData.postValue(board.cells)
        isTakingNotesLiveData.postValue(isTakingNotes)
    }

    fun handleInput(number: Int) {
        if (selectedRow == -1 || selectedCol == -1) return
        val cell = board.getCell(selectedRow, selectedCol)
        if (cell.isStartingCell) return

        if (isTakingNotes) {
            if (cell.notes.contains(number)) {
                cell.notes.remove(number)
            } else {
                cell.notes.add(number)
            }
            highlightedKeysLiveData.postValue(cell.notes)
        } else {
            cell.value = number
        }
        cellsLiveData.postValue(board.cells)
    }


    fun updateSelectedCell(row: Int, col: Int) {
        val cell = board.getCell(row, col)
        if (!cell.isStartingCell) {
            selectedRow = row
            selectedCol = col
            selectedCellLiveData.postValue(Pair(row, col))

            if (isTakingNotes) {
                highlightedKeysLiveData.postValue(cell.notes)
            }
        }
    }

    fun changeNoteTakingState() {
        isTakingNotes = !isTakingNotes
        isTakingNotesLiveData.postValue(isTakingNotes)

        val curNotes = if (isTakingNotes) {
            board.getCell(selectedRow, selectedCol).notes
        } else {
            setOf<Int>()
        }
        highlightedKeysLiveData.postValue(curNotes)
    }

    fun delete() {
        val cell = board.getCell(selectedRow, selectedCol)
        if (isTakingNotes) {
            cell.notes.clear()
            highlightedKeysLiveData.postValue(setOf())
        } else {
            cell.value = 0
        }
        cellsLiveData.postValue(board.cells)
    }

    private lateinit var generator: Generator

    fun generate(level: Level? = null) {
        reset()

        val builder = Generator.Builder()
        level?.also {
            builder.setLevel(it)
        }
        generator = builder.build()

        copyGridToCells(generator.grid, true)
    }

    fun solve(): Boolean {
        val solvable = Solver.solvable(generator.grid)
        if (solvable) {
            copyGridToCells(Solver.grid, false)
            return true
        }

        return false
    }

    private fun copyGridToCells(grid: Array<IntArray>, isStartingCell: Boolean) {
        for (i in 0 until GRID_SIZE) {
            for (j in 0 until GRID_SIZE) {
                val cell = board.getCell(i, j)
                val value = grid[i][j]

                cell.value = value

                // If this cell is a starting cell before, continue the loop
                if (cell.isStartingCell) continue

                cell.isStartingCell = isStartingCell && value != 0
            }
        }
        cellsLiveData.postValue(board.cells)
    }
}
