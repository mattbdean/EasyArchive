package net.dean.easyarchive.library

import java.io.*

/**
 * An Inflater takes a file and expands it, whether that be by decompressing it, unarchiving it, or both. Inflaters do
 * no pre-inflate validation. For this reason it is recommended to use [InflaterAggregation].
 */
public interface Inflater {
    /** Checks to see if this inflater can inflate the given file based on its name */
    fun canOperateOn(f: File): Boolean

    /** Handles the events that come out of this Inflater */
    public var eventHandler: ArchiveEventHandler

    /**
     * Inflates a given file to the destination folder
     *
     * @param f Archive file
     * @param dest Destination folder
     * @return A list of inflated files. Does not include directories.
     *
     * @throws InflationException If there was a problem inflating the archive
     */
    @Throws(InflationException::class)
    fun inflate(f: File, dest: File): List<File>

    /** Gets the amount of files that will be inflated when [inflate] is called. Does not include directories. */
    fun count(f: File): Int

    /** Logs an ArchiveEvent */
    fun log(e: ArchiveEvent)
}

