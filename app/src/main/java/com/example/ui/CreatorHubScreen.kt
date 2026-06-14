package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.SocialViewModel
import kotlinx.coroutines.launch

@Composable
fun CreatorHubScreen(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isLive by viewModel.isLiveStreaming.collectAsStateWithLifecycle()
    val viewersCount by viewModel.liveViewerCount.collectAsStateWithLifecycle()
    val heartsCount by viewModel.liveHeartsCount.collectAsStateWithLifecycle()
    val comments = viewModel.liveComments

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to the latest live comment automatically
    LaunchedEffect(comments.size) {
        if (comments.isNotEmpty()) {
            listState.animateScrollToItem(comments.size - 1)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Header Creator Title
            CreatorTitleSection()

            // Main UI Split: Live Panel vs Metrics panel
            if (isLive) {
                // ACTIVE BROADCAST STREAM INTERACTION INTERFACE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.Black)
                        .border(1.5.dp, primaryColor, RoundedCornerShape(24.dp))
                ) {
                    // Simulated Live View Cam (Abstract dark scanner)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(primaryColor.copy(alpha = 0.15f), Color.Black)
                                )
                            )
                    )

                    // TOP OVERLAY BADGES
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .background(Color.Red, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("LIVE", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Black)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.RemoveRedEye, contentDescription = "Viewers", tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("$viewersCount", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        IconButton(
                            onClick = { viewModel.toggleLiveStream() },
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                .size(36.dp)
                                .testTag("stop_stream_top_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close stream", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    // BOTTOM LIVE COMMENTS RUNNING WINDOW
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                                )
                            )
                            .padding(16.dp)
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(comments) { index, comment ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "${comment.first}: ",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = primaryColor
                                    )
                                    Text(
                                        text = comment.second,
                                        fontSize = 12.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interaction controls: Send simulator tip / Shoot hearts
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.sendDonationTip(25.0) },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tip_25_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.MonetizationOn, contentDescription = "Tip", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tip $25", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }

                            Button(
                                onClick = { viewModel.sendDonationTip(100.0) },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("tip_100_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = "SuperTip", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Tip $100", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Hearts button
                            IconButton(
                                onClick = { viewModel.sendLiveHeart() },
                                modifier = Modifier
                                    .background(primaryColor, CircleShape)
                                    .size(40.dp)
                                    .testTag("stream_heart_icon")
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = "Heart ticker", tint = Color.Black, modifier = Modifier.size(20.dp))
                            }
                            Text(
                                text = "x$heartsCount",
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            } else {
                // CREATORS OFFLINE HUB - OVERVIEW + METRICS
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    
                    // Standby Live prompt
                    item {
                        OfflineStandbyCard(
                            onStartLive = { viewModel.toggleLiveStream() }
                        )
                    }

                    // Metrics cards Row
                    item {
                        CreatorAnalyticsRow(currentUser?.totalEarnings ?: 0.0, currentUser?.subscriberCount ?: 0)
                    }

                    // Simulated Monthly Earnings Detailed breakdown
                    item {
                        CreatorMonthlyRevenueBreakdownCard(currentUser?.totalEarnings ?: 0.0)
                    }

                    // Active Premium Content Subscriptions List
                    item {
                        ActivePremiumSubscriptionsList()
                    }

                    // Monitizations features list
                    item {
                        CreatorFeaturesSection()
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorTitleSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Campaign, contentDescription = "Creator Hub", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Creator Central", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            "Monetize subscriber followings with ad splits, memberships, and real-time active broadcasting tips.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun OfflineStandbyCard(onStartLive: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Aura broadcast module",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Distribute real-time voice, video stream overlays plus active developer discussion arrays globally.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Button(
                onClick = onStartLive,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("go_live_toggle_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Videocam, contentDescription = "Go Live", tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Go Live", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }
    }
}

@Composable
fun CreatorAnalyticsRow(earnings: Double, subs: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Earnings box
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Default.Timeline, contentDescription = "Revenue", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("Month Earnings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Text(
                    text = "$${String.format("%.2f", earnings)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("99% payouts approved", fontSize = 9.sp, color = Color(0xFF00FF66))
            }
        }
 
        // Subscriptions box
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Default.Groups, contentDescription = "Subscribers", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text("Golden Circle Members", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                Text(
                    text = "$subs",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text("Avg $10M streaming clicks", fontSize = 9.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
fun CreatorFeaturesSection() {
    val items = listOf(
        Triple("Golden Circle Subscriptions", "Charge monthly recurring entry fees to exclusive text/image content caches.", Icons.Default.Stars),
        Triple("Video Ads Revenue Sharing", "Qualify for 65/35 advertising visual revenue overlays on long YouTube style logs.", Icons.Default.MonetizationOn),
        Triple("Direct Tipping Portal", "Collect instantaneous tip distributions in voice call or stream broadcasts.", Icons.Default.Celebration),
        Triple("Parental Filter Excluded Tier", "Gain authorization to post unrestricted adult discussions securely.", Icons.Default.NoEncryption)
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Active Tapping Utilities", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
        ) {
            Column(modifier = Modifier.padding(8.dp)) {
                items.forEach { entry ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(entry.third, contentDescription = entry.first, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(entry.first, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(entry.second, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

data class PremiumSub(
    val id: String,
    val name: String,
    val handle: String,
    val planName: String,
    val price: String,
    val dateRenewal: String,
    val status: String, // "ACTIVE", "RENEWING", "PAUSED"
    val avatarColor: Color
)

@Composable
fun CreatorMonthlyRevenueBreakdownCard(totalEarned: Double) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    
    // Simulate estimated splits based on total earnings
    val subSplit = totalEarned * 0.65
    val tipsSplit = totalEarned * 0.25
    val adsSplit = totalEarned * 0.10

    Card(
        modifier = Modifier.fillMaxWidth().testTag("monthly_earnings_breakdown"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PieChart, contentDescription = "Breakdown", tint = primaryColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Est. June Revenue Channels", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Text("Simulated", fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = primaryColor, modifier = Modifier.border(0.5.dp, primaryColor, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp))
            }
            Text("Breakdown of this month's incoming developer flow channels.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.padding(top = 2.dp, bottom = 16.dp))

            // Subscriptions Split
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("✨ Golden Circle Subs (65%)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("$${String.format("%.2f", subSplit)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { 0.65f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = primaryColor,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tipping Split
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🎙️ Call/Live Tipping (25%)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("$${String.format("%.2f", tipsSplit)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { 0.25f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = secondaryColor,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Advertising Split
            Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("📺 Video Ad Shares (10%)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("$${String.format("%.2f", adsSplit)}", fontSize = 11.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { 0.10f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
fun ActivePremiumSubscriptionsList() {
    val context = androidx.compose.ui.platform.LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary

    // Simulated list of subscribers for premium content
    val subList = remember {
        mutableStateListOf(
            PremiumSub("s1", "Julian Vance", "@julian_lens", "Golden Circle Premium", "$9.99/mo", "June 28, 2026", "ACTIVE", Color(0xFF9C27B0)),
            PremiumSub("s2", "Elena Rostova", "@elena_sound", "Super Creator Tier", "$14.99/mo", "July 02, 2026", "RENEWING", Color(0xFF00BCD4)),
            PremiumSub("s3", "Marcus Cole", "@marcus_dev", "Golden Circle Premium", "$9.99/mo", "June 25, 2026", "ACTIVE", Color(0xFFFF9800)),
            PremiumSub("s4", "Sofia Chen", "@sofia_code", "Super Creator Tier", "$14.99/mo", "July 04, 2026", "ACTIVE", Color(0xFF4CAF50)),
            PremiumSub("s5", "Lucas Web3", "@lucas_chain", "Exclusive Audio Tier", "$4.99/mo", "June 29, 2026", "PAUSED", Color(0xFF607D8B))
        )
    }

    var selectedStatusFilter by remember { mutableStateOf("ALL") }

    val filteredList = remember(selectedStatusFilter, subList) {
        if (selectedStatusFilter == "ALL") subList else subList.filter { it.status == selectedStatusFilter }
    }

    Card(
        modifier = Modifier.fillMaxWidth().testTag("active_subscriptions_section"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Stars, contentDescription = "Subs", tint = Color(0xFFFFD700), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Premium Circle Members", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                // Subscription count badge
                Box(
                    modifier = Modifier.background(primaryColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text("${subList.size} Total", fontSize = 10.sp, fontWeight = FontWeight.Black, color = primaryColor)
                }
            }
            Text("Subscribers paying monthly membership fees for your private logs, preset bundles, and audio feeds.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.padding(top = 2.dp, bottom = 12.dp))

            // Quick Filters
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("ALL", "ACTIVE", "RENEWING", "PAUSED").forEach { filter ->
                    val isSelected = selectedStatusFilter == filter
                    val chipColor = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)
                    val textColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                    Box(
                        modifier = Modifier
                            .background(chipColor, RoundedCornerShape(8.dp))
                            .clickable { selectedStatusFilter = filter }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(filter, fontSize = 9.sp, fontWeight = FontWeight.Black, color = textColor)
                    }
                }
            }

            // Interactive list rows
            if (filteredList.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
                    Text("No matching subscribers in this segment.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    filteredList.forEach { subscriber ->
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("subscriber_row_${subscriber.id}"),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.02f)),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Custom Avatar
                                    Box(
                                        modifier = Modifier.size(36.dp).background(subscriber.avatarColor.copy(alpha = 0.25f), CircleShape).border(1.dp, subscriber.avatarColor.copy(alpha = 0.8f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(subscriber.name.take(1), fontWeight = FontWeight.Black, color = subscriber.avatarColor, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(subscriber.name, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            // Status Badge
                                            val badgeBg = when (subscriber.status) {
                                                "ACTIVE" -> Color(0xFF00FF66).copy(alpha = 0.15f)
                                                "RENEWING" -> Color(0xFF00BCD4).copy(alpha = 0.15f)
                                                else -> Color.LightGray.copy(alpha = 0.15f)
                                            }
                                            val badgeText = when (subscriber.status) {
                                                "ACTIVE" -> Color(0xFF00FF66)
                                                "RENEWING" -> Color(0xFF00BCD4)
                                                else -> Color.Gray
                                            }
                                            Box(modifier = Modifier.background(badgeBg, RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                                Text(subscriber.status, fontSize = 8.sp, fontWeight = FontWeight.Black, color = badgeText)
                                            }
                                        }
                                        Text("${subscriber.handle} • ${subscriber.planName}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Billing Renewal", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                        Text("${subscriber.dateRenewal} • ${subscriber.price}", fontSize = 10.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
                                    }

                                    // Quick interactive actions
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        TextButton(
                                            onClick = {
                                                android.widget.Toast.makeText(context, "Promo 1-Month Free applied to ${subscriber.name}", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.textButtonColors(contentColor = primaryColor),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp).testTag("subscriber_action_gift_${subscriber.id}")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.CardGiftcard, contentDescription = "", modifier = Modifier.size(11.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("Gift Free", fontSize = 9.sp, fontWeight = FontWeight.Black)
                                            }
                                        }

                                        Button(
                                            onClick = {
                                                android.widget.Toast.makeText(context, "Relayed secure cryptolink DM with ${subscriber.name}", android.widget.Toast.LENGTH_SHORT).show()
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f), contentColor = MaterialTheme.colorScheme.onSurface),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp).testTag("subscriber_action_dm_${subscriber.id}")
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.ChatBubbleOutline, contentDescription = "", modifier = Modifier.size(11.dp))
                                                Spacer(modifier = Modifier.width(2.dp))
                                                Text("Chat", fontSize = 9.sp, fontWeight = FontWeight.Black)
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
}
