package dev.cr1stalik.phoenixrp.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ImportFoundationTest {
    @Test fun validGgufHeaderIsAccepted() {
        val file = File.createTempFile("phoenix-model", ".gguf")
        try {
            file.writeBytes(byteArrayOf(0x47, 0x47, 0x55, 0x46, 0x01))
            assertTrue(GgufModelInspector.inspect(file).isGguf)
        } finally { file.delete() }
    }

    @Test fun invalidGgufHeaderIsRejected() {
        val file = File.createTempFile("phoenix-model", ".gguf")
        try {
            file.writeBytes(byteArrayOf(0x00, 0x01, 0x02, 0x03))
            assertFalse(GgufModelInspector.inspect(file).isGguf)
        } finally { file.delete() }
    }

    @Test fun fandomTextNeedsEnoughContentAndKeepsSource() {
        val result = FandomTextImporter.fromPastedText(
            "Lore", "https://example.fandom.com/wiki/Lore",
            "This is a sufficiently long copied article text that contains more than eighty characters for the importer."
        )
        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow().sourceUrl.startsWith("https://"))
    }
}
