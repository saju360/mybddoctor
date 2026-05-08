package com.lifeplus.healthcare.ui.screens.manage

import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Test

class AddEntityValidationPropertyTest {

    @Test
    fun entityFormValidationBlocksSubmissionOnInvalidInput() = runTest {
        checkAll(iterations = 100, Arb.string(), Arb.string()) { blankName, district ->
            if (blankName.isNotBlank()) return@checkAll
            val result = validateEntityForm(blankName, district, "Select District")
            assertNotNull(result.nameError)
            assertFalse(result.shouldSubmit)
        }

        checkAll(iterations = 100, Arb.string()) { validName ->
            if (validName.isBlank()) return@checkAll
            val result = validateEntityForm(validName, "Select District", "Select District")
            assertNotNull(result.districtError)
            assertFalse(result.shouldSubmit)
        }
    }
}
