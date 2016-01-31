package net.dean.easyarchive.library.test

import net.dean.easyarchive.library.*
import net.dean.easyarchive.library.InflationUtils.mkdirs
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.io.FileNotFoundException
import java.util.*

@RunWith(Parameterized::class)
public class IndividualInflaterTest(public val inflater: Inflater, public val ext: String) {
    companion object {
        @Parameterized.Parameters @JvmStatic
        public fun data(): Collection<Array<Any>> =
                Arrays.asList(
                        arrayOf(ZipUnarchiver(), "zip"),        // [0]
                        arrayOf(JarUnarchiver(), "jar"),        // [1]
                        arrayOf(TarUnarchiver(), "tar"),        // [2]
                        arrayOf(SevenZUnarchiver(), "7z"),      // [3]
                        arrayOf(RarUnarchiver(), "rar"),        // [4]
                        arrayOf(Bzip2Decompressor(), "bz2"),    // [5]
                        arrayOf(GzipDecompressor(), "gz"),      // [6]
                        arrayOf(LzmaDecompressor(), "lzma"),    // [7]
                        arrayOf(Pack200Decompressor(), "pack"), // [8]
                        arrayOf(XzDecompressor(), "xz")         // [9]
                )
    }

    @Test public fun test() {
        testInflater(inflater, ext)
    }

    private fun testInflater(inflater: Inflater, ext: String) {
        try {
            val archive = sampleFile(inflater)
            val dest = tempDir(ext)
            assertThat(inflater.canOperateOn(archive), `is`(true))
            mkdirs(dest)
            val files = inflater.inflate(archive, dest)
            assertThat(inflater.count(archive), `is`(files.size))
            files.forEach {
                // inflate() only returns files, not directories
                assert(it.isFile)
                when (inflater) {
                    is Unarchiver -> {
                        // Ignore META-INF files generated in jar files
                        if (!(archive.extension == "jar" && it.parentFile.name == "META-INF"))
                            md5Comp(resource("sources/${it.name}"), it)
                    }
                    is Pack200Decompressor -> {
                        // The files are the same but the MD5s do not match up because of the extra info pack200 adds
                        // Rely on JarUnarchiver test
                    }
                    // Decompressor
                    else -> md5Comp(resource("sources/pcgpe10.txt"), it)
                }
            }
        } catch (e: FileNotFoundException) {
            println("Skipping test with inflater ${inflater.javaClass.name} because its sample file could not be found")
        }
    }

    fun sampleFile(inflater: Inflater): File {
        return sample(when (inflater) {
            is Pack200Decompressor -> "pack"
            is Decompressor -> "txt.$ext"
            else -> ext
        })
    }
}