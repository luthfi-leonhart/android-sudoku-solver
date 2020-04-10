package example.sudokusolver.view.custom

import android.content.Context
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import java.text.DecimalFormat

/*
 * The Android chronometer widget revised so as to count milliseconds
 * https://github.com/antoniom/Millisecond-Chronometer
 *
 * With some revision to show milliseconds as per thousands
 */
class Chronometer @JvmOverloads constructor(
    context: Context?,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : AppCompatTextView(context, attrs, defStyle) {
    interface OnChronometerTickListener {
        fun onChronometerTick(chronometer: Chronometer?)
    }

    private var mBase: Long = 0
    private var mVisible = false
    private var mStarted = false
    private var mRunning = false
    var onChronometerTickListener: OnChronometerTickListener? = null
    private var timeElapsed: Long = 0

    private fun init() {
        mBase = SystemClock.elapsedRealtime()
        updateText(mBase)
    }

    var base: Long
        get() = mBase
        set(base) {
            mBase = base
            dispatchChronometerTick()
            updateText(SystemClock.elapsedRealtime())
        }

    fun reset(){
        init()
    }

    fun start() {
        mStarted = true
        updateRunning()
    }

    fun stop() {
        mStarted = false
        updateRunning()
    }

    fun setStarted(started: Boolean) {
        mStarted = started
        updateRunning()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mVisible = false
        updateRunning()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        mVisible = visibility == View.VISIBLE
        updateRunning()
    }

    @Synchronized
    private fun updateText(now: Long) {
        timeElapsed = now - mBase
        val df = DecimalFormat("00")

        val hours = (timeElapsed / (3600 * 1000))
        var remaining = (timeElapsed % (3600 * 1000))

        val minutes = (remaining / (60 * 1000))
        remaining = (remaining % (60 * 1000))

        val seconds = (remaining / 1000)

        val milliseconds = (timeElapsed % 1000)

        var text: String? = ""

        if (hours > 0) {
            text += df.format(hours) + ":"
        }

        text += df.format(minutes) + ":"
        text += df.format(seconds) + ":"

        val df2 = DecimalFormat("000")
        text += df2.format(milliseconds)

        setText(text)
    }

    private fun updateRunning() {
        val running = mVisible && mStarted
        if (running != mRunning) {
            if (running) {
                updateText(SystemClock.elapsedRealtime())
                dispatchChronometerTick()
                mHandler.sendMessageDelayed(
                    Message.obtain(
                        mHandler,
                        TICK_WHAT
                    ), 1
                )
            } else {
                mHandler.removeMessages(TICK_WHAT)
            }
            mRunning = running
        }
    }

    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(m: Message) {
            if (mRunning) {
                updateText(SystemClock.elapsedRealtime())
                dispatchChronometerTick()
                sendMessageDelayed(
                    Message.obtain(
                        this,
                        TICK_WHAT
                    ),
                    100
                )
            }
        }
    }

    fun dispatchChronometerTick() {
        if (onChronometerTickListener != null) {
            onChronometerTickListener!!.onChronometerTick(this)
        }
    }

    companion object {
        private const val TAG = "Chronometer"
        private const val TICK_WHAT = 2
    }

    init {
        init()
    }
}