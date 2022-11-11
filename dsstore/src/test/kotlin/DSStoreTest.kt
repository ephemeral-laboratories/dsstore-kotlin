
import assertk.assertThat
import assertk.assertions.isEqualTo
import types.IntPoint
import java.nio.file.Path
import kotlin.test.Test

class DSStoreTest {
    @Test
    fun testCanWalkFileWrittenByMacOS() {
        DSStore.open(Path.of("src/test/resources/Test_DS_Store")).use { store ->
            val records = store.walk().toList()
            assertThat(records[0].filename).isEqualTo("bam")
            assertThat(records[1].filename).isEqualTo("bar")
            assertThat(records[2].filename).isEqualTo("baz")
        }
    }

    @Test
    fun testCanFindRecordsInFileWrittenByMacOS() {
        DSStore.open(Path.of("src/test/resources/Test_DS_Store")).use { store ->
            assertThat(store["bam"][DSStoreProperties.IconLocation]).isEqualTo(IntPoint(104, 116))
            assertThat(store["bar"][DSStoreProperties.IconLocation]).isEqualTo(IntPoint(256, 235))
            assertThat(store["baz"][DSStoreProperties.IconLocation]).isEqualTo(IntPoint(454, 124))
        }
    }
}
