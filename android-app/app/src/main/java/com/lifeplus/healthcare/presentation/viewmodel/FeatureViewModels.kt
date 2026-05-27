package com.lifeplus.healthcare.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeplus.healthcare.data.model.*
import com.lifeplus.healthcare.data.repository.*
import com.lifeplus.healthcare.data.util.Resource
import com.lifeplus.healthcare.data.util.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.lifeplus.healthcare.data.local.SessionDataStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

// ── Generic list UI state ──────────────────────────────────────────────────────
data class ListUiState<T>(
    val isLoading: Boolean = false,
    val data: List<T> = emptyList(),
    val error: String? = null
)

data class ActionUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

private fun normalizeDistrict(value: String?): String {
    return value?.trim()?.lowercase()?.replace(" city", "") ?: ""
}

private fun <T> sortByCurrentDistrict(
    items: List<T>,
    currentDistrict: String?,
    districtOf: (T) -> String?,
    idOf: (T) -> Long
): List<T> {
    val normalizedCurrent = normalizeDistrict(currentDistrict)
    if (normalizedCurrent.isBlank()) {
        return items.sortedByDescending(idOf)
    }

    val local = items
        .filter { normalizeDistrict(districtOf(it)) == normalizedCurrent }
        .sortedBy(idOf)
    val others = items
        .filter { normalizeDistrict(districtOf(it)) != normalizedCurrent }
        .sortedByDescending(idOf)
    return local + others
}

// ── Hospital ViewModel ─────────────────────────────────────────────────────────
@HiltViewModel
class HospitalViewModel @Inject constructor(
    private val repo: HospitalRepository,
    private val locationHelper: LocationHelper,
    private val session: SessionDataStore
) : ViewModel() {
    private val _state = MutableStateFlow(ListUiState<Hospital>())
    val state: StateFlow<ListUiState<Hospital>> = _state

    private val _nearbyDistrict = MutableStateFlow<String?>(null)
    val nearbyDistrict: StateFlow<String?> = _nearbyDistrict

    private val _action = MutableStateFlow(ActionUiState())
    val action: StateFlow<ActionUiState> = _action

    init { detectNearbyAndLoad() }

    fun detectNearbyAndLoad() = viewModelScope.launch {
        _state.value = ListUiState(isLoading = true)
        
        // Try GPS first
        val location = locationHelper.getCurrentLocation()
        var district = if (location != null) locationHelper.getDistrictFromLocation(location) else null
        
        // Fallback to user's saved district
        if (district == null) {
            district = session.district.stateIn(viewModelScope).value
        }

        if (district != null) {
            _nearbyDistrict.value = district
        }
        loadAll()
    }

    fun loadAll() = viewModelScope.launch {
        _state.value = ListUiState<Hospital>(isLoading = true)
        when (val r = repo.getAll()) {
            is Resource.Success -> _state.value = ListUiState(
                data = sortByCurrentDistrict(r.data, _nearbyDistrict.value, { it.district }, { it.id })
            )
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun search(district: String) = viewModelScope.launch {
        _state.value = ListUiState<Hospital>(isLoading = true)
        when (val r = repo.search(district)) {
            is Resource.Success -> _state.value = ListUiState(
                data = sortByCurrentDistrict(r.data, _nearbyDistrict.value, { it.district }, { it.id })
            )
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun loadMy() = viewModelScope.launch {
        _state.value = ListUiState<Hospital>(isLoading = true)
        when (val r = repo.getMy()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun create(hospital: Hospital) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.create(hospital)) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                loadMy()
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            Resource.Loading -> _action.value = ActionUiState(isLoading = true)
        }
    }

    fun update(id: Long, hospital: Hospital) = viewModelScope.launch {
        repo.update(id, hospital)
        loadMy()
    }

    fun delete(id: Long) = viewModelScope.launch {
        repo.delete(id)
        loadMy()
    }
}

// ── Clinic ViewModel ───────────────────────────────────────────────────────────
@HiltViewModel
class ClinicViewModel @Inject constructor(private val repo: ClinicRepository) : ViewModel() {
    private val _state = MutableStateFlow(ListUiState<Clinic>())
    val state: StateFlow<ListUiState<Clinic>> = _state

    private val _action = MutableStateFlow(ActionUiState())
    val action: StateFlow<ActionUiState> = _action

    init { loadAll() }

    fun loadAll() = viewModelScope.launch {
        _state.value = ListUiState<Clinic>(isLoading = true)
        when (val r = repo.getAll()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun loadMy() = viewModelScope.launch {
        _state.value = ListUiState<Clinic>(isLoading = true)
        when (val r = repo.getMy()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun create(clinic: Clinic) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.create(clinic)) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                loadMy()
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            Resource.Loading -> _action.value = ActionUiState(isLoading = true)
        }
    }

    fun update(id: Long, clinic: Clinic) = viewModelScope.launch {
        repo.update(id, clinic)
        loadMy()
    }

    fun delete(id: Long) = viewModelScope.launch {
        repo.delete(id)
        loadMy()
    }
}

// ── Doctor ViewModel ───────────────────────────────────────────────────────────
@HiltViewModel
class DoctorViewModel @Inject constructor(
    private val repo: DoctorRepository,
    private val locationHelper: LocationHelper,
    private val session: SessionDataStore
) : ViewModel() {
    private val _state = MutableStateFlow(ListUiState<Doctor>())
    val state: StateFlow<ListUiState<Doctor>> = _state

    private val _nearbyDistrict = MutableStateFlow<String?>(null)
    val nearbyDistrict: StateFlow<String?> = _nearbyDistrict

    private val _action = MutableStateFlow(ActionUiState())
    val action: StateFlow<ActionUiState> = _action

    init { detectNearbyAndLoad() }

    fun detectNearbyAndLoad() = viewModelScope.launch {
        _state.value = ListUiState(isLoading = true)
        val location = locationHelper.getCurrentLocation()
        var district = if (location != null) locationHelper.getDistrictFromLocation(location) else null
        
        if (district == null) {
            district = session.district.stateIn(viewModelScope).value
        }

        if (district != null) {
            _nearbyDistrict.value = district
        }
        loadAll()
    }

    fun loadAll() = viewModelScope.launch {
        _state.value = ListUiState<Doctor>(isLoading = true)
        when (val r = repo.getAll()) {
            is Resource.Success -> _state.value = ListUiState(
                data = sortByCurrentDistrict(r.data, _nearbyDistrict.value, { it.district }, { it.id })
            )
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun filter(
        specialty: String? = null,
        hospitalId: Long? = null,
        telemedicine: Boolean? = null,
        district: String? = null
    ) = viewModelScope.launch {
        _state.value = ListUiState<Doctor>(isLoading = true)
        when (val r = repo.search(specialty, hospitalId, telemedicine, district)) {
            is Resource.Success -> _state.value = ListUiState(
                data = sortByCurrentDistrict(r.data, _nearbyDistrict.value, { it.district }, { it.id })
            )
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun loadMy() = viewModelScope.launch {
        _state.value = ListUiState<Doctor>(isLoading = true)
        when (val r = repo.getMy()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun create(doctor: Doctor) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.create(doctor)) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                loadMy()
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            Resource.Loading -> _action.value = ActionUiState(isLoading = true)
        }
    }

    fun update(id: Long, doctor: Doctor) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.update(id, doctor)) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                loadMy()
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            Resource.Loading -> _action.value = ActionUiState(isLoading = true)
        }
    }

    fun delete(id: Long) = viewModelScope.launch {
        repo.delete(id)
        loadMy()
    }
}

// ── Ambulance ViewModel ───────────────────────────────────────────────────────
@HiltViewModel
class AmbulanceViewModel @Inject constructor(
    private val repo: AmbulanceRepository,
    private val locationHelper: LocationHelper,
    private val session: SessionDataStore
) : ViewModel() {
    private val _state = MutableStateFlow(ListUiState<Ambulance>())
    val state: StateFlow<ListUiState<Ambulance>> = _state

    private val _nearbyDistrict = MutableStateFlow<String?>(null)
    val nearbyDistrict: StateFlow<String?> = _nearbyDistrict

    private val _action = MutableStateFlow(ActionUiState())
    val action: StateFlow<ActionUiState> = _action

    init { detectNearbyAndLoad() }

    fun detectNearbyAndLoad() = viewModelScope.launch {
        _state.value = ListUiState(isLoading = true)
        val location = locationHelper.getCurrentLocation()
        var district = if (location != null) locationHelper.getDistrictFromLocation(location) else null
        
        if (district == null) {
            district = session.district.stateIn(viewModelScope).value
        }

        if (district != null) {
            _nearbyDistrict.value = district
        }
        loadAll()
    }

    fun search(district: String? = null) = viewModelScope.launch {
        _state.value = ListUiState<Ambulance>(isLoading = true)
        when (val r = repo.search(district)) {
            is Resource.Success -> _state.value = ListUiState(
                data = sortByCurrentDistrict(r.data, _nearbyDistrict.value, { it.district }, { it.id })
            )
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun loadAll() = viewModelScope.launch {
        _state.value = ListUiState<Ambulance>(isLoading = true)
        when (val r = repo.getAll()) {
            is Resource.Success -> _state.value = ListUiState(
                data = sortByCurrentDistrict(r.data, _nearbyDistrict.value, { it.district }, { it.id })
            )
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun loadMy() = viewModelScope.launch {
        _state.value = ListUiState<Ambulance>(isLoading = true)
        when (val r = repo.getMy()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun create(ambulance: Ambulance) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.create(ambulance)) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                loadMy()
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            Resource.Loading -> _action.value = ActionUiState(isLoading = true)
        }
    }

    fun update(id: Long, ambulance: Ambulance) = viewModelScope.launch {
        repo.update(id, ambulance)
        loadMy()
    }

    fun delete(id: Long) = viewModelScope.launch {
        repo.delete(id)
        loadMy()
    }
}

// ── Pharmacy ViewModel ────────────────────────────────────────────────────────
@HiltViewModel
class PharmacyViewModel @Inject constructor(
    private val repo: PharmacyRepository,
    private val locationHelper: LocationHelper,
    private val session: SessionDataStore
) : ViewModel() {
    private val _state = MutableStateFlow(ListUiState<Pharmacy>())
    val state: StateFlow<ListUiState<Pharmacy>> = _state

    private val _nearbyDistrict = MutableStateFlow<String?>(null)
    val nearbyDistrict: StateFlow<String?> = _nearbyDistrict

    private val _action = MutableStateFlow(ActionUiState())
    val action: StateFlow<ActionUiState> = _action

    init { detectNearbyAndLoad() }

    fun detectNearbyAndLoad() = viewModelScope.launch {
        _state.value = ListUiState(isLoading = true)
        val location = locationHelper.getCurrentLocation()
        var district = if (location != null) locationHelper.getDistrictFromLocation(location) else null
        
        if (district == null) {
            district = session.district.stateIn(viewModelScope).value
        }

        if (district != null) {
            _nearbyDistrict.value = district
        }
        loadAll()
    }

    fun loadAll() = viewModelScope.launch {
        _state.value = ListUiState<Pharmacy>(isLoading = true)
        when (val r = repo.getAll()) {
            is Resource.Success -> _state.value = ListUiState(
                data = sortByCurrentDistrict(r.data, _nearbyDistrict.value, { it.district }, { it.id })
            )
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun search(district: String? = null, open24h: Boolean = false) = viewModelScope.launch {
        _state.value = ListUiState<Pharmacy>(isLoading = true)
        when (val r = repo.search(district, open24h)) {
            is Resource.Success -> _state.value = ListUiState(
                data = sortByCurrentDistrict(r.data, _nearbyDistrict.value, { it.district }, { it.id })
            )
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun loadMy() = viewModelScope.launch {
        _state.value = ListUiState<Pharmacy>(isLoading = true)
        when (val r = repo.getMy()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun create(pharmacy: Pharmacy) = viewModelScope.launch { 
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.create(pharmacy)) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                loadMy()
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            Resource.Loading -> _action.value = ActionUiState(isLoading = true)
        }
    }
    fun update(id: Long, pharmacy: Pharmacy) = viewModelScope.launch { repo.update(id, pharmacy); loadMy() }
    fun delete(id: Long) = viewModelScope.launch { repo.delete(id); loadMy() }
}

// ── BloodBank ViewModel ───────────────────────────────────────────────────────
@HiltViewModel
class BloodBankViewModel @Inject constructor(
    private val repo: BloodBankRepository,
    private val locationHelper: LocationHelper,
    private val session: SessionDataStore
) : ViewModel() {
    private val _state = MutableStateFlow(ListUiState<BloodBank>())
    val state: StateFlow<ListUiState<BloodBank>> = _state

    private val _nearbyDistrict = MutableStateFlow<String?>(null)
    val nearbyDistrict: StateFlow<String?> = _nearbyDistrict

    private val _action = MutableStateFlow(ActionUiState())
    val action: StateFlow<ActionUiState> = _action

    init { detectNearbyAndLoad() }

    fun detectNearbyAndLoad() = viewModelScope.launch {
        _state.value = ListUiState(isLoading = true)
        val location = locationHelper.getCurrentLocation()
        var district = if (location != null) locationHelper.getDistrictFromLocation(location) else null
        
        if (district == null) {
            district = session.district.stateIn(viewModelScope).value
        }

        if (district != null) {
            _nearbyDistrict.value = district
        }
        loadAll()
    }

    fun search(district: String) = viewModelScope.launch {
        _state.value = ListUiState<BloodBank>(isLoading = true)
        when (val r = repo.search(district)) {
            is Resource.Success -> _state.value = ListUiState(
                data = sortByCurrentDistrict(r.data, _nearbyDistrict.value, { it.district }, { it.id })
            )
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun loadAll() = viewModelScope.launch {
        _state.value = ListUiState<BloodBank>(isLoading = true)
        when (val r = repo.getAll()) {
            is Resource.Success -> _state.value = ListUiState(
                data = sortByCurrentDistrict(r.data, _nearbyDistrict.value, { it.district }, { it.id })
            )
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun loadMy() = viewModelScope.launch {
        _state.value = ListUiState<BloodBank>(isLoading = true)
        when (val r = repo.getMy()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun create(bank: BloodBank) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.create(bank)) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                loadMy()
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            Resource.Loading -> _action.value = ActionUiState(isLoading = true)
        }
    }

    fun update(id: Long, bank: BloodBank) = viewModelScope.launch {
        repo.update(id, bank)
        loadMy()
    }

    fun delete(id: Long) = viewModelScope.launch {
        repo.delete(id)
        loadMy()
    }
}

// ── Diagnostic ViewModel ───────────────────────────────────────────────────────
@HiltViewModel
class DiagnosticViewModel @Inject constructor(
    private val repo: DiagnosticRepository,
    private val locationHelper: LocationHelper,
    private val session: SessionDataStore
) : ViewModel() {
    private val _state = MutableStateFlow(ListUiState<DiagnosticCenter>())
    val state: StateFlow<ListUiState<DiagnosticCenter>> = _state

    private val _nearbyDistrict = MutableStateFlow<String?>(null)
    val nearbyDistrict: StateFlow<String?> = _nearbyDistrict

    private val _action = MutableStateFlow(ActionUiState())
    val action: StateFlow<ActionUiState> = _action

    init { detectNearbyAndLoad() }

    fun detectNearbyAndLoad() = viewModelScope.launch {
        _state.value = ListUiState(isLoading = true)
        val location = locationHelper.getCurrentLocation()
        var district = if (location != null) locationHelper.getDistrictFromLocation(location) else null
        
        if (district == null) {
            district = session.district.stateIn(viewModelScope).value
        }

        if (district != null) {
            _nearbyDistrict.value = district
        }
        loadAll()
    }

    fun loadAll() = viewModelScope.launch {
        _state.value = ListUiState<DiagnosticCenter>(isLoading = true)
        when (val r = repo.getAll()) {
            is Resource.Success -> _state.value = ListUiState(
                data = sortByCurrentDistrict(r.data, _nearbyDistrict.value, { it.district }, { it.id })
            )
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun search(district: String? = null, test: String? = null) = viewModelScope.launch {
        _state.value = ListUiState<DiagnosticCenter>(isLoading = true)
        when (val r = repo.search(district, test)) {
            is Resource.Success -> _state.value = ListUiState(
                data = sortByCurrentDistrict(r.data, _nearbyDistrict.value, { it.district }, { it.id })
            )
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun loadMy() = viewModelScope.launch {
        _state.value = ListUiState<DiagnosticCenter>(isLoading = true)
        when (val r = repo.getMy()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun create(diagnostic: DiagnosticCenter) = viewModelScope.launch { 
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.create(diagnostic)) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                loadMy()
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            Resource.Loading -> _action.value = ActionUiState(isLoading = true)
        }
    }
    fun update(id: Long, diagnostic: DiagnosticCenter) = viewModelScope.launch { repo.update(id, diagnostic); loadMy() }
    fun delete(id: Long) = viewModelScope.launch { repo.delete(id); loadMy() }
}

// ── BloodOrg ViewModel ─────────────────────────────────────────────────────────
@HiltViewModel
class BloodOrgViewModel @Inject constructor(
    private val repo: BloodOrgRepository,
    private val locationHelper: LocationHelper,
    private val session: SessionDataStore
) : ViewModel() {
    private val _state = MutableStateFlow(ListUiState<BloodOrganization>())
    val state: StateFlow<ListUiState<BloodOrganization>> = _state

    private val _nearbyDistrict = MutableStateFlow<String?>(null)
    val nearbyDistrict: StateFlow<String?> = _nearbyDistrict

    private val _action = MutableStateFlow(ActionUiState())
    val action: StateFlow<ActionUiState> = _action

    init { detectNearbyAndLoad() }

    fun detectNearbyAndLoad() = viewModelScope.launch {
        _state.value = ListUiState(isLoading = true)
        val location = locationHelper.getCurrentLocation()
        var district = if (location != null) locationHelper.getDistrictFromLocation(location) else null
        
        if (district == null) {
            district = session.district.stateIn(viewModelScope).value
        }

        if (district != null) {
            _nearbyDistrict.value = district
        }
        loadAll()
    }

    fun search(district: String) = viewModelScope.launch {
        _state.value = ListUiState<BloodOrganization>(isLoading = true)
        when (val r = repo.search(district)) {
            is Resource.Success -> _state.value = ListUiState(
                data = sortByCurrentDistrict(r.data, _nearbyDistrict.value, { it.district }, { it.id })
            )
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun loadAll() = viewModelScope.launch {
        _state.value = ListUiState<BloodOrganization>(isLoading = true)
        when (val r = repo.getAll()) {
            is Resource.Success -> _state.value = ListUiState(
                data = sortByCurrentDistrict(r.data, _nearbyDistrict.value, { it.district }, { it.id })
            )
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun loadMy() = viewModelScope.launch {
        _state.value = ListUiState<BloodOrganization>(isLoading = true)
        when (val r = repo.getMy()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun create(org: BloodOrganization) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.create(org)) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                loadMy()
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            Resource.Loading -> _action.value = ActionUiState(isLoading = true)
        }
    }

    fun update(id: Long, org: BloodOrganization) = viewModelScope.launch {
        repo.update(id, org)
        loadMy()
    }

    fun delete(id: Long) = viewModelScope.launch {
        repo.delete(id)
        loadMy()
    }
}

// ── Donor ViewModel ───────────────────────────────────────────────────────────
@HiltViewModel
class DonorViewModel @Inject constructor(
    private val repo: DonorRepository,
    private val orgRepo: BloodOrgRepository,
    private val locationHelper: LocationHelper,
    private val session: SessionDataStore
) : ViewModel() {
    private val _state = MutableStateFlow(ListUiState<Donor>())
    val state: StateFlow<ListUiState<Donor>> = _state

    private val _organizations = MutableStateFlow<List<BloodOrganization>>(emptyList())
    val organizations: StateFlow<List<BloodOrganization>> = _organizations

    private val _nearbyDistrict = MutableStateFlow<String?>(null)
    val nearbyDistrict: StateFlow<String?> = _nearbyDistrict

    private val _action = MutableStateFlow(ActionUiState())
    val action: StateFlow<ActionUiState> = _action

    private val _rewards = MutableStateFlow<Map<String, Any>>(emptyMap())
    val rewards: StateFlow<Map<String, Any>> = _rewards

    private val _currentDonor = MutableStateFlow<Donor?>(null)
    val currentDonor: StateFlow<Donor?> = _currentDonor

    init { 
        detectNearbyAndLoad()
        loadMyProfile()
        loadOrganizations()
    }

    fun loadOrganizations() = viewModelScope.launch {
        when (val r = orgRepo.getAll()) {
            is Resource.Success -> _organizations.value = r.data
            else -> {}
        }
    }

    fun loadMyProfile() = viewModelScope.launch {
        when (val r = repo.myDonors()) {
            is Resource.Success -> _currentDonor.value = r.data.firstOrNull()
            else -> {}
        }
    }

    fun addPoints(points: Int) = viewModelScope.launch {
        _currentDonor.value?.let { donor ->
            val updatedDonor = donor.copy(rewardPoints = (donor.rewardPoints ?: 0) + points)
            repo.update(donor.id, updatedDonor)
            _currentDonor.value = updatedDonor
        }
    }

    fun updateLastDonationDate(date: String) = viewModelScope.launch {
        _currentDonor.value?.let { donor ->
            val updatedDonor = donor.copy(lastDonationDate = date, availableNow = false)
            repo.update(donor.id, updatedDonor)
            _currentDonor.value = updatedDonor
        }
    }

    fun detectNearbyAndLoad() = viewModelScope.launch {
        _state.value = ListUiState(isLoading = true)
        val location = locationHelper.getCurrentLocation()
        var district = if (location != null) locationHelper.getDistrictFromLocation(location) else null
        
        if (district == null) {
            district = session.district.stateIn(viewModelScope).value
        }

        if (district != null) {
            _nearbyDistrict.value = district
        }
        loadAll()
    }

    fun loadAll() = viewModelScope.launch {
        _state.value = ListUiState<Donor>(isLoading = true)
        when (val r = repo.getAll()) {
            is Resource.Success -> _state.value = ListUiState(
                // Only show APPROVED donors in the public list
                data = sortByCurrentDistrict(
                    r.data.filter { it.status == "APPROVED" && it.availableNow },
                    _nearbyDistrict.value, { it.district }, { it.id }
                )
            )
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun loadMy() = viewModelScope.launch {
        _state.value = ListUiState<Donor>(isLoading = true)
        when (val r = repo.myDonors()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun search(district: String? = null, bloodGroup: String? = null) = viewModelScope.launch {
        _state.value = ListUiState<Donor>(isLoading = true)
        when (val r = repo.search(bloodGroup = bloodGroup, district = district)) {
            is Resource.Success -> _state.value = ListUiState(
                // Only show APPROVED donors in the public list
                data = sortByCurrentDistrict(
                    r.data.filter { it.status == "APPROVED" && it.availableNow },
                    _nearbyDistrict.value, { it.district }, { it.id }
                )
            )
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun register(donor: Donor) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        
        // Use repository check if possible, or local check for initial submission
        val canDonate = if (donor.lastDonationDate != null) {
            checkEligibilityLocal(donor.lastDonationDate)
        } else true

        if (!canDonate) {
            _action.value = ActionUiState(error = "You can only donate once every 4 months.")
            return@launch
        }

        when (val r = repo.create(donor)) {
            is Resource.Success -> { _action.value = ActionUiState(isSuccess = true); loadMy() }
            is Resource.Error   -> _action.value = ActionUiState(error = r.message)
            Resource.Loading    -> _action.value = ActionUiState(isLoading = true)
        }
    }

    private fun checkEligibilityLocal(lastDate: String?): Boolean {
        if (lastDate == null || lastDate.isEmpty()) return true
        return try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            val last = sdf.parse(lastDate)
            val now = java.util.Calendar.getInstance().time
            val diff = now.time - (last?.time ?: 0)
            val months = diff / (1000L * 60 * 60 * 24 * 30)
            months >= 4
        } catch (e: Exception) { true }
    }

    fun loadRewards() = viewModelScope.launch {
        when (val r = repo.getRewards()) {
            is Resource.Success -> _rewards.value = r.data
            else -> {}
        }
    }

    private val _eligibility = MutableStateFlow<Map<String, Any>>(emptyMap())
    val eligibility: StateFlow<Map<String, Any>> = _eligibility

    fun checkEligibility(id: Long) = viewModelScope.launch {
        when (val r = repo.checkEligibility(id)) {
            is Resource.Success -> _eligibility.value = r.data
            else -> {}
        }
    }

    fun verifyDonation(requestId: Long) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.verifyDonation(requestId)) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                loadRewards()
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            else -> {}
        }
    }

    fun update(id: Long, donor: Donor) = viewModelScope.launch {
        repo.update(id, donor)
        loadMy()
    }

    fun delete(id: Long) = viewModelScope.launch {
        repo.delete(id)
        loadMy()
    }
}

// ── BloodRequest ViewModel ────────────────────────────────────────────────────
@HiltViewModel
class BloodRequestViewModel @Inject constructor(
    private val repo: BloodRequestRepository,
    private val session: SessionDataStore
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = session.isLoggedIn.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    private val _state = MutableStateFlow(ListUiState<BloodRequest>())
    val state: StateFlow<ListUiState<BloodRequest>> = _state

    private val _action = MutableStateFlow(ActionUiState())
    val action: StateFlow<ActionUiState> = _action

    private val _dashboardRequests = MutableStateFlow<List<BloodRequest>>(emptyList())
    val dashboardRequests: StateFlow<List<BloodRequest>> = _dashboardRequests

    init { loadAll() }

    fun loadAll() = viewModelScope.launch {
        _state.value = ListUiState<BloodRequest>(isLoading = true)
        when (val r = repo.getAll()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun loadDashboard() = viewModelScope.launch {
        when (val r = repo.getDashboardRequests()) {
            is Resource.Success -> _dashboardRequests.value = r.data
            else -> {}
        }
    }

    fun loadForDonor() = viewModelScope.launch {
        _state.value = ListUiState<BloodRequest>(isLoading = true)
        when (val r = repo.getForDonor()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun filter(bloodGroup: String? = null, district: String? = null) = viewModelScope.launch {
        _state.value = ListUiState<BloodRequest>(isLoading = true)
        when (val r = repo.search(bloodGroup, district)) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun post(request: BloodRequest) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.create(request)) {
            is Resource.Success -> { _action.value = ActionUiState(isSuccess = true); loadAll() }
            is Resource.Error   -> _action.value = ActionUiState(error = r.message)
            Resource.Loading    -> _action.value = ActionUiState(isLoading = true)
        }
    }

    fun updateStatus(id: Long, status: String) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.updateStatus(id, status)) {
            is Resource.Success -> { _action.value = ActionUiState(isSuccess = true); loadAll() }
            is Resource.Error   -> _action.value = ActionUiState(error = r.message)
            Resource.Loading    -> _action.value = ActionUiState(isLoading = true)
        }
    }

    fun completeDonation(id: Long, onSuccess: () -> Unit) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        // Change status to PENDING_APPROVAL instead of FULFILLED directly
        when (val r = repo.updateStatus(id, "PENDING_APPROVAL")) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                onSuccess()
                loadAll()
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            else -> {}
        }
    }

    fun delete(id: Long) = viewModelScope.launch {
        repo.delete(id)
        loadAll()
    }
}

// ── Emergency ViewModel ───────────────────────────────────────────────────────
@HiltViewModel
class EmergencyViewModel @Inject constructor(
    private val repo: EmergencyRepository,
    private val session: SessionDataStore
) : ViewModel() {
    val isLoggedIn: StateFlow<Boolean> = session.isLoggedIn.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), false
    )

    private val _state  = MutableStateFlow(ActionUiState())
    val state: StateFlow<ActionUiState> = _state

    private val _history = MutableStateFlow(ListUiState<EmergencyRequest>())
    val history: StateFlow<ListUiState<EmergencyRequest>> = _history

    init { loadHistory() }

    fun loadHistory() = viewModelScope.launch {
        _history.value = ListUiState(isLoading = true)
        when (val r = repo.myRequests()) {
            is Resource.Success -> _history.value = ListUiState(data = r.data)
            is Resource.Error   -> _history.value = ListUiState(error = r.message)
            Resource.Loading    -> Unit
        }
    }

    fun send(name: String, phone: String, district: String, type: String, description: String) =
        viewModelScope.launch {
            if (name.isBlank() || phone.isBlank()) {
                _state.value = ActionUiState(error = "Name and phone are required.")
                return@launch
            }
            _state.value = ActionUiState(isLoading = true)

            val currentUserId = session.userId.firstOrNull()

            val req = EmergencyRequest(
                userId        = currentUserId,
                callerName    = name.trim(),
                contactPhone  = phone.trim(),
                district      = district,
                emergencyType = type,
                description   = description.trim(),
                upazila       = ""
            )
            when (val r = repo.sendEmergency(req)) {
                is Resource.Success -> {
                    _state.value = ActionUiState(isSuccess = true)
                    loadHistory()
                }
                is Resource.Error   -> _state.value = ActionUiState(error = r.message)
                Resource.Loading    -> Unit
            }
        }

    fun clearState() { _state.value = ActionUiState() }
}

// ── Appointment ViewModel ─────────────────────────────────────────────────────
@HiltViewModel
class AppointmentViewModel @Inject constructor(private val repo: AppointmentRepository) : ViewModel() {
    private val _state = MutableStateFlow(ListUiState<Appointment>())
    val state: StateFlow<ListUiState<Appointment>> = _state

    private val _action = MutableStateFlow(ActionUiState())
    val action: StateFlow<ActionUiState> = _action

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = ListUiState(isLoading = true)
        when (val r = repo.myAppointments()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> Unit
        }
    }

    fun book(appointment: Appointment) = viewModelScope.launch {
        if (appointment.doctorId <= 0L) {
            _action.value = ActionUiState(error = "Please select a doctor before booking.")
            return@launch
        }
        if (appointment.doctorName.isBlank()) {
            _action.value = ActionUiState(error = "Doctor name is required.")
            return@launch
        }
        if (appointment.date.isBlank()) {
            _action.value = ActionUiState(error = "Date is required.")
            return@launch
        }
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.book(appointment)) {
            is Resource.Success -> { _action.value = ActionUiState(isSuccess = true); load() }
            is Resource.Error   -> _action.value = ActionUiState(error = r.message)
            Resource.Loading    -> Unit
        }
    }

    fun cancel(id: Long) = viewModelScope.launch {
        when (repo.cancel(id)) {
            is Resource.Success -> load()
            else -> Unit
        }
    }
}

// ── Reminder ViewModel ────────────────────────────────────────────────────────
@HiltViewModel
class ReminderViewModel @Inject constructor(private val repo: ReminderRepository) : ViewModel() {
    private val _state = MutableStateFlow(ListUiState<MedicineReminder>())
    val state: StateFlow<ListUiState<MedicineReminder>> = _state

    private val _action = MutableStateFlow(ActionUiState())
    val action: StateFlow<ActionUiState> = _action

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = ListUiState(isLoading = true)
        when (val r = repo.myReminders()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> Unit
        }
    }

    fun create(reminder: MedicineReminder) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.create(reminder)) {
            is Resource.Success -> { _action.value = ActionUiState(isSuccess = true); load() }
            is Resource.Error   -> _action.value = ActionUiState(error = r.message)
            Resource.Loading    -> Unit
        }
    }

    fun update(reminder: MedicineReminder) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.update(reminder.id, reminder)) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                load()
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            Resource.Loading -> Unit
        }
    }

    fun toggle(reminder: MedicineReminder) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.update(reminder.id, reminder.copy(active = !reminder.active))) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                load()
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            Resource.Loading -> Unit
        }
    }

    fun delete(id: Long) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.delete(id)) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                load()
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            Resource.Loading -> Unit
        }
    }
}

// ── Health Record ViewModel ───────────────────────────────────────────────────
@HiltViewModel
class HealthRecordViewModel @Inject constructor(private val repo: HealthRecordRepository) : ViewModel() {
    private val _state = MutableStateFlow(ListUiState<HealthRecord>())
    val state: StateFlow<ListUiState<HealthRecord>> = _state

    private val _action = MutableStateFlow(ActionUiState())
    val action: StateFlow<ActionUiState> = _action

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = ListUiState(isLoading = true)
        when (val r = repo.myRecords()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> Unit
        }
    }

    fun create(title: String, type: String, imageUrl: String? = null) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        val record = HealthRecord(title = title, type = type, imageUrl = imageUrl)
        when (val r = repo.create(record)) {
            is Resource.Success -> { _action.value = ActionUiState(isSuccess = true); load() }
            is Resource.Error   -> _action.value = ActionUiState(error = r.message)
            Resource.Loading    -> Unit
        }
    }

    fun update(id: Long, title: String, type: String, imageUrl: String? = null) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        val record = HealthRecord(id = id, title = title, type = type, imageUrl = imageUrl)
        when (val r = repo.update(id, record)) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                load()
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            Resource.Loading -> Unit
        }
    }

    fun delete(id: Long) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.delete(id)) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                load()
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            Resource.Loading -> Unit
        }
    }
}

// ── Telemedicine ViewModel ────────────────────────────────────────────────────
@HiltViewModel
class TelemedicineViewModel @Inject constructor(private val repo: TelemedicineRepository) : ViewModel() {
    private val _state = MutableStateFlow(ListUiState<TelemedicineSession>())
    val state: StateFlow<ListUiState<TelemedicineSession>> = _state

    private val _action = MutableStateFlow(ActionUiState())
    val action: StateFlow<ActionUiState> = _action

    init { load() }

    fun load() = viewModelScope.launch {
        _state.value = ListUiState(isLoading = true)
        when (val r = repo.mySessions()) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error   -> _state.value = ListUiState(error = r.message)
            Resource.Loading    -> Unit
        }
    }

    fun create(session: TelemedicineSession) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.create(session)) {
            is Resource.Success -> { _action.value = ActionUiState(isSuccess = true); load() }
            is Resource.Error   -> _action.value = ActionUiState(error = r.message)
            Resource.Loading    -> Unit
        }
    }
}
