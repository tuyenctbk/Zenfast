package com.example

import com.example.data.model.BreakFastCategory
import com.example.data.repository.KitchenRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenKitchenUnitTest {

    private val repository = KitchenRepository()

    @Test
    fun testEmptyBarcodeReturnsFailure() = runTest {
        val result = repository.analyzeBarcode("   ")
        assertTrue(result.isFailure)
    }

    @Test
    fun testBreakFastCategoriesDefined() {
        val categories = BreakFastCategory.entries
        assertTrue(categories.contains(BreakFastCategory.HIGH_PROTEIN))
        assertTrue(categories.contains(BreakFastCategory.GUT_FRIENDLY))
        assertTrue(categories.contains(BreakFastCategory.KETO_GENTLE))
        assertTrue(categories.contains(BreakFastCategory.LIGHT_STARTERS))
    }
}
