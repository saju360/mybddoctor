package com.lifeplus.healthcare.ui.screens.features

import com.lifeplus.healthcare.data.model.HealthRecord
import io.kotest.property.Arb
import io.kotest.property.arbitrary.bind
import io.kotest.property.arbitrary.enum
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.long
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HealthRecordsFilterPropertyTest {

    private enum class RecordType { Lab, Prescription, Vaccination, General }

    private val recordArb = Arb.bind(
        Arb.long(1L..10000L),
        Arb.enum<RecordType>(),
        Arb.string(1..20),
        Arb.string(1..10)
    ) { id, type, title, date ->
        HealthRecord(id = id, type = type.name, title = title, date = date)
    }

    @Test
    fun recordTypeFilterReturnsOnlyMatchingRecords() = runTest {
        checkAll(iterations = 100, Arb.list(recordArb, 0..50), Arb.enum<RecordType>()) { records, filter ->
            val filtered = filterRecords(records, filter.name)
            assertTrue(filtered.all { it.type == filter.name })
            assertTrue(filtered.size <= records.size)
        }

        val sample = listOf(
            HealthRecord(id = 1, type = "Lab", title = "A", date = "2024-01-01"),
            HealthRecord(id = 2, type = "General", title = "B", date = "2024-01-02")
        )
        assertEquals(2, filterRecords(sample, "All").size)
    }
}
