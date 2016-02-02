package net.dean.easyarchive.library.test

import net.dean.easyarchive.library.ArchiveAction
import net.dean.easyarchive.library.ArchiveEvent
import net.dean.easyarchive.library.ArchiveEventHandler
import net.dean.easyarchive.library.InflaterAggregation
import org.junit.Test
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.greaterThan

public class InflationTest {

    @Test
    public fun inflateSingle() {
        val dest = tempDir("tar.gz")
        dest.mkdirs()
        val extractors = InflaterAggregation()
        val archive = sample("tar.gz")
        val result = extractors.inflateSingle(archive, dest)
        assertThat(result.size, `is`(1))
        assertThat(result[0].name, `is`("sample.tar"))
    }

    @Test
    public fun inflateMultiple() {
        val dest = tempDir("tar.gz")
        dest.mkdirs()
        val inflaters = InflaterAggregation()
        val archive = sample("tar.gz")
        assertThat(inflaters.inflate(archive, dest).size, greaterThan(1))
    }

    @Test
    public fun testCount() {
        val dest = tempDir("zip")
        dest.mkdirs()
        val inflaters = InflaterAggregation()
        val archive = sample("zip")
        var lastCount = 0
        inflaters.eventHandler = object: ArchiveEventHandler {
            override fun handle(e: ArchiveEvent) {
                if (e.action == ArchiveAction.INFLATE) {
                    assertThat(e.current, greaterThan(lastCount))
                    lastCount = e.current
                }
            }
        }
        inflaters.inflate(archive, dest)
    }
}
