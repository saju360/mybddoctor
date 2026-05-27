package com.lifeplus.healthcare.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeplus.healthcare.data.api.ApiService
import com.lifeplus.healthcare.data.repository.*
import com.lifeplus.healthcare.data.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.lifeplus.healthcare.model.DashboardSlide

data class DashboardStats(
    val doctorCount: String = "...",
    val donorCount: String = "...",
    val hospitalCount: String = "...",
    val activeRequestsCount: String = "...",
    val bloodGroupStats: Map<String, Int> = emptyMap()
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val api: ApiService,
    private val doctorRepo: DoctorRepository,
    private val donorRepo: DonorRepository,
    private val hospitalRepo: HospitalRepository,
    private val bloodRepo: BloodRequestRepository
) : ViewModel() {

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats

    private val _slides = MutableStateFlow<List<DashboardSlide>>(emptyList())
    val slides: StateFlow<List<DashboardSlide>> = _slides

    init {
        loadStats()
        loadSlides()
    }

    fun loadSlides() {
        viewModelScope.launch {
            try {
                val response = api.getDashboardSlides()
                if (response.isSuccessful) {
                    _slides.value = response.body() ?: emptyList()
                }
            } catch (e: Exception) {}
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            try {
                val docs = doctorRepo.getAll().let { if (it is Resource.Success) it.data.size else 0 }
                val donorsRes = donorRepo.getAll()
                val allDonors = if (donorsRes is Resource.Success) donorsRes.data else emptyList()
                
                // Only count APPROVED and AVAILABLE donors to match the donor list
                val donors = allDonors.filter { it.status == "APPROVED" && it.availableNow }
                
                val hosps = hospitalRepo.getAll().let { if (it is Resource.Success) it.data.size else 0 }
                val reqs = bloodRepo.getAll().let { if (it is Resource.Success) it.data.size else 0 }

                val bgStats = donors.groupBy { it.bloodGroup ?: "Unknown" }
                    .mapValues { it.value.size }

                _stats.value = DashboardStats(
                    doctorCount = docs.toString(),
                    donorCount = donors.size.toString(),
                    hospitalCount = hosps.toString(),
                    activeRequestsCount = reqs.toString(),
                    bloodGroupStats = bgStats
                )
            } catch (e: Exception) {}
        }
    }
}
