package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uniqueId: String,
    val name: String,
    val handle: String,
    val bio: String,
    val avatarUrl: String = "",
    val qrCodeSeed: String = "",
    val isPremium: Boolean = false,
    val membershipTier: String = "FREE", // "FREE", "GOLD", "OBSIDIAN"
    val followersCount: Int = 1250,
    val followingCount: Int = 340,
    val subscriberCount: Int = 0,
    val totalEarnings: Double = 0.0,
    val ratingScore: Float = 4.9f
) : Serializable

@Entity(tableName = "posts")
data class PostEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val authorName: String,
    val authorHandle: String,
    val authorAvatarUrl: String = "",
    val postType: String, // "TEXT" (Twitter/X style), "IMAGE" (Instagram style), "VIDEO_REEL" (Reel/TikTok), "VIDEO_LONG" (YouTube style)
    val textContent: String,
    val mediaUrl: String = "", // Resource name or mock URL
    val timestamp: Long = System.currentTimeMillis(),
    val likesCount: Int = 0,
    val isLiked: Boolean = false,
    val commentsCount: Int = 0,
    val repostsCount: Int = 0,
    val hashtags: String = "", // e.g., "#future #ai #tech"
    val communityName: String = "", // e.g., "AI Builders", "Crypto Talk"
    var moderationStatus: String = "APPROVED", // "APPROVED", "PENDING_REVIEW", "FLAGGED"
    val durationSeconds: Int = 0 // For video posts
) : Serializable

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val chatRoomId: String, // e.g. "dm_john" or "group_tech_builders"
    val senderId: String,
    val senderName: String,
    val senderHandle: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isEncrypted: Boolean = true,
    val isFromMe: Boolean = false,
    val isGroupChat: Boolean = false,
    val groupName: String = "",
    val readStatus: String = "READ" // "SENT", "DELIVERED", "READ"
) : Serializable

@Entity(tableName = "comments")
data class CommentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val postId: Long,
    val authorName: String,
    val authorHandle: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
) : Serializable
