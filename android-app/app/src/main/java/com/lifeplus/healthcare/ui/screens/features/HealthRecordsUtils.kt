package com.lifeplus.healthcare.ui.screens.features

import com.lifeplus.healthcare.data.model.HealthRecord

fun filterRecords(records: List<HealthRecord>, filter: String): List<HealthRecord> {
    if (filter == "All") return records
    return records.filter { it.type == filter }
}
