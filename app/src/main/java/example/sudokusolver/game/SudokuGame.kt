package example.sudokusolver.game

import androidx.lifecycle.MutableLiveData
import example.sudokusolver.GRID_SIZE
import example.sudokusolver.GRID_SIZE_SQUARE_ROOT

class SudokuGame {
    var selectedCellLiveData = MutableLiveData<Pair<Int, Int>>()
    var cellsLiveData = MutableLiveData<List<Cell>>()
    val isTakingNotesLiveData = MutableLiveData<Boolean>()
    val highlightedKeysLiveData = MutableLiveData<Set<Int>>()

    var hasConflictingCells = false
    private var selectedRow = -1
    private var selectedCol = -1
    private var isTakingNotes = false

    private lateinit var board: Board

    init {
        reset()
    }

    fun reset() {
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

        cell.isStartingCell = true

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

        hasConflictingCells = checkConflictingCells()
        cellsLiveData.postValue(board.cells)
    }

    private fun checkConflictingCells(): Boolean {
        var hasConflictingCells = false
        for (i in 0 until GRID_SIZE) {
            for (j in 0 until GRID_SIZE) {
                val cell = board.getCell(i, j)

                if (cell.value == 0) continue

                cell.isConflictingCell = hasConflictingCells(i, j, cell.value)
                if (cell.isConflictingCell) {
                    hasConflictingCells = true
                }
            }
        }
        return hasConflictingCells
    }

    private fun hasConflictingCells(row: Int, col: Int, digit: Int): Boolean {
        return hasConflictInRows(row, col, digit) ||
                hasConflictInColumns(row, col, digit) ||
                hasConflictInBox(row, col, digit)
    }

    private fun hasConflictInRows(row: Int, col: Int, digit: Int): Boolean {
        var conflicted = false
        for (i in 0 until GRID_SIZE) {
            val cell = board.getCell(row, i)
            if (col != i && cell.value == digit) {
                cell.isConflictingCell = true
                conflicted = true
            }
        }
        return conflicted
    }

    private fun hasConflictInColumns(row: Int, col: Int, digit: Int): Boolean {
        var conflicted = false
        for (i in 0 until GRID_SIZE) {
            val cell = board.getCell(i, col)
            if (row != i && cell.value == digit) {
                cell.isConflictingCell = true
                conflicted = true
            }
        }
        return conflicted
    }

    private fun hasConflictInBox(row: Int, col: Int, digit: Int): Boolean {
        val rowStart = findBoxStart(row)
        val columnStart = findBoxStart(col)

        var conflicted = false
        for (i in 0 until GRID_SIZE_SQUARE_ROOT) {
            for (j in 0 until GRID_SIZE_SQUARE_ROOT) {
                if (rowStart + i == row && columnStart + j == col) continue

                val cell = board.getCell(rowStart + i, columnStart + j)
                if (cell.value == digit) {
                    cell.isConflictingCell = true
                    conflicted = true
                }
            }
        }
        return conflicted
    }

    private fun findBoxStart(index: Int) = index - index % GRID_SIZE_SQUARE_ROOT

    fun updateSelectedCell(row: Int, col: Int) {
        val cell = board.getCell(row, col)
        selectedRow = row
        selectedCol = col
        selectedCellLiveData.postValue(Pair(row, col))

        if (isTakingNotes) {
            highlightedKeysLiveData.postValue(cell.notes)
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
        if (selectedRow < 0 || selectedCol < 0) return

        val cell = board.getCell(selectedRow, selectedCol)
        if (isTakingNotes) {
            cell.notes.clear()
            highlightedKeysLiveData.postValue(setOf())
        } else {
            cell.value = 0
            cell.isStartingCell = false
            cell.isConflictingCell = false
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
        val grid = copyCellsToGrid()
        val solvable = Solver.solvable(grid)

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

    private fun copyCellsToGrid(): Array<IntArray> {
        val grid = Array(GRID_SIZE) { IntArray(GRID_SIZE) { 0 } }

        for (i in 0 until GRID_SIZE) {
            for (j in 0 until GRID_SIZE) {
                val cell = board.getCell(i, j)
                grid[i][j] = cell.value
            }
        }

        return grid
    }

    val minimumNumberOfClues = Level.SENIOR.numberOfProvidedDigits

    var totalFilledCells: Int = 0
        private set
        get() {
            var total = 0

            board.cells.forEach {
                if (it.value != 0) total++
            }

            return total
        }
}
