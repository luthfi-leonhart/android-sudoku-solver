package example.sudokusolver.game

class Cell(
    val row: Int,
    val col: Int,
    var value: Int,
    var isStartingCell: Boolean = false,
    var isConflictingCell: Boolean = false,
    var notes: MutableSet<Int> = mutableSetOf()
)