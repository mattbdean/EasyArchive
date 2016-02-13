package net.dean.easyarchive.library

import java.io.File

/**
 * Simple event class fired when some action revolving inflation has occured.
 *
 * @property action The action occurring
 * @property file The relevant file. Will be the file that was just extracted/decompressed when [action] is
 *                [ArchiveAction.INFLATE], the file that was deleted when [action] is [ArchiveAction.DELETE], the
 *                directory where the file will be inflated when [action] is [ArchiveAction.START], or the archive being
 *                inflated otherwise
 * @property current The number of the file being processed. Will be -1 if [action] is not [ArchiveAction.INFLATE]
 * @property total The total amount of files being processed. Will be -1 if [action] is not [ArchiveAction.INFLATE] or
 *                 [ArchiveEvent.START].
 */
data class ArchiveEvent private constructor(val action: ArchiveAction,
                               val file: File,
                               val current: Int = -1,
                               val total: Int = -1) {
    companion object {
        @JvmStatic fun inflate(f: File, current: Int, total: Int) = ArchiveEvent(ArchiveAction.INFLATE, f, current, total)
        @JvmStatic fun count(f: File) = ArchiveEvent(ArchiveAction.COUNT, f)
        @JvmStatic fun delete(f: File) = ArchiveEvent(ArchiveAction.DELETE, f)
        @JvmStatic fun done(f: File) = ArchiveEvent(ArchiveAction.DONE, f)
        @JvmStatic fun start(dir: File, total: Int) = ArchiveEvent(ArchiveAction.START, dir, total = total)
    }
}

/** Handles archive events */
interface ArchiveEventHandler {
    /** Does something with the given event */
    fun handle(e: ArchiveEvent)
}

/** Ignores all events */
class DefaultArchiveEventHandler : ArchiveEventHandler {
    override fun handle(e: ArchiveEvent) {}
}

/** An enumeration of possible actions to be used in [ArchiveEvent] */
enum class ArchiveAction {
    /** Represents when an Inflator has started to operate. Sent after COUNT. */
    START,
    /** Represents when a file has been inflated */
    INFLATE,
    /** Represents when a file has been deleted */
    DELETE,
    /** Represents when the amount of files in an archive is being totaled */
    COUNT,
    /** Represents when an archive has finished extracting */
    DONE
}
