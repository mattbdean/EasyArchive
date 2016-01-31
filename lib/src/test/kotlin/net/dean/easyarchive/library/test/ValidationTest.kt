package net.dean.easyarchive.library.test

import net.dean.easyarchive.library.InflaterAggregation
import net.dean.easyarchive.library.ValidationStatus
import org.junit.Before
import org.junit.Test
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import java.io.File
import java.util.*
import net.dean.easyarchive.library.ValidationStatus.*

@RunWith(Parameterized::class)
public class ValidationTest(val archive: File, val destination: File, val expectedValidationStatus: ValidationStatus) {
    companion object {
        private val nonexistant = File("nonexistant")
        private val validArchive = resource("sample.zip")
        private val validDest = tempDir
        @Parameterized.Parameters @JvmStatic
        public fun data(): Collection<Array<Any>> =
                Arrays.asList(
                        // arrayOf(archive, destination, expectedValidationStatus)
                        arrayOf(nonexistant, nonexistant, ARCHIVE_NONEXISTENT),
                        arrayOf(validArchive, nonexistant, DEST_NONEXISTENT),
                        arrayOf(validArchive, validArchive, DEST_NOT_DIR),
                        arrayOf(validDest, validDest, ARCHIVE_NOT_FILE),
                        arrayOf(validArchive, File("/"), BAD_DIR_PERMS),
                        // Preferably there would be a test for an unreadable file
                        arrayOf(validArchive, File("/abc"), DIR_UNCREATABLE),
                        arrayOf(resource("sources/empty"), validDest, NO_FILE_EXTENSION)
                )
    }
    var extractors = InflaterAggregation()

    @Before
    public fun setUp() {
        validDest.mkdirs()
    }

    @Test public fun testValidate() {
        assertThat(extractors.validate(archive, destination), `is`(expectedValidationStatus))
    }
}

