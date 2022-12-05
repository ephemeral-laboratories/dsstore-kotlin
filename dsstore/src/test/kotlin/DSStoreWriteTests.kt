
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFailure
import assertk.assertions.isInstanceOf
import org.junit.jupiter.api.io.TempDir
import types.IntPoint
import util.FileMode
import java.nio.file.Path
import kotlin.test.Test

class DSStoreWriteTests {
    @TempDir
    lateinit var temp: Path

    @Test
    fun `can write trivial file in order`() {
        val file = temp.resolve("my.DS_Store")
        DSStore.open(file, FileMode.READ_WRITE).use { store ->
            store.insertOrReplace(DSStoreRecord("bam", DSStoreProperties.IconLocation, IntPoint(104, 116)))
            store.insertOrReplace(DSStoreRecord("bar", DSStoreProperties.IconLocation, IntPoint(256, 235)))
            store.insertOrReplace(DSStoreRecord("baz", DSStoreProperties.IconLocation, IntPoint(454, 124)))
        }
        commonTrivialFileAsserts(file)
    }

    @Test
    fun `can write file out of order`() {
        val file = temp.resolve("my.DS_Store")
        DSStore.open(file, FileMode.READ_WRITE).use { store ->
            store.insertOrReplace(DSStoreRecord("baz", DSStoreProperties.IconLocation, IntPoint(454, 124)))
            store.insertOrReplace(DSStoreRecord("bar", DSStoreProperties.IconLocation, IntPoint(256, 235)))
            store.insertOrReplace(DSStoreRecord("bam", DSStoreProperties.IconLocation, IntPoint(104, 116)))
        }
        commonTrivialFileAsserts(file)
    }

    private fun commonTrivialFileAsserts(file: Path) {
        // Because we can't rely on the behaviour matching wherever trivial.DS_Store came from,
        // the best we can do is look for the same records.
        DSStore.open(file).use { store ->
            assertThat(store["bam", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(104, 116))
            assertThat(store["bar", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(256, 235))
            assertThat(store["baz", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(454, 124))
        }
    }

    @Test
    fun `can overwrite existing record`() {
        val file = temp.resolve("my.DS_Store")
        DSStore.open(file, FileMode.READ_WRITE).use { store ->
            store.insertOrReplace(DSStoreRecord("baz", DSStoreProperties.IconLocation, IntPoint(0, 0)))
            store.insertOrReplace(DSStoreRecord("baz", DSStoreProperties.IconLocation, IntPoint(454, 124)))
        }
        DSStore.open(file).use { store ->
            assertThat(store["baz", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(454, 124))
        }
    }

    @Test
    fun `cannot write more than a page size of records`() {
        val file = temp.resolve("my.DS_Store")
        DSStore.open(file, FileMode.READ_WRITE).use { store ->
            // Just guarding that this will throw instead of silently writing a too-large block
            assertThat {
                (1..100).forEach { n ->
                    store.insertOrReplace(DSStoreRecord("file$n", DSStoreProperties.IconLocation, IntPoint(0, 0)))
                }
            }.isFailure().isInstanceOf(NotImplementedError::class)
        }
    }
}
