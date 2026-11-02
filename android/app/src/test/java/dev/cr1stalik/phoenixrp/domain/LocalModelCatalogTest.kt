package dev.cr1stalik.phoenixrp.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalModelCatalogTest {
    @Test
    fun recommendsBalancedPresetForEightGbPhysicalRam() {
        val recommendation = LocalModelCatalog.recommend(
            totalPhysicalRamBytes = 8_000_000_000L,
            freeStorageBytes = 40_000_000_000L
        )
        assertEquals("qwen3-1.7b-q4km", recommendation.id)
    }

    @Test
    fun fallsBackToLiteWhenStorageIsLimited() {
        val recommendation = LocalModelCatalog.recommend(
            totalPhysicalRamBytes = 8_000_000_000L,
            freeStorageBytes = 1_500_000_000L
        )
        assertEquals("qwen3-0.6b-q4km", recommendation.id)
    }

    @Test
    fun powerPresetRequiresTwelveGbPhysicalRam() {
        val recommendation = LocalModelCatalog.recommend(
            totalPhysicalRamBytes = 8_000_000_000L,
            freeStorageBytes = 80_000_000_000L
        )
        assertTrue(recommendation.id != "qwen3-4b-q4km")
    }
}
