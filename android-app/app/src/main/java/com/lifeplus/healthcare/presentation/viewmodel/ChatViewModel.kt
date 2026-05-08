package com.lifeplus.healthcare.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifeplus.healthcare.data.model.ChatMessage
import com.lifeplus.healthcare.data.model.ChatRoom
import com.lifeplus.healthcare.data.repository.ChatRepository
import com.lifeplus.healthcare.data.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repo: ChatRepository
) : ViewModel() {
    private val _rooms = MutableStateFlow(ListUiState<ChatRoom>())
    val rooms: StateFlow<ListUiState<ChatRoom>> = _rooms

    private val _messages = MutableStateFlow(ListUiState<ChatMessage>())
    val messages: StateFlow<ListUiState<ChatMessage>> = _messages

    private val _activeRoom = MutableStateFlow<ChatRoom?>(null)
    val activeRoom: StateFlow<ChatRoom?> = _activeRoom

    fun loadRooms() = viewModelScope.launch {
        _rooms.value = ListUiState(isLoading = true)
        when (val r = repo.getRooms()) {
            is Resource.Success -> _rooms.value = ListUiState(data = r.data)
            is Resource.Error -> _rooms.value = ListUiState(error = r.message)
            Resource.Loading -> Unit
        }
    }

    fun loadMessages(roomId: Long) = viewModelScope.launch {
        _messages.value = ListUiState(isLoading = true)
        when (val r = repo.getMessages(roomId)) {
            is Resource.Success -> _messages.value = ListUiState(data = r.data)
            is Resource.Error -> _messages.value = ListUiState(error = r.message)
            Resource.Loading -> Unit
        }
    }

    fun sendMessage(roomId: Long, text: String) = viewModelScope.launch {
        if (text.isBlank()) return@launch
        val msg = ChatMessage(roomId = roomId, message = text)
        when (val r = repo.sendMessage(msg)) {
            is Resource.Success -> {
                loadMessages(roomId)
                loadRooms() // Update last message
            }
            else -> {}
        }
    }

    fun startChat(userId: Long, onStarted: (Long) -> Unit) = viewModelScope.launch {
        when (val r = repo.startChat(userId)) {
            is Resource.Success -> {
                onStarted(r.data.id)
            }
            else -> {}
        }
    }
}
