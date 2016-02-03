package net.dean.easyarchive.library

import java.io.File
import java.util.*

/**
 * This class is provided for ease-of-use and helps with multi-layered files.
 *
 * In this library, a "layer" is defined as a compounded level of compression or archiving. For example, `archive.tar`
 * has one layer, while `archive.tar.gz` has two. The term "inflating" is described as the removal of a layer. When
 * `archive.tar.gz` is inflated, it results in `archive.tar`. When `archive.tar` is inflated, it results in the contents
 * of the archive.
 *
 * This class has two primary purposes, validation and inflation. Before attempting to inflate a file, it is highly
 * recommended that both the file and destination are validated. This can be done with [validate]. This method will
 * check for common errors, such as the archive not existing or inadequate filesystem permissions. To inflate a file,
 * use [inflate] or [inflateSingle].
 */
public class InflaterAggregation {
    /** A list of all the Inflaters */
    public val inflaters: List<Inflater> = listOf(
            // Unarchivers
            ZipUnarchiver(),
            JarUnarchiver(),
            TarUnarchiver(),
            SevenZUnarchiver(),
            RarUnarchiver(),
            // Decompressors
            Bzip2Decompressor(),
            GzipDecompressor(),
            LzmaDecompressor(),
            Pack200Decompressor(),
            XzDecompressor()
    )

    /** Handles all ArchiveEvents. When set, sets each Inflater's event handler in [inflaters] */
    public var eventHandler: ArchiveEventHandler = DefaultArchiveEventHandler()

    init {
        for (inf in inflaters)
            inf.eventHandler = object: ArchiveEventHandler {
                override fun handle(e: ArchiveEvent) {
                    eventHandler.handle(e)
                }
            }
    }

    /**
     * Checks if any Inflater in this aggregation can operate on a given file
     */
    public fun canOperateOn(f: File): Boolean = inflaters.find { it.canOperateOn(f) } != null

    /**
     * Tests a possible archive-destination combination. A value of anything except [ValidationStatus.READY] is an
     * error.
     */
    public fun validate(f: File, dest: File): ValidationStatus {
        val archiveStatus = validateArchive(f)
        return if (archiveStatus == ValidationStatus.READY) validateDestination(dest) else archiveStatus
    }

    /**
     * Checks for any possible error before attempting to extract an archive. A value of anything except
     * [ValidationStatus.READY] is an error.
     */
    public fun validateArchive(f: File): ValidationStatus {
        if (!f.exists()) return ValidationStatus.ARCHIVE_NONEXISTENT
        if (!f.isFile) return ValidationStatus.ARCHIVE_NOT_FILE
        if (!f.canRead()) return ValidationStatus.BAD_ARCHIVE_PERMS
        if (!f.name.contains('.')) return ValidationStatus.NO_FILE_EXTENSION
        return ValidationStatus.READY
    }

    /** Checks for any possible issue with the output directory */
    public fun validateDestination(dest: File): ValidationStatus {
        if (!dest.exists()) {
            val parent: File = dest.absoluteFile.parentFile
            // Can't write to parent, therefore can't create the directory
            if (!parent.canWrite()) return ValidationStatus.DIR_UNCREATABLE
            return ValidationStatus.DEST_NONEXISTENT
        }
        if (!dest.isDirectory) return ValidationStatus.DEST_NOT_DIR
        if (!dest.canWrite()) return ValidationStatus.BAD_DIR_PERMS
        if (dest.listFiles().size > 0) return ValidationStatus.DIR_NOT_EMPTY

        return ValidationStatus.READY
    }

    private fun validateWithException(f: File, dest: File? = null) {
        if (validateArchive(f) != ValidationStatus.READY && (dest == null || validateDestination(dest) != ValidationStatus.READY))
            throw IllegalStateException("Not ready. f=${f.absolutePath}, dest=${dest?.absolutePath}, status=${validate(f, dest!!)}")
    }

    /**
     * Inflates the given file to the highest extent. For example, calling this method with an archive called
     * "archive.tar.gz.xz.lzma", would result in the contents of the archive.tar after all the other layers have been
     * removed. First the LZMA file would be decompressed, then the XZ, then the GZ, then finally the Tar file would be
     * unarchived.
     *
     * @param f Archive file
     * @param dest Destination
     *
     * @return A list of files that resulted in the inflation of the bottom-most layer. For example, if the archive
     *         given was "archive.tar.gz", then the returned list would be the contents of archive.tar
     *
     * @throws InflationException If there was a problem inflating the file
     *
     * @see inflateSingle
     */
    @Throws(InflationException::class)
    public fun inflate(f: File, dest: File): List<File> {
        if (!hasInflaterFor(f))
            throw NoApplicableInflaterException(f)
        var archive: File? = f
        var inflated: List<File> = listOf()
        // Files left over by decompressors. For example, when inflating "archive.tar.gz", this method will leave a file
        // named "archive.tar". This file is then unarchived. archive.tar is not desired and will be deleted afterwards.
        var leftovers: MutableList<File> = ArrayList()

        try {
            while (archive != null && hasInflaterFor(archive)) {
                val inflater = getInflaterFor(f)
                when (inflater) {
                    is Decompressor -> {
                        // Inflate the highest layer
                        inflated = inflateSingle(archive, dest)
                        if (inflated.isNotEmpty()) {
                            archive = inflated[0]
                            // We can remove more layers, this file will be a leftover after it is inflated
                            if (hasInflaterFor(archive))
                                leftovers.add(archive)
                        }
                    }
                    is Unarchiver -> {
                        inflated = inflateSingle(archive, dest)
                        // Stop the loop
                        archive = null
                    }
                    else -> throw IllegalStateException("Cannot handle Inflater of type ${inflater.javaClass.name}, " +
                            "subclass of Decompressor or Unarchiver expected.")
                }
            }
        } catch (e: Exception) {
            throw InflationException("Could not inflate file ${f.absolutePath}", e)
        } finally {
            for (leftover in leftovers) {
                eventHandler.handle(ArchiveEvent.delete(leftover))
                leftover.delete()
            }
        }

        return inflated
    }

    /**
     * Inflates a single layer. For example, if the given file was archive.tar.gz.xz.lzma, the resulting file would be
     * archive.tar.gz.xz
     *
     * @throws NoApplicableInflaterException
     */
    @Throws(NoApplicableInflaterException::class)
    public fun inflateSingle(f: File, dest: File): List<File> {
        validateWithException(f, dest)
        return getInflaterFor(f).inflate(f, dest)
    }

    /**
     * Assesses the amount of files in the topmost layer
     *
     * @throws NoApplicableInflaterException
     */
    @Throws(NoApplicableInflaterException::class)
    public fun count(f: File): Int {
        validateWithException(f)
        return getInflaterFor(f).count(f)
    }

    private fun hasInflaterFor(f: File): Boolean {
        for (ex in inflaters) if (ex.canOperateOn(f)) return true
        return false
    }

    @Throws(NoApplicableInflaterException::class)
    private fun getInflaterFor(f: File): Inflater {
        for (ex in inflaters) if (ex.canOperateOn(f)) return ex
        throw NoApplicableInflaterException(f)
    }
}

/**
 * Thrown when attempting to get an [Inflater] for a file for which this library does not know how to handle
 */
public class NoApplicableInflaterException(f: File) : Exception("No applicable extractor for file '${f.absolutePath}'")

/**
 * Thrown when there is an error inflating an archive
 */
public class InflationException(reason: String, cause: Exception? = null): Exception(reason, cause)

/**
 * Reasons why a file or directory does not to seem to be valid. All besides [READY] are error codes.
 */
public enum class ValidationStatus(public val severity: Severity = Severity.SEVERE) {
    /** The archive file does not exist */
    ARCHIVE_NONEXISTENT(),
    /** The given archive file exists but is not a file */
    ARCHIVE_NOT_FILE(),
    /** The active user does not have read permissions to this archive */
    BAD_ARCHIVE_PERMS(),
    /** The archive file has no extension; it cannot be determined which [Inflater] to use */
    NO_FILE_EXTENSION(),

    /** The destination directory does not exist */
    DEST_NONEXISTENT(Severity.TOLERABLE),
    /** The destination is not a directory */
    DEST_NOT_DIR(),
    /** The current user does not write permissions in this directory */
    BAD_DIR_PERMS(),
    /** The destination does not exist and cannot be created due to lack of permission */
    DIR_UNCREATABLE(),
    /** The destination is not empty, files may be overwritten */
    DIR_NOT_EMPTY(Severity.TOLERABLE),
    /** All pre-checks have passed. This does not guarantee that there will be no error extracting the archive. */
    READY(Severity.FINE)
}

public enum class Severity {
    SEVERE,
    TOLERABLE,
    FINE
}
