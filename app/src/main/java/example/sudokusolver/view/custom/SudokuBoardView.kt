package example.sudokusolver.view.custom

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import example.sudokusolver.GRID_SIZE
import example.sudokusolver.GRID_SIZE_SQUARE_ROOT
import example.sudokusolver.R
import example.sudokusolver.game.Cell
import kotlin.math.min

class SudokuBoardView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    // these are set in onDraw
    private var cellSizePixels = 0F
    private var noteSizePixels = 0F

    private var selectedRow = 0
    private var selectedCol = 0

    private var listener: OnTouchListener? = null

    private var cells: List<Cell>? = null

    private val thickLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = ContextCompat.getColor(context, R.color.colorAccent)
        strokeWidth = 6F
    }

    private val thinLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        color = Color.BLACK
        strokeWidth = 2F
    }

    private val selectedCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.parseColor("#6ead3a")
    }

    private val conflictingCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.parseColor("#efedef")
    }

    private val startingCellPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.parseColor("#acacac")
    }

    private val cellPaint1 = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(context, R.color.box1)
    }

    private val cellPaint2 = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(context, R.color.box2)
    }

    private val textPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.WHITE
    }

    private val startingCellTextPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = ContextCompat.getColor(context, R.color.colorAccent)
        typeface = Typeface.DEFAULT_BOLD
    }

    private val noteTextPaint = Paint().apply {
        style = Paint.Style.FILL_AND_STROKE
        color = Color.WHITE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val sizePixels = min(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(sizePixels, sizePixels)
    }

    override fun onDraw(canvas: Canvas) {
        updateMeasurements(width)
        fillCells(canvas)
        drawLines(canvas)
        drawText(canvas)
    }

    private fun updateMeasurements(width: Int) {
        cellSizePixels = width / GRID_SIZE.toFloat()
        noteSizePixels = cellSizePixels / GRID_SIZE_SQUARE_ROOT.toFloat()
        noteTextPaint.textSize = cellSizePixels / GRID_SIZE_SQUARE_ROOT.toFloat()
        textPaint.textSize = cellSizePixels / 1.5F
        startingCellTextPaint.textSize = cellSizePixels / 1.5F
    }

    private fun fillCells(canvas: Canvas) {
        cells?.forEach {
            val r = it.row
            val c = it.col

            val cellPaint = when ((r + c) % 2) {
                0 -> cellPaint1
                else -> cellPaint2
            }

            if (r == selectedRow && c == selectedCol) {
                drawSelectedCell(canvas, r, c, cellPaint)
            } else fillCell(canvas, r, c, cellPaint)

            // Highlight the conflicting rows, columns, and the box this cell belongs to
//            if (it.isStartingCell) {
//                fillCell(canvas, r, c, startingCellPaint)
//            } else if (r == selectedRow || c == selectedCol) {
//                fillCell(canvas, r, c, conflictingCellPaint)
//            } else if (r / sqrtSize == selectedRow / sqrtSize && c / sqrtSize == selectedCol / sqrtSize) {
//                fillCell(canvas, r, c, conflictingCellPaint)
//            }
        }
    }

    private fun fillCell(canvas: Canvas, r: Int, c: Int, paint: Paint) {
        canvas.drawRect(
            c * cellSizePixels,
            r * cellSizePixels,
            (c + 1) * cellSizePixels,
            (r + 1) * cellSizePixels,
            paint
        )
    }

    private fun drawSelectedCell(canvas: Canvas, r: Int, c: Int, paint: Paint) {
        canvas.drawRect(
            c * cellSizePixels,
            r * cellSizePixels,
            (c + 1) * cellSizePixels,
            (r + 1) * cellSizePixels,
            paint
        )

        val rectF = RectF(
            c * cellSizePixels,
            r * cellSizePixels,
            (c + 1) * cellSizePixels - 2,
            (r + 1) * cellSizePixels - 2
        )
        canvas.drawRoundRect(rectF, 8f, 8f, thickLinePaint)
    }

    private fun drawLines(canvas: Canvas) {
        val rectF = RectF(0F, 0F, width.toFloat(), height.toFloat())
        canvas.drawRoundRect(rectF, 16f, 16f, thickLinePaint)

        // Draw vertical and horizontal lines inside the board
        for (i in 1 until GRID_SIZE) {
//            val paintToUse = when (i % sqrtSize) {
//                0 -> thickLinePaint
//                else -> thinLinePaint
//            }

            // Draw only the thick line
            if (i % GRID_SIZE_SQUARE_ROOT != 0) continue

            val paintToUse = thickLinePaint

            canvas.drawLine(
                i * cellSizePixels,
                0F,
                i * cellSizePixels,
                height.toFloat(),
                paintToUse
            )

            canvas.drawLine(
                0F,
                i * cellSizePixels,
                width.toFloat(),
                i * cellSizePixels,
                paintToUse
            )
        }
    }

    private fun drawText(canvas: Canvas) {
        cells?.forEach { cell ->
            val value = cell.value

            if (value == 0) {
                // draw notes
                cell.notes.forEach { note ->
                    val rowInCell = (note - 1) / GRID_SIZE_SQUARE_ROOT
                    val colInCell = (note - 1) % GRID_SIZE_SQUARE_ROOT
                    val valueString = note.toString()

                    val textBounds = Rect()
                    noteTextPaint.getTextBounds(valueString, 0, valueString.length, textBounds)
                    val textWidth = noteTextPaint.measureText(valueString)
                    val textHeight = textBounds.height()
                    canvas.drawText(
                        valueString,
                        (cell.col * cellSizePixels) + (colInCell * noteSizePixels) + noteSizePixels / 2 - textWidth / 2f,
                        (cell.row * cellSizePixels) + (rowInCell * noteSizePixels) + noteSizePixels / 2 + textHeight / 2f,
                        noteTextPaint
                    )
                }
            } else {
                val row = cell.row
                val col = cell.col
                val valueString = cell.value.toString()

                val paintToUse = if (cell.isStartingCell) startingCellTextPaint else textPaint
                val textBounds = Rect()
                paintToUse.getTextBounds(valueString, 0, valueString.length, textBounds)
                val textWidth = paintToUse.measureText(valueString)
                val textHeight = textBounds.height()

                canvas.drawText(
                    valueString, (col * cellSizePixels) + cellSizePixels / 2 - textWidth / 2,
                    (row * cellSizePixels) + cellSizePixels / 2 + textHeight / 2, paintToUse
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                handleTouchEvent(event.x, event.y)
                true
            }
            else -> false
        }
    }

    private fun handleTouchEvent(x: Float, y: Float) {
        val possibleSelectedRow = (y / cellSizePixels).toInt()
        val possibleSelectedCol = (x / cellSizePixels).toInt()
        listener?.onCellTouched(possibleSelectedRow, possibleSelectedCol)
    }

    fun updateSelectedCellUI(row: Int, col: Int) {
        selectedRow = row
        selectedCol = col
        invalidate()
    }

    fun updateCells(cells: List<Cell>) {
        this.cells = cells
        invalidate()
    }

    fun registerListener(listener: OnTouchListener) {
        this.listener = listener
    }

    interface OnTouchListener {
        fun onCellTouched(row: Int, col: Int)
    }
}
