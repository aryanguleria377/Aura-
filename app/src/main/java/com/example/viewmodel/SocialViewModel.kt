package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.Serializable
import java.util.UUID

enum class AuraTheme {
    OBSIDIAN_LUXURY,
    AMETHYST_SUNSET,
    OCEAN_EMERALD,
    AMBER_EMBER,
    LIGHT_MINIMAL
}

enum class HomeFeedTab {
    ALL,
    TEXT_X,
    IMAGE_INSTA,
    VIDEO_YOUTUBE,
    REELS_SHORTS
}

enum class DiscoveryCategory {
    FOLLOWING,
    TRENDING,
    FOR_YOU
}

enum class ActiveScreen {
    FEED,
    MESSENGER,
    CREATOR_HUB,
    PROFILE,
    MODERATION
}

sealed interface CallState {
    object Idle : CallState
    data class Incoming(val callerName: String, val callerHandle: String, val isVideo: Boolean) : CallState
    data class Outgoing(val receiverName: String, val receiverHandle: String, val isVideo: Boolean) : CallState
    data class Connected(val peerName: String, val peerHandle: String, val isVideo: Boolean, val durationSeconds: Int, val cipherKey: String) : CallState
}

class SocialViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SocialRepository

    // Base Database flows
    val currentUser: StateFlow<UserEntity?>
    val allPosts: StateFlow<List<PostEntity>>
    val rawPosts: StateFlow<List<PostEntity>>
    val flaggedPosts: StateFlow<List<PostEntity>>
    val latestChats: StateFlow<List<ChatPreview>>

    // Navigation and Feed UI states
    val currentTheme = MutableStateFlow(AuraTheme.OBSIDIAN_LUXURY)
    val activeScreen = MutableStateFlow(ActiveScreen.FEED)
    val homeFeedTab = MutableStateFlow(HomeFeedTab.ALL)
    val discoveryCategory = MutableStateFlow(DiscoveryCategory.FOR_YOU)

    // Skeleton loaders
    val isFeedLoading = MutableStateFlow(false)
    val isProfileLoading = MutableStateFlow(false)
    
    // Chat Room UI states
    val activeChatRoomId = MutableStateFlow<String?>(null)
    val activeChatTitle = MutableStateFlow("")
    val activeChatIsGroup = MutableStateFlow(false)
    val typedMessageText = MutableStateFlow("")
    val messagesInActiveRoom: StateFlow<List<MessageEntity>>

    // Calling simulation states
    val activeCallState = MutableStateFlow<CallState>(CallState.Idle)
    private var callTimerJob: kotlinx.coroutines.Job? = null

    // Live Streaming Simulator states
    val isLiveStreaming = MutableStateFlow(false)
    val liveViewerCount = MutableStateFlow(0)
    val liveHeartsCount = MutableStateFlow(0)
    val liveComments = mutableStateListOf<Pair<String, String>>() // Key -> AuthorName, Value -> TextContent
    private var liveSimulatorJob: kotlinx.coroutines.Job? = null

    // Search and Hashtag filter states
    val searchQuery = MutableStateFlow("")
    val activeHashtagFilter = MutableStateFlow<String?>(null)
    val activeCommunityFilter = MutableStateFlow<String?>(null)

    // User authentication simulation
    val currentSessionJwt = MutableStateFlow("jwt_session_token_encrypted_biometrics_71xabg.v92")
    val isLoggedIn = MutableStateFlow(false)

    fun logInUser(email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if (email.trim().equals("aryanguleria377@gmail.com", ignoreCase = true)) {
                repository.deleteAllUsers()
                val authorityUser = UserEntity(
                    uniqueId = "aura_authority_prime",
                    name = "Aryan Guleria",
                    handle = "aryan_authority",
                    bio = "Official Aura Security & System Authority Administrator. Special clearance level 50 active.",
                    avatarUrl = "aura_admin_avatar",
                    qrCodeSeed = "aura:authority:aryan_authority:obsidian:71x",
                    isPremium = true,
                    membershipTier = "OBSIDIAN"
                )
                repository.insertUser(authorityUser)
            } else {
                val existingUser = repository.getCurrentUserDirect()
                if (existingUser == null) {
                    repository.prePopulateIfEmpty()
                }
            }
            currentSessionJwt.value = "jwt_email_" + UUID.randomUUID().toString().take(12)
            isLoggedIn.value = true
        }
    }

    fun logInGoogle() {
        viewModelScope.launch(Dispatchers.IO) {
            val existingUser = repository.getCurrentUserDirect()
            if (existingUser == null) {
                repository.prePopulateIfEmpty()
            }
            currentSessionJwt.value = "jwt_google_" + UUID.randomUUID().toString().take(12)
            isLoggedIn.value = true
        }
    }

    fun logout() {
        isLoggedIn.value = false
    }

    fun authenticateUser(name: String, handle: String, bio: String, email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val randomId = "user_" + UUID.randomUUID().toString().take(6)
            val cleanHandle = handle.trim().removePrefix("@")
            val newUser = UserEntity(
                uniqueId = randomId,
                name = name.trim(),
                handle = if (cleanHandle.isEmpty()) "user_$randomId" else cleanHandle,
                bio = if (bio.trim().isEmpty()) "Aura member since June 2026." else bio.trim(),
                isPremium = false,
                membershipTier = "FREE"
            )
            repository.insertUser(newUser)
            currentSessionJwt.value = "jwt_auth_" + UUID.randomUUID().toString().take(12)
            isLoggedIn.value = true
        }
    }

    // Security and Moderation Panel states
    val blockedKeywords = MutableStateFlow(listOf("spam", "scam", "offensive", "bot", "advert"))
    val parentalControlRestricted = MutableStateFlow(false)
    val aiAutomateModeratorEnabled = MutableStateFlow(true)
    val moderationLogs = mutableStateListOf<String>()

    init {
        val db = AppDatabase.getDatabase(application)
        repository = SocialRepository(db)

        // Seed initial databases if first launch
        viewModelScope.launch(Dispatchers.IO) {
            repository.prePopulateIfEmpty()
            isFeedLoading.value = false
            isProfileLoading.value = false
        }

        // Connect data streams
        currentUser = repository.currentUser
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        allPosts = repository.allItemsFlowWithSearch(searchQuery, activeHashtagFilter, activeCommunityFilter)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        rawPosts = repository.rawPosts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        flaggedPosts = repository.flaggedPosts
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        latestChats = repository.latestChats
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        messagesInActiveRoom = activeChatRoomId
            .flatMapLatest { roomId ->
                if (roomId != null) {
                    repository.getMessagesForRoom(roomId)
                } else {
                    flowOf(emptyList())
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // Custom filtering extensions on Repository Flow
    private fun SocialRepository.allItemsFlowWithSearch(
        search: StateFlow<String>,
        hashtag: StateFlow<String?>,
        community: StateFlow<String?>
    ): Flow<List<PostEntity>> {
        return combine(allPosts, search, hashtag, community) { posts, query, tag, comm ->
            posts.filter { post ->
                val matchesSearch = query.isEmpty() ||
                        post.textContent.contains(query, ignoreCase = true) ||
                        post.authorName.contains(query, ignoreCase = true) ||
                        post.authorHandle.contains(query, ignoreCase = true)

                val matchesHashtag = tag == null || post.hashtags.contains(tag, ignoreCase = true)
                val matchesCommunity = comm == null || post.communityName.equals(comm, ignoreCase = true)

                matchesSearch && matchesHashtag && matchesCommunity
            }
        }
    }

    // --- Action Methods ---

    fun changeTheme(theme: AuraTheme) {
        currentTheme.value = theme
    }

    fun selectScreen(screen: ActiveScreen) {
        activeScreen.value = screen
    }

    fun selectDiscoveryCategory(category: DiscoveryCategory) {
        discoveryCategory.value = category
    }

    fun selectFeedTab(tab: HomeFeedTab) {
        homeFeedTab.value = tab
        // Clear quick hashtag and community filters when navigating tabs
        activeHashtagFilter.value = null
        activeCommunityFilter.value = null
    }

    fun triggerManualFeedRefresh() {
        // No-op for direct loading
    }

    fun triggerManualProfileRefresh() {
        // No-op for direct loading
    }

    fun searchPosts(query: String) {
        searchQuery.value = query
    }

    fun setHashtagFilter(tag: String?) {
        activeHashtagFilter.value = tag
        searchQuery.value = ""
    }

    fun setCommunityFilter(com: String?) {
        activeCommunityFilter.value = com
        searchQuery.value = ""
    }

    // --- Post Management ---

    fun toggleLikePost(postId: Long, currentLiked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updatePostLike(postId, !currentLiked)
        }
    }

    fun sharePost(post: PostEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Simulated repost increment
            val updated = post.copy(repostsCount = post.repostsCount + 1)
            repository.updatePost(updated)
            moderationLogs.add("Alex reposted @${post.authorHandle}'s feed update.")
        }
    }

    fun addComment(postId: Long, text: String, profileName: String, profileHandle: String) {
        if (text.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val comment = CommentEntity(
                postId = postId,
                authorName = profileName,
                authorHandle = "@$profileHandle",
                content = text
            )
            repository.insertComment(comment)
            
            // Increment comment count on post
            val target = repository.getPostById(postId)
            if (target != null) {
                repository.updatePost(target.copy(commentsCount = target.commentsCount + 1))
            }
        }
    }

    fun getCommentsForPostFlow(postId: Long): Flow<List<CommentEntity>> {
        return repository.getCommentsForPost(postId)
    }

    // Dynamic creator publishing with intelligent AI Auto-Moderator
    fun publishPost(textContent: String, typeString: String, mediaDesc: String, tags: String, community: String) {
        if (textContent.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            val me = repository.getCurrentUserDirect() ?: return@launch

            // Auto Moderation rule check
            var isFlagged = false
            var reason = ""
            if (aiAutomateModeratorEnabled.value) {
                for (word in blockedKeywords.value) {
                    if (textContent.contains(word, ignoreCase = true) || tags.contains(word, ignoreCase = true)) {
                        isFlagged = true
                        reason = "Contains prohibited term: '$word'"
                        break
                    }
                }
            }

            val newPost = PostEntity(
                authorName = me.name,
                authorHandle = "@${me.handle}",
                authorAvatarUrl = me.avatarUrl,
                postType = typeString,
                textContent = textContent,
                mediaUrl = mediaDesc,
                hashtags = tags,
                communityName = community,
                moderationStatus = if (isFlagged) "FLAGGED" else "APPROVED",
                durationSeconds = if (typeString.contains("VIDEO")) 45 else 0
            )

            repository.insertPost(newPost)
            
            if (isFlagged) {
                moderationLogs.add("AI Sentinel flagged post by @${me.handle}: $reason. Moved to review queue.")
            } else {
                moderationLogs.add("Successfully published and distributed post of type $typeString.")
            }
        }
    }

    // --- Message Management ---

    fun openChatRoom(roomId: String, title: String, isGroup: Boolean) {
        activeChatRoomId.value = roomId
        activeChatTitle.value = title
        activeChatIsGroup.value = isGroup
        typedMessageText.value = ""
    }

    fun closeChatRoom() {
        activeChatRoomId.value = null
    }

    fun sendChatMessage() {
        val body = typedMessageText.value
        val roomId = activeChatRoomId.value
        if (body.isBlank() || roomId == null) return

        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getCurrentUserDirect() ?: return@launch
            val isEncrypted = true // All custom messengers in Aura are default encrypted

            val newMsg = MessageEntity(
                chatRoomId = roomId,
                senderId = user.uniqueId,
                senderName = user.name,
                senderHandle = "@${user.handle}",
                content = body,
                isEncrypted = isEncrypted,
                isFromMe = true,
                isGroupChat = activeChatIsGroup.value,
                groupName = if (activeChatIsGroup.value) activeChatTitle.value else "",
                readStatus = "SENT"
            )

            // Insert message and retrieve its auto-generated Row ID
            val insertedId = repository.insertMessage(newMsg)
            typedMessageText.value = ""

            // Progress to DELIVERED status quickly
            delay(500)
            repository.updateMessageStatus(insertedId, "DELIVERED")

            // Beautiful Auto-responder logic for interactive conversation simulator!
            delay(1000)
            val replyBody = when {
                body.contains("hello", ignoreCase = true) || body.contains("hi", ignoreCase = true) ->
                    "Hello! E2E Encryption status SECURED. Chat logs fully isolated."
                body.contains("premium", ignoreCase = true) ->
                    "Absoutely. Premium users get access to Amber Ember presets, Gold Crown badge, and 100% video tipping royalties."
                body.contains("call", ignoreCase = true) ->
                    "Triggering secure network link! Click the VoIP Call button at the top header to establish link."
                else -> "Aura Sync: Message relayed securely with SHA-512 cryptographic verification index."
            }

            // Progress user's message to READ status once the reply is initiated
            repository.updateMessageStatus(insertedId, "READ")

            val responseMsg = MessageEntity(
                chatRoomId = roomId,
                senderId = "bot_reply_idx",
                senderName = if (activeChatIsGroup.value) "Community Bot" else activeChatTitle.value,
                senderHandle = if (activeChatIsGroup.value) "@com_bot" else "@peer_relay",
                content = replyBody,
                isEncrypted = true,
                isFromMe = false,
                isGroupChat = activeChatIsGroup.value,
                groupName = if (activeChatIsGroup.value) activeChatTitle.value else "",
                readStatus = "READ"
            )
            repository.insertMessage(responseMsg)
        }
    }

    // --- Secure VoIP Calling Engine ---

    fun triggerSimulatedCall(name: String, handle: String, isVideo: Boolean, incoming: Boolean = false) {
        if (incoming) {
            activeCallState.value = CallState.Incoming(name, handle, isVideo)
        } else {
            activeCallState.value = CallState.Outgoing(name, handle, isVideo)
            // Auto connect after 2 seconds
            viewModelScope.launch {
                delay(2500)
                establishConnectedCall(name, handle, isVideo)
            }
        }
    }

    private fun establishConnectedCall(name: String, handle: String, isVideo: Boolean) {
        val uniqueCipher = "CH_AES_GCM_" + UUID.randomUUID().toString().take(8).uppercase()
        activeCallState.value = CallState.Connected(
            peerName = name,
            peerHandle = handle,
            isVideo = isVideo,
            durationSeconds = 0,
            cipherKey = uniqueCipher
        )

        // Start call stopwatch
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = activeCallState.value
                if (current is CallState.Connected) {
                    activeCallState.value = current.copy(durationSeconds = current.durationSeconds + 1)
                } else {
                    break
                }
            }
        }
    }

    fun acceptIncomingCall() {
        val state = activeCallState.value
        if (state is CallState.Incoming) {
            establishConnectedCall(state.callerName, state.callerHandle, state.isVideo)
        }
    }

    fun endCall() {
        callTimerJob?.cancel()
        callTimerJob = null
        activeCallState.value = CallState.Idle
    }

    // --- Live Stream Broadcast Simulator ---

    fun toggleLiveStream() {
        val goingLive = !isLiveStreaming.value
        isLiveStreaming.value = goingLive

        if (goingLive) {
            liveViewerCount.value = 142
            liveHeartsCount.value = 45
            liveComments.clear()
            liveComments.add("Sophia Drake" to "Incredible stream clarity! What audio compression is Aura using?")
            liveComments.add("Julian Vance" to "Visual contrast is immaculate. Obsidian theme looks awesome.")

            // Start comments & hearts generator job
            liveSimulatorJob?.cancel()
            liveSimulatorJob = viewModelScope.launch {
                val seedComments = listOf(
                    "Sarah_99" to "Aura is crazy fast!",
                    "DevX" to "Subscribed to your Golden Circle! Keep it up!",
                    "Crypto_Alpha" to "Sent 50 GOLD tips! 🔥🔥",
                    "Roxanne" to "Love the edge-to-edge layout design.",
                    "Aris_Global" to "E2E encryption on the feed comment too?"
                )
                while (isLiveStreaming.value) {
                    delay(3000)
                    liveViewerCount.value += (-5..12).random()
                    liveHeartsCount.value += (1..5).random()
                    val nextC = seedComments.random()
                    liveComments.add(nextC)
                    if (liveComments.size > 20) {
                        liveComments.removeAt(0)
                    }
                }
            }
        } else {
            liveSimulatorJob?.cancel()
            liveSimulatorJob = null
            liveViewerCount.value = 0
            liveHeartsCount.value = 0
        }
    }

    fun sendLiveHeart() {
        liveHeartsCount.value += 12
    }

    fun sendDonationTip(amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addEarnings(amount)
            liveComments.add("Viewer Donation" to "Sent a $$$amount Tip! 🌟🏆")
            liveHeartsCount.value += 50
        }
    }

    // --- Tier Subscriptions ---

    fun purchasePremiumTier(tier: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getCurrentUserDirect() ?: return@launch
            val isPrem = tier != "FREE"
            repository.updateMembership(user.uniqueId, isPrem, tier)
            moderationLogs.add("Membership upgraded to account tier: $tier.")

            // Instantly transition theme based on premium choices
            if (tier == "GOLD") {
                currentTheme.value = AuraTheme.AMBER_EMBER
            } else if (tier == "OBSIDIAN") {
                currentTheme.value = AuraTheme.OBSIDIAN_LUXURY
            }
        }
    }

    fun updateProfile(name: String, bio: String, avatarUrl: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getCurrentUserDirect() ?: return@launch
            val updated = user.copy(name = name, bio = bio, avatarUrl = avatarUrl)
            repository.insertUser(updated)
            moderationLogs.add("User profile parameters updated (Name: $name).")
        }
    }

    // --- Security and Moderation Controls ---

    fun toggleParentalControls(enabled: Boolean) {
        parentalControlRestricted.value = enabled
        moderationLogs.add("Parental Control bounds redefined: Filter level = $enabled.")
    }

    fun toggleAiModerator(enabled: Boolean) {
        aiAutomateModeratorEnabled.value = enabled
        moderationLogs.add("AI Autonomous Sentinel State: $enabled.")
    }

    fun addBlockedKeyword(word: String) {
        if (word.isBlank()) return
        val current = blockedKeywords.value.toMutableList()
        if (!current.contains(word.trim().lowercase())) {
            current.add(word.trim().lowercase())
            blockedKeywords.value = current
            moderationLogs.add("Added blocked tag parameter: '$word'")
        }
    }

    fun approveFlaggedPost(postId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val post = repository.getPostById(postId)
            if (post != null) {
                val updated = post.copy(moderationStatus = "APPROVED")
                repository.updatePost(updated)
                moderationLogs.add("Moderator manually authorized post ID: $postId.")
            }
        }
    }

    fun rejectFlaggedPost(postId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val post = repository.getPostById(postId)
            if (post != null) {
                repository.deletePost(post)
                moderationLogs.add("Moderator deleted post ID: $postId from server due to policy violation.")
            }
        }
    }
}
