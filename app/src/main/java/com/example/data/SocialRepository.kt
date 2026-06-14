package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.UUID

class SocialRepository(private val database: AppDatabase) {
    private val userDao = database.userDao()
    private val postDao = database.postDao()
    private val messageDao = database.messageDao()
    private val commentDao = database.commentDao()

    val currentUser: Flow<UserEntity?> = userDao.getCurrentUserFlow()
    val allPosts: Flow<List<PostEntity>> = postDao.getAllApprovedPosts()
    val rawPosts: Flow<List<PostEntity>> = postDao.getAllPostsRaw()
    val flaggedPosts: Flow<List<PostEntity>> = postDao.getFlaggedPosts()
    val latestChats: Flow<List<ChatPreview>> = messageDao.getLatestChatPreviews()

    fun getPostsByType(type: String): Flow<List<PostEntity>> = postDao.getPostsByType(type)
    fun getMessagesForRoom(roomId: String): Flow<List<MessageEntity>> = messageDao.getMessagesForRoom(roomId)
    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>> = commentDao.getCommentsForPost(postId)

    suspend fun insertUser(user: UserEntity) = userDao.insertUser(user)
    suspend fun deleteAllUsers() = userDao.deleteAllUsers()
    suspend fun updateMembership(userId: String, isPremium: Boolean, tier: String) =
        userDao.updateMembership(userId, isPremium, tier)
    suspend fun addEarnings(amount: Double) = userDao.addEarnings(amount)

    suspend fun getCurrentUserDirect(): UserEntity? = userDao.getCurrentUser()

    suspend fun insertPost(post: PostEntity): Long = postDao.insertPost(post)
    suspend fun updatePost(post: PostEntity) = postDao.updatePost(post)
    suspend fun getPostById(id: Long): PostEntity? = postDao.getPostById(id)
    suspend fun deletePost(post: PostEntity) = postDao.deletePost(post)
    suspend fun updatePostLike(postId: Long, clicked: Boolean) {
        val delta = if (clicked) 1 else -1
        postDao.updatePostLike(postId, delta, clicked)
    }

    suspend fun insertMessage(message: MessageEntity): Long = messageDao.insertMessage(message)
    suspend fun updateMessageStatus(msgId: Long, status: String) = messageDao.updateMessageStatus(msgId, status)
    suspend fun insertComment(comment: CommentEntity) = commentDao.insertComment(comment)

    suspend fun prePopulateIfEmpty() {
        val existingUser = userDao.getCurrentUser()
        if (existingUser != null) return // Already populated

        // 1. Create Default User
        val defaultUser = UserEntity(
            uniqueId = "aura_user_99a",
            name = "Alex Mercer",
            handle = "alex_aura",
            bio = "Digital Architect & Tech Creator ⚡ Crafting the future of global privacy and high-fidelity UI systems. Aura Lead Designer.",
            avatarUrl = "https://images.unsplash.com/photo-1544725176-7c40e5a71c5e?w=400&q=80",
            qrCodeSeed = "aura:user:alex_aura:premium:obsidian:99a",
            isPremium = true,
            membershipTier = "OBSIDIAN",
            followersCount = 14205,
            followingCount = 482,
            subscriberCount = 230,
            totalEarnings = 4250.00
        )
        userDao.insertUser(defaultUser)

        // 2. Insert Social Posts (Diverse Mix: Reels, Videos, Images, Text Updates)
        val samplePosts = listOf(
            PostEntity(
                authorName = "Sofia Chen",
                authorHandle = "@sofia_code",
                authorAvatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400&q=80",
                postType = "TEXT",
                textContent = "Excited to launch Aura! We built a unified platform prioritizing beautiful interfaces, custom dynamic themes, and military-grade end-to-end encryption. The messaging and visual calling flows are so fluid. Welcome to the new era! 🚀🌌",
                hashtags = "#auraconnect #dev #crypto #design",
                communityName = "Aura Founders",
                likesCount = 892,
                commentsCount = 142,
                repostsCount = 76,
                isLiked = false
            ),
            PostEntity(
                authorName = "Julian Vance",
                authorHandle = "@julian_lens",
                authorAvatarUrl = "https://images.unsplash.com/photo-1542204165-65bf26472b9b?w=400&q=80",
                postType = "IMAGE",
                textContent = "Midnight render of our decentralized storage network hubs. Built with frosted glass layouts, modern negative space, and deep violet luminescence. 📸🏔️🎨",
                mediaUrl = "https://images.unsplash.com/photo-1620121692029-d088224ddc74?w=800&q=80",
                hashtags = "#photography #metaspheres #3dartist",
                likesCount = 1205,
                commentsCount = 59,
                repostsCount = 43,
                isLiked = false
            ),
            PostEntity(
                authorName = "Elena Rostova",
                authorHandle = "@elena_sound",
                authorAvatarUrl = "https://images.unsplash.com/photo-1438761681033-6461ffad8d80?w=400&q=80",
                postType = "VIDEO_LONG",
                textContent = "The Future of Smart Synthesizers and Ambient Generative Audio in the Creators Era. Complete setup tour and acoustic physics breakdown.",
                mediaUrl = "https://images.unsplash.com/photo-1598488035139-bdbb2231ce04?w=800&q=80",
                durationSeconds = 1240, // 20m 40s
                hashtags = "#audiotech #youtube #creators #gear",
                likesCount = 4210,
                commentsCount = 345,
                repostsCount = 180,
                isLiked = false
            ),
            PostEntity(
                authorName = "Marcus Aurelius",
                authorHandle = "@marcus_fit",
                authorAvatarUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400&q=80",
                postType = "VIDEO_REEL",
                textContent = "Morning focus workout ⚡ 4 steps to elevate creative stamina. Let's crush today's sprint! 🔥",
                mediaUrl = "https://images.unsplash.com/photo-1517836357463-d25dfe09ce14?w=800&q=80",
                durationSeconds = 15,
                hashtags = "#growth #motivation #calisthenics #shorts",
                likesCount = 9812,
                commentsCount = 612,
                repostsCount = 1109,
                isLiked = false
            ),
            PostEntity(
                authorName = "Aura Moderation Bot",
                authorHandle = "@aura_safety",
                authorAvatarUrl = "https://images.unsplash.com/photo-1473830394358-9159871542f7?w=400&q=80",
                postType = "TEXT",
                textContent = "Automatic Security Alert: Aura is powered by end-to-end biometric signature encryption. Your messages, calls, and files are protected securely from unauthorized snooping. Enable parental safety controls in the Security panel! 🔒🛡️",
                hashtags = "#safety #encryption #cybersecurity",
                likesCount = 2050,
                commentsCount = 12,
                repostsCount = 412,
                isLiked = false
            )
        )

        for (post in samplePosts) {
            val pId = postDao.insertPost(post)
            // Add some comments
            commentDao.insertComment(CommentEntity(postId = pId, authorName = "Julian Vance", authorHandle = "@julian_lens", content = "Loving the contrast in this! Well done!"))
            commentDao.insertComment(CommentEntity(postId = pId, authorName = "Elena Rostova", authorHandle = "@elena_sound", content = "This encryption standard is incredible. Seamless!"))
        }

        // 3. Insert Chats & Messages (DMs & Group Conversations)
        val conversations = listOf(
            // Team Chatroom
            MessageEntity(
                chatRoomId = "group_aura_builders",
                senderId = "sophia_id",
                senderName = "Sophia Drake",
                senderHandle = "@sophia_dir",
                content = "Team, the custom shader for Amber Ember looks jaw-dropping. Have we completed the QR code renderer?",
                isEncrypted = true,
                isGroupChat = true,
                groupName = "Aura Core Devs Group"
            ),
            MessageEntity(
                chatRoomId = "group_aura_builders",
                senderId = "aura_user_99a", // Me
                senderName = "Alex Mercer",
                senderHandle = "alex_aura",
                content = "Yes, Sophia! I built a direct custom drawn QR code generator using the Compose Canvas. It works 100% offline and generates dynamic links instantly.",
                isEncrypted = true,
                isFromMe = true,
                isGroupChat = true,
                groupName = "Aura Core Devs Group"
            ),
            MessageEntity(
                chatRoomId = "group_aura_builders",
                senderId = "julian_id",
                senderName = "Julian Vance",
                senderHandle = "@julian_lens",
                content = "Stellar work. I added some mock analytics so premium creators can track subscriptions and livestream viewer tips in real time.",
                isEncrypted = true,
                isGroupChat = true,
                groupName = "Aura Core Devs Group"
            ),

            // Direct Chat with Sophia
            MessageEntity(
                chatRoomId = "dm_sophia",
                senderId = "sophia_id",
                senderName = "Sophia Drake",
                senderHandle = "@sophia_dir",
                content = "Hey Alex, are we hopping on a secure video call to review the monetize features?",
                isEncrypted = true
            ),
            MessageEntity(
                chatRoomId = "dm_sophia",
                senderId = "aura_user_99a", // Me
                senderName = "Alex Mercer",
                senderHandle = "alex_aura",
                content = "Sure! Let me trigger the E2E encrypted voice/video calling interface. Ready whenever you are.",
                isEncrypted = true,
                isFromMe = true
            ),

            // Direct Chat with Julian
            MessageEntity(
                chatRoomId = "dm_julian",
                senderId = "julian_id",
                senderName = "Julian Vance",
                senderHandle = "@julian_lens",
                content = "Hey, check out the new dark mode color accents, they are blazing clean.",
                isEncrypted = true
            )
        )

        for (msg in conversations) {
            messageDao.insertMessage(msg)
        }
    }
}
