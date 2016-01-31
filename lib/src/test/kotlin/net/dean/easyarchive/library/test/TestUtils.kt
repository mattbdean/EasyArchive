package net.dean.easyarchive.library.test

import net.dean.easyarchive.library.Inflater
import net.dean.easyarchive.library.test.TestUtils.md5
import java.io.File
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import java.io.FileNotFoundException

public fun resource(path: String): File = File(File(Inflater::class.java.getResource("/").toURI()).parentFile.parentFile, "resources/test/$path")
public fun sample(extension: String): File {
    val f = resource("sample.$extension")
    if (!f.exists())
        throw FileNotFoundException("${f.absolutePath} does not exist. Did you run mk.sh?")
    return f
}
public val tempDir: File = File(System.getProperty("java.io.tmpdir"), "EasyArchive")
public fun tempDir(extension: String): File = File(tempDir, "out/$extension")
public fun md5Comp(original: File, new: File) {
    assertThat("MD5s did not match", md5(new), `is`(md5(original)))
}