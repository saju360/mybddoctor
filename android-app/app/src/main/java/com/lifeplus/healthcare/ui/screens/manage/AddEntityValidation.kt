package com.lifeplus.healthcare.ui.screens.manage

data class EntityFormValidationResult(
    val nameError: String? = null,
    val districtError: String? = null,
    val shouldSubmit: Boolean = nameError == null && districtError == null
)

fun validateEntityForm(name: String, district: String, placeholder: String): EntityFormValidationResult {
    val nameError = if (name.isBlank()) "Name is required" else null
    val districtError = if (district == placeholder) "Please select a district" else null
    return EntityFormValidationResult(
        nameError = nameError,
        districtError = districtError,
        shouldSubmit = nameError == null && districtError == null
    )
}
