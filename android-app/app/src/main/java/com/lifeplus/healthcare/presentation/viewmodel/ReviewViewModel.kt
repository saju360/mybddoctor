package com.lifeplus.healthcare.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeplus.healthcare.data.model.Review
import com.lifeplus.healthcare.data.repository.ReviewRepository
import com.lifeplus.healthcare.data.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val repo: ReviewRepository
) : ViewModel() {
    private val _state = MutableStateFlow(ListUiState<Review>())
    val state: StateFlow<ListUiState<Review>> = _state

    private val _action = MutableStateFlow(ActionUiState())
    val action: StateFlow<ActionUiState> = _action

    fun load(type: String, id: Long) = viewModelScope.launch {
        _state.value = ListUiState(isLoading = true)
        when (val r = repo.getReviews(type, id)) {
            is Resource.Success -> _state.value = ListUiState(data = r.data)
            is Resource.Error -> _state.value = ListUiState(error = r.message)
            Resource.Loading -> _state.value = ListUiState(isLoading = true)
        }
    }

    fun submit(review: Review) = viewModelScope.launch {
        _action.value = ActionUiState(isLoading = true)
        when (val r = repo.submit(review)) {
            is Resource.Success -> {
                _action.value = ActionUiState(isSuccess = true)
                load(review.entityType, review.entityId)
            }
            is Resource.Error -> _action.value = ActionUiState(error = r.message)
            Resource.Loading -> _action.value = ActionUiState(isLoading = true)
        }
    }
}
