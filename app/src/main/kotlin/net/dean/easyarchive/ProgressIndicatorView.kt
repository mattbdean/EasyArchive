package net.dean.easyarchive

import android.content.Context
import android.util.AttributeSet
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import com.pawegio.kandroid.find
import java.io.File

public class ProgressIndicatorView : RelativeLayout {
    private var current = 0
        set(value) {
            if (current > total)
                throw IllegalArgumentException("current cannot be greater than total")
            field = value
            val progress = ((current.toFloat() / total.toFloat()) * 100).toInt()
            progressBar.progress = progress
            checkDone()
        }
    public var total = -1
        set(value) {
            field = value
            checkDone()
        }
    private var done = false
        set(value) {
            field = value
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
            val current = 12
            val total = 154
            find<TextView>(R.id.file_counter).text = "$current/$total"
            find<ProgressBar>(R.id.progress_bar).progress = 8 // 12 / 154
            find<TextView>(R.id.filename).text = "/mnt/sdcard/filename"
        }
    }

    public fun update(f: File, current: Int) {
        filename.text = f.absolutePath
        this.current = current
    }

    private fun checkDone() {
        done = current == total
        if (done)
            fileCounter.setText(R.string.done)
        else
            fileCounter.text = "$current/$total"

    }
}
