package net.dean.easyarchive

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.pawegio.kandroid.find
import com.pawegio.kandroid.i
import net.dean.easyarchive.library.ArchiveAction
import net.dean.easyarchive.library.ArchiveEvent
import java.io.File

/**
 * Indicates an Inflater's progress. This view has a `ProgressBar` to show overall progress, a `TextView` to show the
 * name of the file being inflated, and another `TextView` to show progress in terms of current and total files.
 */
class ProgressIndicatorView : RelativeLayout {
    /** Number of files that have been inflated */
    private var current = 0
        set(value) {
            if (current > total)
                throw IllegalArgumentException("current cannot be greater than total")
            field = value
            val progress = ((current.toFloat() / total.toFloat()) * 100).toInt()
            progressBar.progress = progress
            checkDone()
        }
    /** Total amount of files that needs to be inflated */
    var total = -1
        set(value) {
            field = value
            checkDone()
        }

    private var done = false
        set(value) {
            field = value
            // Hide filename when done
            filename.visibility = if (done) GONE else VISIBLE
        }
    private val progressBar: ProgressBar by lazy { find<ProgressBar>(R.id.progress_bar) }
    private val filename: TextView by lazy { find<TextView>(R.id.filename) }
    private val fileCounter: TextView by lazy { find<TextView>(R.id.file_counter) }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    init {
        inflate(context, R.layout.view_progress_indicator, this)

        if (isInEditMode) {
            // Default values for previewing in an IDE
            val current = 12
            val total = 154
            find<TextView>(R.id.file_counter).text = "$current/$total"
            find<ProgressBar>(R.id.progress_bar).progress = 8 // 12 / 154
            find<TextView>(R.id.filename).text = "/mnt/sdcard/filename"
        }
    }

    /**
     * Updates the view with new progress
     *
     * @param f The file being inflated
     * @param current How many files have been inflated so far, including this one
     */
    fun postUpdate(event: ArchiveEvent) {
        when (event.action) {
            ArchiveAction.START -> {
                visibility = VISIBLE
                total = event.total
            }
            ArchiveAction.INFLATE -> {
                filename.text = event.file.absolutePath
                this.current = current
            }
            ArchiveAction.DONE -> {
                visibility = GONE
            }
            else -> i("Ignoring event $event")
        }
    }

    private fun checkDone() {
        done = current == total
        if (done)
            fileCounter.setText(R.string.done)
        else
            fileCounter.text = "$current/$total"

    }
}
