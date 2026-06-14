package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.CallState
import com.example.viewmodel.SocialViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun ChatScreen(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val chats by viewModel.latestChats.collectAsStateWithLifecycle(initialValue = emptyList())
    val activeRoomId by viewModel.activeChatRoomId.collectAsStateWithLifecycle()
    val callState by viewModel.activeCallState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        if (activeRoomId == null) {
            // Screen 1: List of encrypted chat rooms
            Column(modifier = Modifier.fillMaxSize()) {
                ChatScreenHeader()
                ChatsList(chats = chats, onChatSelected = { preview ->
                    viewModel.openChatRoom(preview.chatRoomId, preview.groupName ?: preview.senderName, preview.isGroupChat)
                })
            }
        } else {
            // Screen 2: Detailed single room chat logs
            ChatRoomDetail(viewModel = viewModel)
        }

        // Screen 3: Absolute overlay full-screen secure call panel
        AnimatedVisibility(
            visible = callState != CallState.Idle,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            VoipCallOverlay(callState = callState, viewModel = viewModel)
        }
    }
}

@Composable
fun ChatScreenHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.EnhancedEncryption,
                contentDescription = "Encrypted Messenger",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Secure Network",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))
            Badge(
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text("E2E ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
        Text(
            text = "Peer-to-peer chats and calls are hardened with double-ratchet encryption.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun ChatsList(
    chats: List<com.example.data.ChatPreview>,
    onChatSelected: (com.example.data.ChatPreview) -> Unit
) {
    if (chats.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Empty Chats",
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No active dialogues",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Start a secure conversation directly from a creator profile or search community portals.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(chats, key = { it.chatRoomId }) { item ->
                ListItem(
                    headlineContent = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.groupName ?: item.senderName,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            if (item.isGroupChat) {
                                Box(
                                    modifier = Modifier
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text("GROUP", fontSize = 8.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    },
                    supportingContent = {
                        Text(
                            text = item.content,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    },
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f), Color.Transparent)
                                    ),
                                    shape = CircleShape
                                )
                                .border(1.5.dp, MaterialTheme.colorScheme.primary, shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = (item.groupName ?: item.senderName).take(2).uppercase(),
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    trailingContent = {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "15:27", // Sample timestamp
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "E2E Encrypted",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier
                        .clickable { onChatSelected(item) }
                        .testTag("chat_room_item_${item.chatRoomId}")
                )
                Divider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatRoomDetail(viewModel: SocialViewModel) {
    val title by viewModel.activeChatTitle.collectAsStateWithLifecycle()
    val isGroup by viewModel.activeChatIsGroup.collectAsStateWithLifecycle()
    val roomMessages by viewModel.messagesInActiveRoom.collectAsStateWithLifecycle()
    val userTypedText by viewModel.typedMessageText.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        // Detailed room topbar
        TopAppBar(
            title = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text("Secure Peer Node Link Active", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                }
            },
            navigationIcon = {
                IconButton(
                    onClick = { viewModel.closeChatRoom() },
                    modifier = Modifier.testTag("back_to_chats_list")
                ) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                // VoIP secure voice call
                IconButton(
                    onClick = { viewModel.triggerSimulatedCall(title, "@peer_node", isVideo = false) },
                    modifier = Modifier.testTag("voice_call_header_btn")
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = "VoIP Voice Call", tint = MaterialTheme.colorScheme.primary)
                }
                // VoIP secure video call
                IconButton(
                    onClick = { viewModel.triggerSimulatedCall(title, "@peer_node", isVideo = true) },
                    modifier = Modifier.testTag("video_call_header_btn")
                ) {
                    Icon(imageVector = Icons.Default.Videocam, contentDescription = "VoIP Video Call", tint = MaterialTheme.colorScheme.primary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
        )

        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

        // Chats Thread LazyColumn
        LazyColumn(
            modifier = Modifier
                .weight(1.5f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
        ) {
            items(roomMessages, key = { it.id }) { msg ->
                val bubbleAlign = if (msg.isFromMe) Alignment.CenterEnd else Alignment.CenterStart
                val bubbleShape = if (msg.isFromMe) {
                    RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                } else {
                    RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                }
                val bubbleColor = if (msg.isFromMe) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = bubbleAlign
                ) {
                    Column(
                        horizontalAlignment = if (msg.isFromMe) Alignment.End else Alignment.Start,
                        modifier = Modifier.widthIn(max = 280.dp)
                    ) {
                        if (!msg.isFromMe && isGroup) {
                            Text(
                                text = msg.senderName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(bubbleColor, shape = bubbleShape)
                                .border(
                                    width = 0.5.dp,
                                    color = if (msg.isFromMe) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else Color.Transparent,
                                    shape = bubbleShape
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            val formattedTime = remember(msg.timestamp) {
                                val sdf = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                                sdf.format(java.util.Date(msg.timestamp))
                            }
                            Column {
                                Text(
                                    text = msg.content,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    // Timestamp
                                    Text(
                                        text = formattedTime,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Encrypted",
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "E2E",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                                    )
                                    if (msg.isFromMe) {
                                        Spacer(modifier = Modifier.width(2.dp))
                                        when (msg.readStatus) {
                                            "SENT" -> {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = "Sent",
                                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                    modifier = Modifier.size(12.dp).testTag("read_receipt_sent")
                                                )
                                            }
                                            "DELIVERED" -> {
                                                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Delivered",
                                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                        modifier = Modifier.size(12.dp).testTag("read_receipt_delivered_1")
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Delivered",
                                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                        modifier = Modifier.size(12.dp).testTag("read_receipt_delivered_2")
                                                    )
                                                }
                                            }
                                            "READ" -> {
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy((-6).dp),
                                                    modifier = Modifier.testTag("read_receipt_indicator")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Read",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Read",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom composition chat typing bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = userTypedText,
                onValueChange = { viewModel.typedMessageText.value = it },
                placeholder = { Text("Compose encrypted message...", fontSize = 13.sp) },
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .testTag("chat_input_text_field"),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { viewModel.sendChatMessage() },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("send_chat_message_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun VoipCallOverlay(
    callState: CallState,
    viewModel: SocialViewModel
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val displayBackground = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(displayBackground, Color.Black)
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Secure connection padlock header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Icon(Icons.Default.VerifiedUser, contentDescription = "Verified Key", tint = primaryColor, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    "MILITARY-GRADE CODER E2E CALL",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = primaryColor,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Central Ring Avatar
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .drawBehind {
                        drawCircle(
                            color = primaryColor.copy(alpha = 0.15f),
                            radius = size.minDimension / 1.5f
                        )
                    }
                    .border(2.dp, primaryColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (callState) {
                        is CallState.Incoming -> callState.callerName.take(2).uppercase()
                        is CallState.Outgoing -> callState.receiverName.take(2).uppercase()
                        is CallState.Connected -> callState.peerName.take(2).uppercase()
                        else -> "AU"
                    },
                    fontWeight = FontWeight.Black,
                    fontSize = 38.sp,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Peer names
            val labelName = when (callState) {
                is CallState.Incoming -> callState.callerName
                is CallState.Outgoing -> callState.receiverName
                is CallState.Connected -> callState.peerName
                else -> "Aura Node"
            }
            val labelHandle = when (callState) {
                is CallState.Incoming -> callState.callerHandle
                is CallState.Outgoing -> callState.receiverHandle
                is CallState.Connected -> callState.peerHandle
                else -> "@node_stream"
            }

            Text(text = labelName, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text(text = labelHandle, fontSize = 14.sp, color = primaryColor, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(16.dp))

            // Call status
            val codecLabel = when (callState) {
                is CallState.Incoming -> "Incoming cryptographic voice connection..."
                is CallState.Outgoing -> "Spinning up secure UDP tunnels..."
                is CallState.Connected -> "Connected • AES-256-GCM Dual-Channel"
                else -> ""
            }
            Text(text = codecLabel, fontSize = 12.sp, color = Color.LightGray)

            Spacer(modifier = Modifier.height(24.dp))

            // Connected Waveform & Encryption Keys section
            if (callState is CallState.Connected) {
                // Call Stopwatch
                val mins = String.format("%02d", callState.durationSeconds / 60)
                val secs = String.format("%02d", callState.durationSeconds % 60)
                Text(
                    text = "$mins:$secs",
                    fontSize = 32.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                // custom waveform generator canvas!
                BouncingEqualizer(color = primaryColor)

                Spacer(modifier = Modifier.height(24.dp))

                // Cipher token
                Text("Verification cryptographic fingerprint hash:", fontSize = 10.sp, color = Color.Gray)
                Text(
                    text = callState.cipherKey,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .background(Color(0xFF0C0C12), shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(50.dp))

            // Action Dialer Keys row
            Row(
                horizontalArrangement = Arrangement.spacedBy(28.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (callState is CallState.Incoming) {
                    // Decline inbound Call button
                    IconButton(
                        onClick = { viewModel.endCall() },
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color.Red, CircleShape)
                            .testTag("decline_call_btn")
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "Decline Call", tint = Color.White)
                    }

                    // Accept inbound Call button
                    IconButton(
                        onClick = { viewModel.acceptIncomingCall() },
                        modifier = Modifier
                            .size(60.dp)
                            .background(Color(0xFF00E676), CircleShape)
                            .testTag("accept_call_btn")
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Accept Call", tint = Color.Black)
                    }
                } else {
                    // Outgoing/Connected actions: Mute toggle
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.Default.MicNone, contentDescription = "Mute mic", tint = Color.White)
                    }

                    // Hang up call button
                    IconButton(
                        onClick = { viewModel.endCall() },
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color.Red, CircleShape)
                            .testTag("end_call_btn")
                    ) {
                        Icon(Icons.Default.CallEnd, contentDescription = "End call link", tint = Color.White)
                    }

                    // Speaker toggle
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Speaker boost", tint = Color.White)
                    }
                }
            }
        }
    }
}

// Custom canvas wave visual drawing
@Composable
fun BouncingEqualizer(color: Color) {
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .padding(horizontal = 32.dp)
    ) {
        val count = 28
        val spacing = size.width / count
        val strokeWidthPx = 3.dp.toPx()
        for (i in 0 until count) {
            val offsetFactor = (i.toFloat() / count) * 4f * Math.PI.toFloat()
            // Sine math drives the heights to represent raw vocal vibration frequencies!
            val amplitude = (sin(phase.toDouble() + offsetFactor.toDouble()) * 15.0 + 20.0).toFloat()
            val barX = i * spacing
            drawLine(
                color = color,
                start = Offset(x = barX.toFloat(), y = (size.height / 2f - amplitude).toFloat()),
                end = Offset(x = barX.toFloat(), y = (size.height / 2f + amplitude).toFloat()),
                strokeWidth = strokeWidthPx
            )
        }
    }
}
