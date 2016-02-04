package net.dean.easyarchive

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import com.pawegio.kandroid.find
import com.pawegio.kandroid.v
import net.dean.easyarchive.library.ArchiveAction
import net.dean.easyarchive.library.ArchiveEvent
import net.dean.easyarchive.library.ArchiveEventHandler
import net.dean.easyarchive.library.InflaterAggregation
import java.io.File

public class MainActivity : AppCompatActivity() {
    private val progress: ProgressIndicatorView by lazy { find<ProgressIndicatorView>(R.id.progress_indicator) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Enable the inflate button if and only if all the inputs are valid
        val dependents = listOf(find<FileInputView>(R.id.input_file), find<FileInputView>(R.id.input_directory))
        val onStatusChanged = {
            var enabled = dependents.filter { !it.valid() }.size == 0
            find<Button>(R.id.inflate).isEnabled = enabled
        }
        dependents.forEach { it.onStatusChanged = onStatusChanged }

        find<Button>(R.id.inflate).setOnClickListener {
            InflateTask(progress).execute(find<FileInputView>(R.id.input_file).file(),
                    find<FileInputView>(R.id.input_directory).file())
        }
    }
}

public class InflateTask(private val progress: ProgressIndicatorView) : AsyncTask<File, ArchiveEvent, File>() {
    override fun doInBackground(vararg params: File?): File {
        val inflaters = InflaterAggregation()
        inflaters.eventHandler = object: ArchiveEventHandler {
            override fun handle(e: ArchiveEvent) {
                publishProgress(e)
            }
        }
        if (params.size != 2)
            throw IllegalArgumentException("Something other than a file and a directory was given")
        val file = params[0]!!
        val directory = params[1]!!
        val files = inflaters.inflate(file, directory)
        return if (files.size == 1) files[0] else directory
    }

    override fun onProgressUpdate(vararg values: ArchiveEvent?) {
        val event = values[0]!!
        v(event.toString())
        if (event.action == ArchiveAction.START) {
            progress.visibility = View.VISIBLE
            progress.total = event.total
        } else if (event.action == ArchiveAction.INFLATE) {
            progress.update(event.file, event.current)
        } else if (event.action == ArchiveAction.DONE) {
            progress.visibility = View.GONE
        }
    }

    override fun onPostExecute(f: File) {
        Snackbar.make(progress, R.string.file_extracted, Snackbar.LENGTH_LONG)
                .setAction(R.string.show_files) { openFile(f) }
                .show()
    }

    private fun openFile(f: File) {
        val path = Uri.fromFile(f)
        val intent = Intent(Intent.ACTION_VIEW, path)
        val chooser = Intent.createChooser(intent, progress.context.resources.getString(R.string.intent_chooser))

        if (intent.resolveActivity(progress.context.packageManager) != null) {
            progress.context.startActivity(chooser)
        } else {
            Snackbar.make(progress, R.string.no_app, Snackbar.LENGTH_SHORT).show()
        }
    }
}
