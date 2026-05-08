package com.lifeplus.healthcare.ui.screens.auth

import io.kotest.property.Arb
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ForgotPasswordValidationPropertyTest {

    @Test
    fun passwordMismatchBlocksSubmissionAndProducesError() = runTest {
        checkAll(iterations = 100, Arb.string(), Arb.string()) { p1, p2 ->
            if (p1 == p2) return@checkAll
            val result = validatePasswordMatch(p1, p2)
            assertNotNull(result.error)
            assertFalse(result.shouldSubmit)
        }
    }

    @Test
    fun resendOtpEnabledStateDependsOnCooldown() = runTest {
        checkAll(iterations = 100, Arb.int(1..60)) { cooldown ->
            assertFalse(isResendEnabled(cooldown))
        }
        assertTrue(isResendEnabled(0))
    }
}
