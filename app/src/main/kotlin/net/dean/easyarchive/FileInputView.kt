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
            find<TextView>(R.id.status).setText(statusFor(value))
            find<ImageView>(R.id.status_icon).setImageResource(iconFor(value.severity))
            field = value
        }
    private var inputMode: InputMode by TardyNotNullVal()
    private val hint: Int
        get() = if (inputMode == InputMode.FILE) R.string.hint_input_file else R.string.hint_input_directory

    constructor(context: Context) : super(context) {
        inputMode = InputMode.FILE
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initialize(context, attrs)
    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initialize(context, attrs)
    }

    public fun initialize(context: Context, attrs: AttributeSet) {
        val array = context.theme.obtainStyledAttributes(attrs, R.styleable.FileInputView, 0, 0)
        inputMode = InputMode.values()[array.getInteger(R.styleable.FileInputView_inputMode, 0)]

        inflate(context, R.layout.view_file_input, this)

        if (!isInEditMode) {
            find<EditText>(R.id.filename).textWatcher {
                afterTextChanged { text ->
                    val f = File(text.toString())
                    validationStatus = if (inputMode == InputMode.FILE)
                        EasyArchive.inflaters.validateArchive(f)
                    else
                        EasyArchive.inflaters.validateDestination(f)
                }
            }
            find<EditText>(R.id.filename).setHint(hint)
        } else {
            find<EditText>(R.id.filename).setText(hint)
            find<TextView>(R.id.status).setText(R.string.vs_ready)
            find<ImageView>(R.id.status_icon).setImageResource(R.drawable.ic_good)
        }
    }

    private fun statusFor(s: ValidationStatus?): Int =
            if (s == null) 0 else context.resources.getIdentifier("vs_${s.name.toLowerCase()}", "string", "net.dean.easyarchive")
    private fun iconFor(s: Severity): Int =
            if (s == Severity.SEVERE) R.drawable.ic_bad else R.drawable.ic_good
}

public enum class InputMode {
    FILE,
    DIRECTORY
}

