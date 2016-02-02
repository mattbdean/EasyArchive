package net.dean.easyarchive.library

import java.io.File

/**
 * Simple event class fired when some action revolving inflation has occured.
 *
 * @property action The action occurring
 * @property file The relevant file. Will be the file that was just extracted/decompressed when [action] is
 *                [ArchiveAction.INFLATE], the file that was deleted when [action] is [ArchiveAction.DELETE], or the
 *                archive being inflated otherwise
 * @property current The number of the file being processed. Will be -1 if [action] is not [ArchiveAction.INFLATE]
 * @property total The total amount of files being processed. Will be -1 if [action] is not [ArchiveAction.INFLATE]
 */
public data class ArchiveEvent private constructor(public val action: ArchiveAction,
                               public val file: File,
                               public val current: Int = -1,
                               public val total: Int = -1) {
    companion object {
        @JvmStatic public fun inflate(f: File, current: Int, total: Int) = ArchiveEvent(ArchiveAction.INFLATE, f, current, total)
        @JvmStatic public fun count(f: File) = ArchiveEvent(ArchiveAction.COUNT, f)
        @JvmStatic public fun delete(f: File) = ArchiveEvent(ArchiveAction.DELETE, f)
        @JvmStatic public fun done(f: File) = ArchiveEvent(ArchiveAction.DONE, f)
    }
}

/** Handles archive events */
public interface ArchiveEventHandler {
    /** Does something with the given event */
    fun handle(e: ArchiveEvent)
}

/** Ignores all events */
public class DefaultArchiveEventHandler : ArchiveEventHandler {
    override fun handle(e: ArchiveEvent) {}
}

/** An enumeration of possible actions to be used in [ArchiveEvent] */
public enum class ArchiveAction {
    /** Represents when a file has been inflated */
    INFLATE,
    /** Represents when a file has been deleted */
    DELETE,
    /** Represents when the amount of files in an archive is being totaled */
    COUNT,
    /** Represents when an archive has finished extracting */
    DONE
}
