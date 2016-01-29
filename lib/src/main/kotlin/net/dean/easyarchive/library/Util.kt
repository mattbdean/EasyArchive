package net.dean.easyarchive.library

import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.compressors.CompressorStreamFactory
import java.io.*

/**
 * Collection of utility constants/methods
 */
public object InflationUtils {
    /** Most efficient size in bytes for a buffer. See [here](http://stackoverflow.com/a/11221907/1275092) for more. */
    public const val BUFFER_SIZE = 32768

    /** Creates a new buffered file output stream */
    @JvmStatic fun newOutputStream(f: File): OutputStream = BufferedOutputStream(f.outputStream(), BUFFER_SIZE)

    /** Creates a new buffered file input stream */
    @JvmStatic fun newInputStream(f: File): InputStream = BufferedInputStream(f.inputStream())

    /**
     * Attempts to create a directory at the given location. More or less equivalent to `mkdir -p $dir`
     * @throws IOException If the directory could not be created
     */
    @Throws(IOException::class)
    @JvmStatic fun mkdirs(dir: File) {
        if (!dir.isDirectory && !dir.mkdirs())
            throw IOException("Could not make directory ${dir.absolutePath}")
    }

    /** Default CompressorStreamFactory */
    @JvmStatic val compressorStreamFactory: CompressorStreamFactory = CompressorStreamFactory()

    /** Default ArchiveStreamFactory */
    @JvmStatic val archiveStreamFactory: ArchiveStreamFactory = ArchiveStreamFactory()
}
