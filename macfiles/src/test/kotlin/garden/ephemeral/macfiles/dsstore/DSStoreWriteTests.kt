package garden.ephemeral.macfiles.dsstore
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import garden.ephemeral.macfiles.dsstore.types.IntPoint
import garden.ephemeral.macfiles.dsstore.util.FileMode
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Files
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
    fun `writing more than a page size of records`() {
        val file = temp.resolve("my.DS_Store")
        DSStore.open(file, FileMode.READ_WRITE).use { store ->
            (1..100).forEach { n ->
                store.insertOrReplace(DSStoreRecord("file$n", DSStoreProperties.IconLocation, IntPoint(n, n)))
            }
        }
        DSStore.open(file).use { store ->
            (1..100).forEach { n ->
                assertThat(store["file$n", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(n, n))
            }
        }
    }

    @Test
    fun `writing more than a page size of records backwards`() {
        val file = temp.resolve("my.DS_Store")
        DSStore.open(file, FileMode.READ_WRITE).use { store ->
            (100 downTo 1).forEach { n ->
                store.insertOrReplace(DSStoreRecord("file$n", DSStoreProperties.IconLocation, IntPoint(n, n)))
            }
        }
        DSStore.open(file).use { store ->
            (1..100).forEach { n ->
                assertThat(store["file$n", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(n, n))
            }
        }
    }

    @Test
    fun `replacing an record which was directly inside a branch`() {
        val file = temp.resolve("my.DS_Store")
        Files.copy(getFilePath("branch-at-file52.DS_Store"), file)
        DSStore.open(file, FileMode.READ_WRITE).use { store ->
            store.insertOrReplace(DSStoreRecord("file52", DSStoreProperties.IconLocation, IntPoint(42, 42)))
        }
        DSStore.open(file).use { store ->
            assertThat(store["file52", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(42, 42))
        }
    }

    @Test
    fun `deleting a record`() {
        val file = temp.resolve("my.DS_Store")
        Files.copy(getFilePath("trivial.DS_Store"), file)
        DSStore.open(file, FileMode.READ_WRITE).use { store ->
            store.delete("bar", DSStoreProperties.IconLocation)
        }
        DSStore.open(file).use { store ->
            assertThat(store["bam", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(104, 116))
            assertThat(store["baz", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(454, 124))
        }
    }

    @Test
    fun `deleting last record`() {
        val file = temp.resolve("my.DS_Store")
        Files.copy(getFilePath("trivial.DS_Store"), file)
        DSStore.open(file, FileMode.READ_WRITE).use { store ->
            store.delete("bar", DSStoreProperties.IconLocation)
            store.delete("bam", DSStoreProperties.IconLocation)
            store.delete("baz", DSStoreProperties.IconLocation)
        }
        DSStore.open(file).use { store ->
            val recordCount = store.walk().count()
            assertThat(recordCount).isEqualTo(0)
        }
    }

    @Test
    fun `deleting a record which was directly inside a branch`() {
        val file = temp.resolve("my.DS_Store")
        Files.copy(getFilePath("branch-at-file52.DS_Store"), file)
        DSStore.open(file, FileMode.READ_WRITE).use { store ->
            store.delete("file52", DSStoreProperties.IconLocation)
        }
        DSStore.open(file).use { store ->
            assertThat(store["file52", DSStoreProperties.IconLocation]).isNull()
            // Entries either side are still good
            assertThat(store["file51", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(51, 51))
            assertThat(store["file53", DSStoreProperties.IconLocation]).isEqualTo(IntPoint(53, 53))
        }
    }
}
