package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PostDao {
    @Query("SELECT * FROM posts WHERE moderationStatus != 'FLAGGED' ORDER BY timestamp DESC")
    fun getAllApprovedPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE postType = :type AND moderationStatus != 'FLAGGED' ORDER BY timestamp DESC")
    fun getPostsByType(type: String): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts WHERE moderationStatus = 'FLAGGED' ORDER BY timestamp DESC")
    fun getFlaggedPosts(): Flow<List<PostEntity>>

    @Query("SELECT * FROM posts ORDER BY timestamp DESC")
    fun getAllPostsRaw(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPost(post: PostEntity): Long

    @Update
    suspend fun updatePost(post: PostEntity)

    @Query("SELECT * FROM posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: Long): PostEntity?

    @Delete
    suspend fun deletePost(post: PostEntity)
    
    @Query("UPDATE posts SET likesCount = likesCount + :delta, isLiked = :isLiked WHERE id = :postId")
    suspend fun updatePostLike(postId: Long, delta: Int, isLiked: Boolean)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatRoomId = :roomId ORDER BY timestamp ASC")
    fun getMessagesForRoom(roomId: String): Flow<List<MessageEntity>>

    @Query("SELECT chatRoomId, senderName, senderHandle, content, timestamp, isGroupChat, groupName FROM messages GROUP BY chatRoomId ORDER BY timestamp DESC")
    fun getLatestChatPreviews(): Flow<List<ChatPreview>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: MessageEntity): Long

    @Query("UPDATE messages SET readStatus = :status WHERE id = :msgId")
    suspend fun updateMessageStatus(msgId: Long, status: String)
}

data class ChatPreview(
    val chatRoomId: String,
    val senderName: String,
    val senderHandle: String,
    val content: String,
    val timestamp: Long,
    val isGroupChat: Boolean,
    val groupName: String?
)

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUserFlow(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getCurrentUser(): UserEntity?

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE users SET isPremium = :isPremium, membershipTier = :tier WHERE uniqueId = :userId")
    suspend fun updateMembership(userId: String, isPremium: Boolean, tier: String)

    @Query("UPDATE users SET totalEarnings = totalEarnings + :amount")
    suspend fun addEarnings(amount: Double)
}

@Dao
interface CommentDao {
    @Query("SELECT * FROM comments WHERE postId = :postId ORDER BY timestamp ASC")
    fun getCommentsForPost(postId: Long): Flow<List<CommentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: CommentEntity)
}
