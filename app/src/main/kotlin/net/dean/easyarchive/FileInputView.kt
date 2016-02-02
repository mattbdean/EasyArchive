package net.dean.easyarchive

import android.content.Context
import android.util.AttributeSet
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.pawegio.kandroid.find
import com.pawegio.kandroid.textWatcher
import net.dean.easyarchive.library.Severity
import net.dean.easyarchive.library.ValidationStatus
import java.io.File

public class FileInputView : RelativeLayout {
    private var validationStatus: ValidationStatus? = null
        set(value) {
            if (value == null) throw NullPointerException("validationStatus cannot be null")
            status.setText(statusFor(value))
            statusIcon.setImageResource(iconFor(value.severity))
            field = value
        }
    private var inputMode: InputMode by TardyNotNullVal()
    private val hint: Int
        get() = if (inputMode == InputMode.FILE) R.string.hint_input_file else R.string.hint_input_directory

    private val filename: EditText by lazy { find<EditText>(R.id.filename) }
    private val status: TextView by lazy { find<TextView>(R.id.status) }
    private val statusIcon: ImageView by lazy { find<ImageView>(R.id.status_icon) }

    constructor(context: Context) : super(context) {
        inputMode = InputMode.FILE
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context, attrs)
    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initialize(context, attrs)
    }

    private fun initialize(context: Context, attrs: AttributeSet) {
        val array = context.theme.obtainStyledAttributes(attrs, R.styleable.FileInputView, 0, 0)
        inputMode = InputMode.values()[array.getInteger(R.styleable.FileInputView_inputMode, 0)]

        inflate(context, R.layout.view_file_input, this)

        filename.setHint(hint)
        if (!isInEditMode) {
            filename.textWatcher {
                afterTextChanged { text ->
                    val f = File(text.toString())
                    validationStatus = if (inputMode == InputMode.FILE)
                        EasyArchive.inflaters.validateArchive(f)
                    else
                        EasyArchive.inflaters.validateDestination(f)
                }
            }
            whenDebug {
                filename.setText("/mnt/sdcard/sample" + if (inputMode == InputMode.FILE) ".zip" else "")
            }
        } else {
            find<TextView>(R.id.status).setText(R.string.vs_ready)
            find<ImageView>(R.id.status_icon).setImageResource(R.drawable.ic_serverity_fine)
        }
    }

    public fun file() = File(filename.text.toString())

    private fun statusFor(s: ValidationStatus?): Int =
            if (s == null) 0 else
                context.resources.getIdentifier("vs_${s.name.toLowerCase()}", "string", "net.dean.easyarchive")

    private fun iconFor(s: Severity): Int =
            if (s == Severity.SEVERE) R.drawable.ic_severity_severe else R.drawable.ic_serverity_fine
}

public enum class InputMode {
    FILE,
    DIRECTORY
}

