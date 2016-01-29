package net.dean.easyarchive.library.test

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
}
