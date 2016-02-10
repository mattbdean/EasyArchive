package net.dean.easyarchive.library

import org.apache.commons.compress.compressors.CompressorException
import org.apache.commons.compress.compressors.CompressorStreamFactory
import org.apache.commons.compress.utils.IOUtils
import java.io.File

import net.dean.easyarchive.library.InflationUtils.compressorStreamFactory
import net.dean.easyarchive.library.InflationUtils.newInputStream
import net.dean.easyarchive.library.InflationUtils.newOutputStream


/**
 * Decompressors are Inflaters that decompresses a single stream of data. gzip, bzip2, xz, lzma, Pack200, DEFLATE, and Z
 * are examples of compressor formats.
 */
interface Decompressor : Inflater

/**
 * Generic Decompressor implementation. Fills in some boilerplate that every Decompressor can use.
 *
 * @property ext File extension
 * @property algo A String constant in Commons Compress' `CompressorStreamFactory`. Used to create a
 *                `CompressorInputStream` dynamically.
 */
abstract class AbstractDecompressor(val ext: String, val algo: String) : Decompressor {
    private val size = 1
    /** Handles events for this Decompressor */
    override var eventHandler: ArchiveEventHandler = object: ArchiveEventHandler {
        override fun handle(e: ArchiveEvent) {}
    }

    override fun inflate(f: File, dest: File): List<File> {
        log(ArchiveEvent.start(f, size))
        val input = try {
            compressorStreamFactory.createCompressorInputStream(algo, newInputStream(f))
        } catch (e: CompressorException) {
            throw IllegalArgumentException("Invalid format: $algo", e)
        }
        val basename = getNameFor(f)
        val outFile = File(dest, basename)
        val output = newOutputStream(outFile)

        IOUtils.copy(input, output)
        log(ArchiveEvent.inflate(outFile, size, size))

        input.close()
        output.close()

        log(ArchiveEvent.done(outFile))

        return listOf(outFile)
    }

    override fun count(f: File): Int = size
    override fun canOperateOn(f: File): Boolean = f.name.endsWith('.' + ext, ignoreCase = true)
    override fun log(e: ArchiveEvent) {
        eventHandler.handle(e)
    }

    /**
     * Gets the name for the new file after the given file has been decompressed. By default removes the last extension.
     * For example, `archive.tar.gz` would result in `archive.tar`.
     */
    open fun getNameFor(f: File): String = f.name.substring(0..f.name.lastIndexOf('.') - 1)
}

/** Decompresses bz2 files */
class Bzip2Decompressor : AbstractDecompressor("bz2", CompressorStreamFactory.BZIP2)
/** Decompresses gz files */
class GzipDecompressor : AbstractDecompressor("gz", CompressorStreamFactory.GZIP)
/** Decompresses lzma files */
class LzmaDecompressor : AbstractDecompressor("lzma", CompressorStreamFactory.LZMA)
/** Decompresses pack files into jar files */
class Pack200Decompressor : AbstractDecompressor("pack", CompressorStreamFactory.PACK200) {
    override fun getNameFor(f: File): String = super.getNameFor(f) + ".jar"
}

/** Decompresses xz files */
class XzDecompressor : AbstractDecompressor("xz", CompressorStreamFactory.XZ)

