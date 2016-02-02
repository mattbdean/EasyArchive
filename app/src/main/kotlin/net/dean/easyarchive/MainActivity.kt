package net.dean.easyarchive

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.pawegio.kandroid.d
import com.pawegio.kandroid.find
import net.dean.easyarchive.library.ArchiveEvent
import net.dean.easyarchive.library.ArchiveEventHandler

public class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun unarchive(view: View) {
        EasyArchive.inflaters.eventHandler = object: ArchiveEventHandler {
            override fun handle(e: ArchiveEvent) {
                d(e.toString())
            }
        }
        EasyArchive.inflaters.inflate(
                find<FileInputView>(R.id.input_file).file(),
                find<FileInputView>(R.id.input_directory).file())
    }
}
