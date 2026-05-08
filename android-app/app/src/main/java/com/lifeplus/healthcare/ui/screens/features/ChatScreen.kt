package com.lifeplus.healthcare.ui.screens.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeplus.healthcare.data.model.ChatMessage
import com.lifeplus.healthcare.data.model.ChatRoom
import com.lifeplus.healthcare.presentation.viewmodel.ChatViewModel
import com.lifeplus.healthcare.presentation.viewmodel.AuthViewModel
import com.lifeplus.healthcare.ui.components.*
import com.lifeplus.healthcare.ui.theme.*

@Composable
fun ChatListScreen(
    onRoomClick: (Long, String) -> Unit,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    val roomsState by viewModel.rooms.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadRooms()
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            PremiumTopBar(
                title = "Messages",
                subtitle = "Consult with experts",
                onBackClick = onBack
            )

            if (roomsState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (roomsState.data.isEmpty()) {
                com.lifeplus.healthcare.ui.screens.browse.EmptyState(query = "")
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(roomsState.data) { room ->
                        ChatRoomItem(room = room, onClick = { onRoomClick(room.id, room.otherUserName) })
                    }
                }
            }
        }
    }
}

@Composable
fun ChatRoomItem(room: ChatRoom, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = Color.White
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(52.dp), shape = CircleShape, color = PrimaryLight) {
                Box(contentAlignment = Alignment.Center) {
                    Text(room.otherUserName.take(1), color = Primary, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(room.otherUserName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(room.lastMessage ?: "Tap to start chatting", maxLines = 1, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            if (room.lastMessageTime != null) {
                Text(room.lastMessageTime.take(10), style = MaterialTheme.typography.labelSmall, color = TextHint)
            }
        }
    }
}

@Composable
fun ChatDetailScreen(
    roomId: Long,
    otherName: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val messagesState by viewModel.messages.collectAsState()
    val currentUserId by authViewModel.userId.collectAsState()
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(roomId) {
        viewModel.loadMessages(roomId)
    }

    AppBackground {
        Column(modifier = Modifier.fillMaxSize()) {
            PremiumTopBar(
                title = otherName,
                subtitle = "Active now",
                onBackClick = onBack
            )

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    reverseLayout = false
                ) {
                    items(messagesState.data) { msg ->
                        MessageBubble(msg = msg, isMine = msg.senderId == (currentUserId ?: -1L))
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = Surface2Light
                        )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(roomId, messageText)
                                messageText = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = Primary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(msg: ChatMessage, isMine: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (isMine) Primary else Surface2Light,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isMine) 16.dp else 4.dp,
                bottomEnd = if (isMine) 4.dp else 16.dp
            )
        ) {
            Text(
                text = msg.message,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = if (isMine) Color.White else TextPrimary,
                fontSize = 15.sp
            )
        }
    }
}
