package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import kotlinx.coroutines.delay
import androidx.compose.animation.core.*
import androidx.compose.ui.text.font.FontFamily
import com.example.data.CommentEntity
import com.example.data.PostEntity
import com.example.viewmodel.HomeFeedTab
import com.example.viewmodel.SocialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val posts by viewModel.allPosts.collectAsStateWithLifecycle()
    val rawPostsCount = viewModel.rawPosts.collectAsStateWithLifecycle().value.size
    val currentTab by viewModel.homeFeedTab.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeTag by viewModel.activeHashtagFilter.collectAsStateWithLifecycle()
    val activeCommunity by viewModel.activeCommunityFilter.collectAsStateWithLifecycle()
    val discoveryCat by viewModel.discoveryCategory.collectAsStateWithLifecycle()
    val isFeedLoading by viewModel.isFeedLoading.collectAsStateWithLifecycle()

    var showPublishDialog by remember { mutableStateOf(false) }
    var selectedCommentsPostId by remember { mutableStateOf<Long?>(null) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // 1. Sleek Search Header with Accents
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchPosts(it) },
                    placeholder = { 
                        Text(
                            text = "Search Aura or hashtags...", 
                            fontSize = 14.sp,
                            color = onSurfaceColor.copy(alpha = 0.5f)
                        ) 
                    },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Search, 
                            contentDescription = "Search", 
                            tint = primaryColor 
                        ) 
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty() || activeTag != null || activeCommunity != null) {
                            IconButton(onClick = { 
                                viewModel.searchPosts("")
                                viewModel.setHashtagFilter(null)
                                viewModel.setCommunityFilter(null)
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Filters")
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("search_posts_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = primaryColor,
                        unfocusedIndicatorColor = onSurfaceColor.copy(alpha = 0.2f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.width(8.dp))

                // Publish Action FAB-Trigger
                Button(
                    onClick = { showPublishDialog = true },
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("add_post_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add, 
                        contentDescription = "Publish Post", 
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Active Filters Banner (If present)
            if (activeTag != null || activeCommunity != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val label = activeTag ?: ("Group: $activeCommunity")
                    AssistChip(
                        onClick = {},
                        label = { Text(label, fontWeight = FontWeight.Bold, color = primaryColor) },
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close, 
                                contentDescription = "Clear",
                                modifier = Modifier
                                    .size(16.dp)
                                    .clickable {
                                        viewModel.setHashtagFilter(null)
                                        viewModel.setCommunityFilter(null)
                                    }
                            )
                        }
                    )
                }
            }

            if (searchQuery.isNotEmpty()) {
                val creators = remember {
                    listOf(
                        Pair("Sofia Chen", "@sofia_code"),
                        Pair("Julian Vance", "@julian_lens"),
                        Pair("Elena Rostova", "@elena_sound"),
                        Pair("Marcus Aurelius", "@marcus_fit"),
                        Pair("Aura Moderation Bot", "@aura_safety")
                    )
                }
                val matchedCreators = remember(searchQuery) {
                    creators.filter {
                        it.first.contains(searchQuery, ignoreCase = true) ||
                        it.second.contains(searchQuery, ignoreCase = true)
                    }
                }
                val allTags = remember {
                    listOf("#auraconnect", "#photography", "#dev", "#crypto", "#design", "#metaspheres", "#3dartist", "#audiotech", "#youtube", "#creators", "#gear", "#growth", "#motivation", "#calisthenics", "#shorts", "#safety")
                }
                val matchedTags = remember(searchQuery) {
                    allTags.filter { it.contains(searchQuery, ignoreCase = true) }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("search_results_container"),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Matched Creator Nodes Section
                    if (matchedCreators.isNotEmpty()) {
                        Column {
                            Text(
                                text = "MATCHED CREATOR SECURE NODES",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = primaryColor,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(matchedCreators) { creator ->
                                    Card(
                                        modifier = Modifier
                                            .width(160.dp)
                                            .clickable {
                                                viewModel.openChatRoom(creator.second, creator.first, false)
                                                viewModel.selectScreen(com.example.viewmodel.ActiveScreen.MESSENGER)
                                            }
                                            .testTag("search_creator_${creator.second.removePrefix("@")}"),
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            AuraAvatar(
                                                name = creator.first,
                                                avatarUrl = "", // displays initials
                                                sizeDp = 44,
                                                fontSizeSp = 16
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(creator.first, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(creator.second, fontSize = 10.sp, color = primaryColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Row(
                                                modifier = Modifier
                                                    .background(primaryColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Default.Send, contentDescription = null, tint = primaryColor, modifier = Modifier.size(10.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Secure Chat", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 2. Matched Topics / Trends Section
                    if (matchedTags.isNotEmpty()) {
                        Column {
                            Text(
                                text = "MATCHED HASHTAG TOPICS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                matchedTags.take(5).forEach { tag ->
                                    AssistChip(
                                        onClick = {
                                            viewModel.setHashtagFilter(tag)
                                            viewModel.searchPosts("")
                                        },
                                        label = { Text(tag, fontSize = 10.sp, fontWeight = FontWeight.SemiBold) },
                                        colors = AssistChipDefaults.assistChipColors(labelColor = MaterialTheme.colorScheme.secondary),
                                        modifier = Modifier.testTag("search_topic_${tag.removePrefix("#")}")
                                    )
                                }
                            }
                        }
                    }

                    // 3. Matched Posts List
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MATCHED FLOW FEED UPDATES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        val postsMatchingQuery = posts.filter { post ->
                            post.textContent.contains(searchQuery, ignoreCase = true) ||
                            post.authorName.contains(searchQuery, ignoreCase = true) ||
                            post.hashtags.contains(searchQuery, ignoreCase = true)
                        }

                        if (postsMatchingQuery.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No matches found in communication nodes.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(bottom = 80.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(postsMatchingQuery, key = { it.id }) { post ->
                                    PostCard(
                                        post = post,
                                        isCurrentUserPremium = currentUser?.isPremium ?: false,
                                        onLike = { viewModel.toggleLikePost(post.id, post.isLiked) },
                                        onCommentClick = { selectedCommentsPostId = post.id },
                                        onShare = { viewModel.sharePost(post) },
                                        onHashtagClick = { tag ->
                                            viewModel.setHashtagFilter(tag)
                                            viewModel.searchPosts("")
                                        },
                                        onCommunityClick = { comm ->
                                            viewModel.setCommunityFilter(comm)
                                            viewModel.searchPosts("")
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // --- Discovery Category Section ('Following', 'Trending', 'For You') ---
                DiscoveryCategoryTabRow(
                    selectedCategory = discoveryCat,
                    onCategorySelected = { viewModel.selectDiscoveryCategory(it) }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 2. Active Stories Feed (Gradient Borders)
                StoriesRow(onStoryClicked = { name, handle ->
                    viewModel.triggerSimulatedCall(name, handle, isVideo = true, incoming = true)
                })

                // 3. Category Horizontal Tab List (X text, Insta image, YT video, Reels short)
                FeedTabSelector(
                    selectedTab = currentTab, 
                    onTabSelected = { viewModel.selectFeedTab(it) }
                )

                // 4. Infinite Content List with Dynamic Cards
                val filteredPosts = remember(posts, currentTab, discoveryCat) {
                    posts.filter { post ->
                        val matchesType = when (currentTab) {
                            HomeFeedTab.ALL -> true
                            HomeFeedTab.TEXT_X -> post.postType == "TEXT"
                            HomeFeedTab.IMAGE_INSTA -> post.postType == "IMAGE"
                            HomeFeedTab.VIDEO_YOUTUBE -> post.postType == "VIDEO_LONG"
                            HomeFeedTab.REELS_SHORTS -> post.postType == "VIDEO_REEL"
                        }

                        val matchesDiscovery = when (discoveryCat) {
                            com.example.viewmodel.DiscoveryCategory.FOR_YOU -> true
                            com.example.viewmodel.DiscoveryCategory.FOLLOWING -> {
                                // Filter posts by community accounts or premium users
                                post.authorHandle.contains("sofia") || 
                                post.authorHandle.contains("julian") || 
                                post.authorHandle.contains("elena") || 
                                post.authorHandle.contains("safety") ||
                                post.authorName.contains("Bot") ||
                                post.authorHandle.contains("marcus")
                            }
                            com.example.viewmodel.DiscoveryCategory.TRENDING -> {
                                // Match trending triggers (high likes, content topics, hashtags)
                                post.likesCount > 1000 || 
                                post.hashtags.contains("auraconnect") || 
                                post.hashtags.contains("photography") || 
                                post.hashtags.contains("grows") ||
                                post.hashtags.contains("motivation")
                            }
                        }

                        matchesType && matchesDiscovery
                    }
                }

                if (isFeedLoading) {
                    FeedSkeletonLoading(modifier = Modifier.weight(1f))
                } else if (filteredPosts.isEmpty()) {
                    EmptyStateLayout()
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredPosts, key = { it.id }) { post ->
                            PostCard(
                                post = post,
                                isCurrentUserPremium = currentUser?.isPremium ?: false,
                                onLike = { viewModel.toggleLikePost(post.id, post.isLiked) },
                                onCommentClick = { selectedCommentsPostId = post.id },
                                onShare = { viewModel.sharePost(post) },
                                onHashtagClick = { tag -> viewModel.setHashtagFilter(tag) },
                                onCommunityClick = { comm -> viewModel.setCommunityFilter(comm) }
                            )
                        }
                    }
                }
            }
        }

        // 5. Publisher Dialog Modal
        if (showPublishDialog) {
            PublishPostDialog(
                onDismiss = { showPublishDialog = false },
                onPublish = { text, type, mediaDesc, tags, community ->
                    viewModel.publishPost(text, type, mediaDesc, tags, community)
                    showPublishDialog = false
                }
            )
        }

        // 6. Comments Section Modal
        selectedCommentsPostId?.let { postId ->
            CommentsDialog(
                postId = postId,
                viewModel = viewModel,
                onDismiss = { selectedCommentsPostId = null }
            )
        }
    }
}

@Composable
fun StoriesRow(onStoryClicked: (String, String) -> Unit) {
    val stories = remember {
        listOf(
            Triple("Sophia Chen", "@sofia_code", Color(0xFFDA70D6)),
            Triple("Julian Vance", "@julian_lens", Color(0xFFF2B8B5)),
            Triple("Elena Rostova", "@elena_sound", Color(0xFF00C9A7)),
            Triple("Marcus Aurelius", "@marcus_fit", Color(0xFFFF5E62)),
            Triple("Lucas Web3", "@lucas_dapp", Color(0xFF00F3FF)),
            Triple("Zephyr Tech", "@zephyr_ai", Color(0xFFDA70D6))
        )
    }

    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = "Creators Active Now",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(stories) { story ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onStoryClicked(story.first, story.second) }
                        .testTag("story_item_${story.second}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .border(
                                width = 2.dp,
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        story.third,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                shape = CircleShape
                            )
                            .padding(3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(story.third.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = story.first.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            )
                        }
                        // Active status green light
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color(0xFF00FF66), shape = CircleShape)
                                .border(1.5.dp, MaterialTheme.colorScheme.background, shape = CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = story.first.split(" ")[0],
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(62.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FeedTabSelector(
    selectedTab: HomeFeedTab,
    onTabSelected: (HomeFeedTab) -> Unit
) {
    val tabs = remember { HomeFeedTab.values() }

    ScrollableTabRow(
        selectedTabIndex = selectedTab.ordinal,
        edgePadding = 16.dp,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {},
        indicator = { tabPositions ->
            if (selectedTab.ordinal < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 2.dp
                )
            }
        },
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
    ) {
        tabs.forEach { tab ->
            val (label, icon) = when (tab) {
                HomeFeedTab.ALL -> "Global Flow" to Icons.Default.AllInclusive
                HomeFeedTab.TEXT_X -> "Trending X" to Icons.Default.AlternateEmail
                HomeFeedTab.IMAGE_INSTA -> "Spotlight Duo" to Icons.Default.PhotoLibrary
                HomeFeedTab.VIDEO_YOUTUBE -> "Broadcasting" to Icons.Default.Tv
                HomeFeedTab.REELS_SHORTS -> "Reels/Shorts" to Icons.Default.SlowMotionVideo
            }
            Tab(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(16.dp)) },
                text = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                modifier = Modifier.testTag("feed_tab_${tab.name}")
            )
        }
    }
}

@Composable
fun PostCard(
    post: PostEntity,
    isCurrentUserPremium: Boolean,
    onLike: () -> Unit,
    onCommentClick: () -> Unit,
    onShare: () -> Unit,
    onHashtagClick: (String) -> Unit,
    onCommunityClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: User credentials + Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                AuraAvatar(
                    name = post.authorName,
                    avatarUrl = post.authorAvatarUrl,
                    sizeDp = 40,
                    fontSizeSp = 14,
                    borderGlowColors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = post.authorName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        // Premium User visual indication
                        if (post.authorHandle.contains("aura") || post.authorName.contains("Bot") || post.authorName.contains("Sofia")) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = "Obsidian Premium Tier",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = post.authorHandle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                // Community Pill if exists
                if (post.communityName.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .clickable { onCommunityClick(post.communityName) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = post.communityName,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body: Content text
            Text(
                text = post.textContent,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Hashtags (Clickable)
            if (post.hashtags.isNotEmpty()) {
                val hashtagList = remember(post.hashtags) {
                    post.hashtags.split(" ").filter { it.startsWith("#") && it.isNotEmpty() }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    hashtagList.forEach { hashtag ->
                        Text(
                            text = hashtag,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onHashtagClick(hashtag) }
                        )
                    }
                }
            }

            // Attached Media Preview (If media is tagged)
            if (post.mediaUrl.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                if (post.postType == "VIDEO_REEL") {
                    ReelsVideoPlayer(post = post, onLike = onLike)
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                Brush.sweepGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = when (post.postType) {
                                    "VIDEO_REEL" -> Icons.Default.SlowMotionVideo
                                    "VIDEO_LONG" -> Icons.Default.PlayCircleOutline
                                    else -> Icons.Default.Image
                                },
                                contentDescription = "Attachment preview",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val label = when (post.postType) {
                                "VIDEO_REEL" -> "Interactive Reel Preview (0:15)"
                                "VIDEO_LONG" -> "Aura Video Player: ${post.durationSeconds / 60}m ${post.durationSeconds % 60}s"
                                else -> "Visual Gallery Artboard"
                            }
                            Text(
                                text = label, 
                                fontSize = 12.sp, 
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))

            // Footer controls: Likes, Comments, Shares
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                // Like Button with Heart Animation & Local State Toggle
                AnimatedHeartLikeButton(
                    isLikedInitial = post.isLiked,
                    likesCount = post.likesCount,
                    onLikeToggle = { onLike() },
                    textColor = MaterialTheme.colorScheme.onSurface,
                    testTag = "like_btn_${post.id}"
                )

                // Comment Button
                IconButtonWithLabel(
                    icon = Icons.Outlined.ChatBubbleOutline,
                    label = post.commentsCount.toString(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    onClick = onCommentClick,
                    testTag = "comment_btn_${post.id}"
                )

                // Repost button
                IconButtonWithLabel(
                    icon = Icons.Outlined.Repeat,
                    label = post.repostsCount.toString(),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    onClick = onShare,
                    testTag = "repost_btn_${post.id}"
                )
            }
        }
    }
}

@Composable
fun IconButtonWithLabel(
    imageVector: ImageVector? = null,
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = imageVector ?: icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun EmptyStateLayout() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Feed,
                contentDescription = "Empty Feed",
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "No updates match",
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Try clearing search parameter filters or tags to reveal other streams.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishPostDialog(
    onDismiss: () -> Unit,
    onPublish: (String, String, String, String, String) -> Unit
) {
    var contentText by remember { mutableStateOf("") }
    var selectType by remember { mutableStateOf("TEXT") } // "TEXT", "IMAGE", "VIDEO_LONG", "VIDEO_REEL"
    var attachmentsDesc by remember { mutableStateOf("") }
    var hashtagsText by remember { mutableStateOf("") }
    var communitySelected by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Publish New Update", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Formatting channel selectors
                Text("Content Stream Classification", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val types = listOf("TEXT" to "X Update", "IMAGE" to "Insta Duo", "VIDEO_LONG" to "YouTube", "VIDEO_REEL" to "Reel")
                    types.forEach { pair ->
                        FilterChip(
                            selected = selectType == pair.first,
                            onClick = { 
                                selectType = pair.first 
                                attachmentsDesc = if (pair.first != "TEXT") "mock_media" else ""
                            },
                            label = { Text(pair.second, fontSize = 10.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                OutlinedTextField(
                    value = contentText,
                    onValueChange = { contentText = it },
                    placeholder = { Text("State your creative thoughts here...", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("publish_input_text"),
                    singleLine = false
                )

                OutlinedTextField(
                    value = hashtagsText,
                    onValueChange = { hashtagsText = it },
                    placeholder = { Text("Hashtags: #tech #design #sound", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("publish_input_tags"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = communitySelected,
                    onValueChange = { communitySelected = it },
                    placeholder = { Text("Target community: (Optional e.g. Design Block)", fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("publish_input_community"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onPublish(contentText, selectType, attachmentsDesc, hashtagsText, communitySelected) },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.testTag("submit_publish_btn")
            ) {
                Text("Distribute", color = MaterialTheme.colorScheme.onPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsDialog(
    postId: Long,
    viewModel: SocialViewModel,
    onDismiss: () -> Unit
) {
    val comments by viewModel.getCommentsForPostFlow(postId).collectAsStateWithLifecycle(initialValue = emptyList())
    var commentText by remember { mutableStateOf("") }
    val user by viewModel.currentUser.collectAsStateWithLifecycle()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Comments Feed", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .width(400.dp)
                    .height(300.dp)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (comments.isEmpty()) {
                        item {
                            Text(
                                "No remarks yet. Be the first to express feedback!",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                        }
                    } else {
                        items(comments) { comment ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                    .padding(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(comment.authorName, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(comment.authorHandle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(comment.content, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Add comment...", fontSize = 12.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("comment_input_box"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = {
                            viewModel.addComment(
                                postId = postId,
                                text = commentText,
                                profileName = user?.name ?: "Anonymous",
                                profileHandle = user?.handle ?: "anon"
                            )
                            commentText = ""
                        },
                        modifier = Modifier.testTag("comment_submit_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send Comment", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun DiscoveryCategoryTabRow(
    selectedCategory: com.example.viewmodel.DiscoveryCategory,
    onCategorySelected: (com.example.viewmodel.DiscoveryCategory) -> Unit
) {
    val categories = remember { com.example.viewmodel.DiscoveryCategory.values() }

    TabRow(
        selectedTabIndex = selectedCategory.ordinal,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {},
        indicator = { tabPositions ->
            if (selectedCategory.ordinal < tabPositions.size) {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedCategory.ordinal]),
                    color = MaterialTheme.colorScheme.primary,
                    height = 3.dp
                )
            }
        },
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
    ) {
        categories.forEach { cat ->
            val label = when (cat) {
                com.example.viewmodel.DiscoveryCategory.FOLLOWING -> "Following"
                com.example.viewmodel.DiscoveryCategory.TRENDING -> "🔥 Trending"
                com.example.viewmodel.DiscoveryCategory.FOR_YOU -> "✨ For You"
            }
            Tab(
                selected = selectedCategory == cat,
                onClick = { onCategorySelected(cat) },
                text = { Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("discovery_tab_${cat.name}")
            )
        }
    }
}

@Composable
fun ReelsVideoPlayer(
    post: PostEntity,
    onLike: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isMuted by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0f) }
    var showVolumeIndicator by remember { mutableStateOf(false) }
    var showPlayIndicator by remember { mutableStateOf(false) }

    // Auto-play looping coroutine
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            val totalSteps = 100
            val delayMs = ((post.durationSeconds.takeIf { it > 0 } ?: 15) * 1000L) / totalSteps
            while (true) {
                delay(delayMs)
                progress += 0.01f
                if (progress >= 1f) {
                    progress = 0f
                }
            }
        }
    }

    // Auto-clear volume & play indicators
    LaunchedEffect(showVolumeIndicator) {
        if (showVolumeIndicator) {
            delay(1000)
            showVolumeIndicator = false
        }
    }
    LaunchedEffect(showPlayIndicator) {
        if (showPlayIndicator) {
            delay(1000)
            showPlayIndicator = false
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Black)
            .border(
                width = 1.dp,
                color = if (isPlaying) primaryColor.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable {
                isPlaying = !isPlaying
                showPlayIndicator = true
            }
    ) {
        // Aesthetic simulated scanning background lines
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    val lineSpacing = 16.dp.toPx()
                    val lineCount = (size.height / lineSpacing).toInt()
                    for (i in 0..lineCount) {
                        drawLine(
                            color = primaryColor.copy(alpha = 0.03f),
                            start = Offset(0f, i * lineSpacing),
                            end = Offset(size.width, i * lineSpacing),
                            strokeWidth = 1f
                        )
                    }
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            if (isPlaying) primaryColor.copy(alpha = 0.15f) else Color.DarkGray.copy(alpha = 0.1f),
                            Color.Black
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (isPlaying) {
                    // Equalizer bars jumping dynamically
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.height(60.dp)
                    ) {
                        val barCount = 12
                        val transition = rememberInfiniteTransition()
                        for (i in 0 until barCount) {
                            val scale by transition.animateFloat(
                                initialValue = 0.15f,
                                targetValue = 1f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(durationMillis = (400..900).random(), easing = LinearEasing),
                                    repeatMode = RepeatMode.Reverse
                                )
                            )
                            val heightFactor = if (isMuted) 0.05f else scale
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .fillMaxHeight(heightFactor)
                                    .background(
                                        brush = Brush.verticalGradient(
                                            colors = listOf(primaryColor, secondaryColor)
                                        ),
                                        shape = RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Paused",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(56.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isPlaying) "Playing Real-Time Auto-Stream" else "Tap to Resume Stream",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        // Bottom Details Panel Overlay
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(if (isPlaying) Color(0xFF00FF66) else Color.Red, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isPlaying) "HD AUTO-PLAYING" else "PAUSED",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = post.textContent.take(35) + "...",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1
            )
        }

        // Volume Controller Toggle Button
        IconButton(
            onClick = {
                isMuted = !isMuted
                showVolumeIndicator = true
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                .size(36.dp)
                .testTag("mute_unmute_toggle_${post.id}")
        ) {
            Icon(
                imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                contentDescription = if (isMuted) "Unmute" else "Mute",
                tint = primaryColor,
                modifier = Modifier.size(18.dp)
            )
        }

        // Floating action column on right side
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Heart Like icon on the reel itself, customized for a floating, dark UI look
            Box(
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.62f), CircleShape)
                    .border(0.5.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                    .padding(2.dp)
            ) {
                // Customized AnimatedHeartLikeButton to use white text for readability over reel content
                AnimatedHeartLikeButton(
                    isLikedInitial = post.isLiked,
                    likesCount = post.likesCount,
                    onLikeToggle = { onLike() },
                    textColor = Color.White,
                    testTag = "reel_like_btn_${post.id}"
                )
            }
        }

        // Interaction Volume indicator banner
        if (showVolumeIndicator) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.82f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = "",
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isMuted) "MUTED" else "UNMUTED (STEREO)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        if (showPlayIndicator) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.82f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = "",
                            tint = primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isPlaying) "AUTO-PLAY" else "PAUSED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Live real-time stream status progress bar & counters
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.End
            ) {
                val duration = post.durationSeconds.takeIf { it > 0 } ?: 15
                val currentSeconds = (progress * duration).toInt()
                Text(
                    text = "0:${String.format("%02d", currentSeconds)} / 0:${String.format("%02d", duration)}",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = primaryColor,
                trackColor = Color.White.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
fun AnimatedHeartLikeButton(
    isLikedInitial: Boolean,
    likesCount: Int,
    onLikeToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    var isLikedLocal by remember { mutableStateOf(isLikedInitial) }
    var localLikesCount by remember { mutableStateOf(likesCount) }
    
    // Sync with external state changes (e.g. if another button likes/unlikes this post)
    LaunchedEffect(isLikedInitial) {
        if (isLikedLocal != isLikedInitial) {
            isLikedLocal = isLikedInitial
        }
    }
    LaunchedEffect(likesCount) {
        if (localLikesCount != likesCount) {
            localLikesCount = likesCount
        }
    }

    // Scale animation
    var isTapped by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isTapped) 1.5f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "HeartScale"
    )

    LaunchedEffect(isTapped) {
        if (isTapped) {
            delay(150)
            isTapped = false
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                isTapped = true
                val newLikedState = !isLikedLocal
                isLikedLocal = newLikedState
                // Optimistically update count
                if (newLikedState) {
                    localLikesCount++
                } else {
                    localLikesCount = (localLikesCount - 1).coerceAtLeast(0)
                }
                onLikeToggle(newLikedState)
            }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = if (isLikedLocal) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = "Like",
            tint = if (isLikedLocal) Color(0xFFFF2D55) else textColor.copy(alpha = 0.6f),
            modifier = Modifier
                .size(20.dp)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale
                )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = localLikesCount.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.22f),
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(10f, 10f),
        end = Offset(translateAnim.value, translateAnim.value)
    )
}

@Composable
fun SkeletonPlaceholder(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(4.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush())
    )
}

@Composable
fun FeedSkeletonLoading(
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        userScrollEnabled = false
    ) {
        items(3) {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("feed_skeleton_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonPlaceholder(
                            modifier = Modifier.size(46.dp),
                            shape = CircleShape
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            SkeletonPlaceholder(
                                modifier = Modifier.size(width = 120.dp, height = 16.dp)
                            )
                            SkeletonPlaceholder(
                                modifier = Modifier.size(width = 80.dp, height = 12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        SkeletonPlaceholder(
                            modifier = Modifier.size(width = 40.dp, height = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    SkeletonPlaceholder(
                        modifier = Modifier.fillMaxWidth().height(16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SkeletonPlaceholder(
                        modifier = Modifier.fillMaxWidth(0.9f).height(16.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    SkeletonPlaceholder(
                        modifier = Modifier.fillMaxWidth(0.6f).height(16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SkeletonPlaceholder(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        repeat(3) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                SkeletonPlaceholder(
                                    modifier = Modifier.size(20.dp),
                                    shape = CircleShape
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                SkeletonPlaceholder(
                                    modifier = Modifier.size(width = 30.dp, height = 12.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
