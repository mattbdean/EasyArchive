package net.dean.easyarchive

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.widget.*
import com.nononsenseapps.filepicker.FilePickerActivity
import com.pawegio.kandroid.find
import com.pawegio.kandroid.textWatcher
import net.dean.easyarchive.library.Severity
import net.dean.easyarchive.library.ValidationStatus
import java.io.File

/**
 * Lets the user pick a file or directory and validates it as such. This view contains an `EditText` for user entry, a
 * `Button` that shows a file picker, an `ImageView` that shows an icon related to the file's validation status, and a
 * `TextView` to show why the given file or directory is or is not valid.
 */
class FileInputView : RelativeLayout {
    companion object {
        private const val CODE_FILE = 0
    }
    /** Called when the ValidationStatus is changed. */
    var onStatusChanged: () -> Unit = {}
    private var validationStatus: ValidationStatus? = null
        set(value) {
            if (value == null) {
                status.text = ""
                statusIcon.setImageDrawable(null)
            } else {
                status.setText(statusFor(value))
                statusIcon.setImageResource(iconFor(value.severity))
            }
            field = value
            onStatusChanged()
        }
    // Assigned in initialize()
    private var inputMode: InputMode by TardyNotNullVal()
    /** EditText hint */
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
        // Get inputMode from the XML. Assume FILE if none is given
        val array = context.theme.obtainStyledAttributes(attrs, R.styleable.FileInputView, 0, 0)
        inputMode = InputMode.values()[array.getInteger(R.styleable.FileInputView_inputMode, 0)]

        inflate(context, R.layout.view_file_input, this)

        filename.setHint(hint)
        if (!isInEditMode) {
            // Validate the file name after the text has been changed
            filename.textWatcher {
                afterTextChanged { text ->
                    if (text.isNullOrBlank()) {
                        validationStatus = null
                    } else {
                        val f = File(text.toString())
                        validationStatus = if (inputMode == InputMode.FILE)
                            EasyArchive.inflaters.validateArchive(f)
                        else
                            EasyArchive.inflaters.validateDestination(f)
                    }
                }
            }
            find<ImageButton>(R.id.browse).setOnClickListener { chooseFile() }
            // If debugging, save some time and put default values in the input
            whenDebug {
                filename.setText("/mnt/sdcard/sample" + if (inputMode == InputMode.FILE) ".zip" else "")
            }
        } else {
            // Developer mode preview
            find<TextView>(R.id.status).setText(R.string.vs_ready)
            find<ImageView>(R.id.status_icon).setImageResource(R.drawable.ic_severity_fine)
        }
    }

    /** Returns true if the ValidationStatus' severity is anything but `SEVERE` */
    fun valid() = validationStatus != null && validationStatus!!.severity != Severity.SEVERE

    /** Gets a File object whose path is the input */
    fun file() = File(filename.text.toString())

    fun chooseFile() {
        val fm = (context as AppCompatActivity).supportFragmentManager
        val aux = object : Fragment() {
            override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
                if (requestCode == CODE_FILE && resultCode == Activity.RESULT_OK) {
                    filename.setText(data!!.data.path)
                }
            }
        }
        fm.beginTransaction().add(aux, "FRAGMENT_TAG").commit()
        fm.executePendingTransactions()

        val intent = Intent(context, ArchivePickerActivity::class.java)
        intent.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)
        intent.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, true)
        intent.putExtra(FilePickerActivity.EXTRA_MODE,
                if (inputMode == FileInputView.InputMode.FILE) FilePickerActivity.MODE_FILE else FilePickerActivity.MODE_DIR)
        intent.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().absolutePath)
        aux.startActivityForResult(intent, CODE_FILE)
    }

    /** Gets a `R.string` resource ID for the given resource identifier */
    private fun statusFor(s: ValidationStatus): Int =
            context.resources.getIdentifier("vs_${s.name.toLowerCase()}", "string", "net.dean.easyarchive")

    /** Gets a `R.drawable` resource ID for the given Severity */
    private fun iconFor(s: Severity): Int =
            if (s == Severity.SEVERE) R.drawable.ic_severity_severe else R.drawable.ic_severity_fine

    private fun reset() {

    }

    /** How [FileInputView] can validate user input */
    enum class InputMode {
        FILE,
        DIRECTORY
    }
}



