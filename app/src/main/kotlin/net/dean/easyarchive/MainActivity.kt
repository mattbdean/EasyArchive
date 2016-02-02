package net.dean.easyarchive

import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.pawegio.kandroid.d
import com.pawegio.kandroid.find
import net.dean.easyarchive.library.ArchiveEvent
import net.dean.easyarchive.library.ArchiveEventHandler
import net.dean.easyarchive.library.InflaterAggregation
import java.io.File

public class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun unarchive(view: View) {
        InflateTask().execute(find<FileInputView>(R.id.input_file).file(),
                find<FileInputView>(R.id.input_directory).file())
    }
}

public class InflateTask : AsyncTask<File, ArchiveEvent, Unit>() {
    override fun doInBackground(vararg params: File?) {
        val inflaters = InflaterAggregation()
        inflaters.eventHandler = object: ArchiveEventHandler {
            override fun handle(e: ArchiveEvent) {
                onProgressUpdate(e)
            }
        }
        if (params.size != 2)
            throw IllegalArgumentException("Something other than a file and a directory was given")
        val file = params[0]!!
        val directory = params[1]!!
        inflaters.inflate(file, directory)
    }

    override fun onProgressUpdate(vararg values: ArchiveEvent?) {
        d(values[0].toString())
    }
}
