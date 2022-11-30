
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import types.IntPoint
import java.nio.file.Path
import kotlin.test.Test

class DSStoreReadTests {
    @Test
    fun testCanWalkTrivialFile() {
        DSStore.open(Path.of("src/test/resources/trivial.DS_Store")).use { store ->
            val records = store.walk().toList()
            assertThat(records[0].filename).isEqualTo("bam")
            assertThat(records[1].filename).isEqualTo("bar")
            assertThat(records[2].filename).isEqualTo("baz")
        }
    }

    @Test
    fun testCanFindRecordsInTrivialFile() {
        DSStore.open(Path.of("src/test/resources/trivial.DS_Store")).use { store ->
            assertThat(store["bam", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(104, 116))
            assertThat(store["bar", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(256, 235))
            assertThat(store["baz", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(454, 124))
        }
    }

    @Test
    fun testCanWalkLargerFile() {
        DSStore.open(Path.of("src/test/resources/large.DS_Store")).use { store ->
            val recordCount = store.walk().count()
            assertThat(recordCount).isEqualTo(10128)
        }
    }

    @Test
    fun testCanFindRecordsInLargerFile() {
        DSStore.open(Path.of("src/test/resources/large.DS_Store")).use { store ->
            // Examples cunningly selected to be near boundaries to get maximum code coverage
            assertThat(store["2188q7ay6JNcqKwXy1uJH29GD6DrpTNbVkJDKFNSR1ma", DSStoreProperties.PhysicalSize1])
                .isEqualTo(8192L)
            assertThat(store["ZMtXWkRQ9DGXoYPr7BFm4dRQzmmj5kRS1CPeDMPYnWd", DSStoreProperties.PhysicalSize1])
                .isEqualTo(8192L)
            // Not found cases to complete the coverage
            assertThat(store["7RXeGbHV1LMHnSWb9CgbRkSL789mGrTJb6gUULQyz2wD", DSStoreProperties.PhysicalSize])
                .isNull()
            assertThat(store["ZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ", DSStoreProperties.PhysicalSize1])
                .isNull()
        }
    }
}
