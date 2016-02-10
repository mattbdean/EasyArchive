package net.dean.easyarchive

import com.nononsenseapps.filepicker.AbstractFilePickerActivity
import com.nononsenseapps.filepicker.AbstractFilePickerFragment
import com.nononsenseapps.filepicker.FilePickerFragment
import java.io.File

class ArchivePickerActivity : AbstractFilePickerActivity<File>() {
    override fun getFragment(startPath: String?, mode: Int, allowMultiple: Boolean, allowCreateDir: Boolean): AbstractFilePickerFragment<File>? {
        val picker = ArchivePickerFragment()
        picker.setArgs(startPath, mode, allowMultiple, allowCreateDir)
        return picker
    }
}

class ArchivePickerFragment : FilePickerFragment() {
    override fun isItemVisible(file: File): Boolean {
        var visible = super.isItemVisible(file)
        if (visible && !isDir(file) && (mode == MODE_FILE || mode == MODE_FILE_AND_DIR)) {
            visible = EasyArchive.inflaters.canOperateOn(file)
        }
        return visible
    }
}

