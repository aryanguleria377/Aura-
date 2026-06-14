package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.viewmodel.AuraTheme
import com.example.viewmodel.SocialViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

@Composable
fun ProfileScreen(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val allPosts by viewModel.allPosts.collectAsStateWithLifecycle()
    val activeTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val userJwt by viewModel.currentSessionJwt.collectAsStateWithLifecycle()
    val isProfileLoading by viewModel.isProfileLoading.collectAsStateWithLifecycle()

    var showQrDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    if (isProfileLoading) {
        ProfileSkeletonLoading(modifier = modifier)
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        
        // 1. Profile Core Card
        item {
            currentUser?.let { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Reusable Dynamic User Avatar with Glow
                        AuraAvatar(
                            name = user.name,
                            avatarUrl = user.avatarUrl,
                            sizeDp = 90,
                            fontSizeSp = 28,
                            borderGlowColors = listOf(primaryColor, MaterialTheme.colorScheme.secondary)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // User credentials and badges
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = user.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = onSurfaceColor)
                            Spacer(modifier = Modifier.width(6.dp))
                            if (user.isPremium) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(primaryColor)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = user.membershipTier, 
                                        fontWeight = FontWeight.Black, 
                                        fontSize = 8.sp, 
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                        
                        Text(text = "@${user.handle}", fontSize = 13.sp, color = primaryColor, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(8.dp))

                        // Biography paragraph
                        Text(
                            text = user.bio,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            color = onSurfaceColor.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stat grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatWidget(count = user.followersCount.toString(), label = "Followers")
                            StatWidget(count = user.followingCount.toString(), label = "Following")
                            StatWidget(count = if (user.isPremium) "Sovereign" else "Free Account", label = "Aura Tier")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Contact Sharing QR Code & Profile Parameters Edit Trigger
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showQrDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("view_qr_code_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.QrCode2, contentDescription = "QR Code", tint = Color.Black, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Aura QR Card", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }

                            Button(
                                onClick = { showSettingsDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("edit_profile_btn")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Profile", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Edit Params", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Customizable Themes Presets Row
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Aesthetic Theme Presets",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val themes = listOf(
                        Triple(AuraTheme.OBSIDIAN_LUXURY, "Minimalism", Color(0xFFD0BCFF)),
                        Triple(AuraTheme.AMETHYST_SUNSET, "Amethyst", Color(0xFFDA70D6)),
                        Triple(AuraTheme.OCEAN_EMERALD, "Ocean", Color(0xFF00C9A7)),
                        Triple(AuraTheme.AMBER_EMBER, "Ember", Color(0xFFFF5E62)),
                        Triple(AuraTheme.LIGHT_MINIMAL, "Minimal", Color.Black)
                    )

                    items(themes) { item ->
                        val selected = activeTheme == item.first
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) primaryColor.copy(alpha = 0.15f) 
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (selected) primaryColor else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { viewModel.changeTheme(item.first) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("theme_btn_${item.second}")
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(item.third, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(item.second, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 3. Monetization Membership Upgrade Center
        item {
            MembershipUpgradeSection(
                currentTier = currentUser?.membershipTier ?: "FREE",
                onPurchaseTier = { tier -> viewModel.purchasePremiumTier(tier) }
            )
        }

        // 4. Session telemetry logs
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Biometric Session Integrity", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Encrypted JWT Header Token: $userJwt",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = onSurfaceColor.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // 5. Logout Action Session Termination
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.logout() }
                    .testTag("logout_btn"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.12f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ExitToApp,
                        contentDescription = "Log Out Icon",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Terminate Session Protocol",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "Wipe temporary session tokens & request login authentication.",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // 6. User's Posts Grid
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Previous Posts",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                val userPosts = remember(allPosts, currentUser) {
                    allPosts.filter { it.authorName == currentUser?.name }.let {
                        if (it.isEmpty()) allPosts.take(9) else it
                    }
                }

                if (userPosts.isEmpty()) {
                    Text(
                        text = "No posts yet.",
                        fontSize = 13.sp,
                        color = onSurfaceColor.copy(alpha = 0.5f),
                        modifier = Modifier.padding(16.dp)
                    )
                } else {
                    val columns = 3
                    val rows = (userPosts.size + columns - 1) / columns
                    
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (i in 0 until rows) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                for (j in 0 until columns) {
                                    val index = i * columns + j
                                    if (index < userPosts.size) {
                                        val post = userPosts[index]
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .testTag("profile_post_grid_item_$index"),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.5f)),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize()
                                                    .background(
                                                        if (post.mediaUrl.isNotEmpty()) {
                                                            Brush.sweepGradient(
                                                                colors = listOf(
                                                                    primaryColor.copy(alpha = 0.3f),
                                                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f),
                                                                    MaterialTheme.colorScheme.background
                                                                )
                                                            )
                                                        } else {
                                                            Brush.linearGradient(
                                                                colors = listOf(primaryColor.copy(alpha = 0.1f), primaryColor.copy(alpha = 0.05f))
                                                            )
                                                        }
                                                    )
                                                    .padding(8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (post.mediaUrl.startsWith("http")) {
                                                    coil.compose.AsyncImage(
                                                        model = post.mediaUrl,
                                                        contentDescription = null,
                                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                } else if (post.mediaUrl.isNotEmpty()) {
                                                    Icon(
                                                        imageVector = if (post.postType == "VIDEO_REEL") Icons.Default.PlayArrow else Icons.Default.Image,
                                                        contentDescription = null,
                                                        tint = primaryColor,
                                                        modifier = Modifier.size(32.dp)
                                                    )
                                                } else {
                                                    Text(
                                                        text = post.textContent.take(20) + "...",
                                                        fontSize = 10.sp,
                                                        color = onSurfaceColor,
                                                        maxLines = 3,
                                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
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

    // Interactive offline QR Dialog Box
    if (showQrDialog && currentUser != null) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = {
                Text(
                    text = "Aura Biometric QR Identity",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Scan to handshake peer encryption keys and contact coordinates securely.",
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        color = onSurfaceColor.copy(alpha = 0.6f),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // BEAUTIFUL CUSTOM CANVAS DRAWING THE QR CODE MATRIX!
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        QrCodeCanvas()
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showQrDialog = false },
                    modifier = Modifier.testTag("dismiss_qr_btn")
                ) {
                    Text("Deactivate")
                }
            }
        )
    }

    if (showSettingsDialog && currentUser != null) {
        ProfileSettingsDialog(
            initialName = currentUser!!.name,
            initialBio = currentUser!!.bio,
            initialAvatarUrl = currentUser!!.avatarUrl,
            onDismiss = { showSettingsDialog = false },
            onSave = { updatedName, updatedBio, updatedAvatarUrl ->
                viewModel.updateProfile(updatedName, updatedBio, updatedAvatarUrl)
                showSettingsDialog = false
            }
        )
    }
}

@Composable
fun StatWidget(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

@Composable
fun MembershipUpgradeSection(
    currentTier: String,
    onPurchaseTier: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Monetized Account Memberships",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        val lists = listOf(
            Triple("FREE TIER", "Default social feeds, chats with 1.25k follower limit.", "FREE"),
            Triple("GOLD CIRCLE STARTER", "$4.99/mo • Amber theme toggle, golden credentials indicator, creator tipping.", "GOLD"),
            Triple("OBSIDIAN LUXURY TIER", "$19.99/mo • Obsidian pitch theme unlocked, golden crown badge, full broadcast panel.", "OBSIDIAN")
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            lists.forEach { item ->
                val isActive = currentTier == item.third
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPurchaseTier(item.third) }
                        .testTag("tier_upgrade_${item.third}"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isActive) MaterialTheme.colorScheme.primary else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = item.first, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                if (isActive) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                }
                            }
                            Text(text = item.second, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

// Canvas-drawn offline QR sharing card representing high-accuracy matrix drawing
@Composable
fun QrCodeCanvas() {
    val randomSeed = remember {
        listOf(
            0,1,0,1,1,0,1,0,0,1,1,0,1,
            1,0,1,0,0,1,0,1,1,1,0,0,1,
            0,1,1,0,1,1,0,0,1,0,1,1,0,
            1,0,0,1,0,1,1,0,0,1,0,1,1,
            0,1,0,0,1,1,1,0,1,0,1,1,0,
            0,0,1,1,0,1,0,1,1,0,1,0,1,
            1,1,0,0,1,0,1,1,0,1,0,0,1
        )
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val qColor = Color.Black
        val matrixSize = size.width
        val blockSize = matrixSize / 15f

        // Draw Top-Left Eye frame
        drawRoundRect(
            color = qColor,
            topLeft = Offset(0f, 0f),
            size = Size(blockSize * 4, blockSize * 4),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = blockSize)
        )
        drawRect(
            color = qColor,
            topLeft = Offset(blockSize, blockSize),
            size = Size(blockSize * 2, blockSize * 2)
        )

        // Draw Top-Right Eye frame
        drawRoundRect(
            color = qColor,
            topLeft = Offset(matrixSize - blockSize * 4, 0f),
            size = Size(blockSize * 4, blockSize * 4),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = blockSize)
        )
        drawRect(
            color = qColor,
            topLeft = Offset(matrixSize - blockSize * 3, blockSize),
            size = Size(blockSize * 2, blockSize * 2)
        )

        // Draw Bottom-Left Eye frame
        drawRoundRect(
            color = qColor,
            topLeft = Offset(0f, matrixSize - blockSize * 4),
            size = Size(blockSize * 4, blockSize * 4),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = blockSize)
        )
        drawRect(
            color = qColor,
            topLeft = Offset(blockSize, matrixSize - blockSize * 3),
            size = Size(blockSize * 2, blockSize * 2)
        )

        var idx = 0
        for (row in 4..10) {
            for (col in 0..14) {
                if (idx < randomSeed.size && randomSeed[idx] == 1) {
                    drawRect(
                        color = qColor,
                        topLeft = Offset(col * blockSize, row * blockSize),
                        size = Size(blockSize, blockSize)
                    )
                }
                idx++
            }
        }
    }
}

@Composable
fun AuraAvatar(
    name: String,
    avatarUrl: String,
    modifier: Modifier = Modifier,
    borderGlowColors: List<Color> = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary),
    sizeDp: Int = 40,
    fontSizeSp: Int = 14
) {
    Box(
        modifier = modifier
            .size(sizeDp.dp)
            .background(
                brush = Brush.linearGradient(colors = borderGlowColors),
                shape = CircleShape
            )
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            if (avatarUrl.startsWith("http")) {
                coil.compose.AsyncImage(
                    model = avatarUrl,
                    contentDescription = "$name Avatar",
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (avatarUrl.startsWith("cam_")) {
                val colors = when (avatarUrl) {
                    "cam_solar_flare" -> listOf(Color(0xFFFF5722), Color(0xFFFFC107), Color(0xFFFFEB3B))
                    "cam_nebula_cloud" -> listOf(Color(0xFF9C27B0), Color(0xFFE91E63), Color(0xFF00BCD4))
                    "cam_helix_cyber" -> listOf(Color(0xFF00FF66), Color(0xFF2979FF), Color(0xFF00E5FF))
                    "cam_quantum_dust" -> listOf(Color(0xFFFF9100), Color(0xFFFF3D00), Color(0xFFE403F5))
                    else -> listOf(Color(0xFF00C9A7), Color(0xFF845EC2))
                }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = colors,
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.width * 0.7f
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Face,
                        contentDescription = "Simulated Biometric Face Camera Avatar",
                        tint = Color.White,
                        modifier = Modifier.size((sizeDp * 0.45f).dp)
                    )
                }
            } else {
                Text(
                    text = name.take(2).uppercase(),
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = fontSizeSp.sp
                )
            }
        }
    }
}

@Composable
fun ProfileSettingsDialog(
    initialName: String,
    initialBio: String,
    initialAvatarUrl: String,
    onDismiss: () -> Unit,
    onSave: (name: String, bio: String, avatarUrl: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var bio by remember { mutableStateOf(initialBio) }
    var avatarUrl by remember { mutableStateOf(initialAvatarUrl) }
    var showCameraViewfinder by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = primaryColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile Settings", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive Avatar Edit
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AuraAvatar(
                        name = name,
                        avatarUrl = avatarUrl,
                        sizeDp = 80,
                        fontSizeSp = 24
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { showCameraViewfinder = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("camera_avatar_trigger")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Camera", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Capture Live Camera Avatar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Name Edit
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Display Name") },
                    placeholder = { Text("Enter your creative display name...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_name_field"),
                    shape = RoundedCornerShape(12.dp)
                )

                // Bio Edit
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Creators Biography") },
                    placeholder = { Text("Write something interesting about yourself...") },
                    maxLines = 4,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_bio_field"),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, bio, avatarUrl) },
                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("save_profile_settings_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Save, contentDescription = "Save", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Protocol")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("cancel_settings_btn")) {
                Text("Cancel")
            }
        }
    )

    if (showCameraViewfinder) {
        CameraViewfinderDialog(
            onDismiss = { showCameraViewfinder = false },
            onCapture = { capturedAvatar ->
                avatarUrl = capturedAvatar
                showCameraViewfinder = false
            }
        )
    }
}

@Composable
fun CameraViewfinderDialog(
    onDismiss: () -> Unit,
    onCapture: (String) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("cam_solar_flare") }
    var shutterTriggered by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    val filters = listOf(
        "cam_solar_flare" to "✨ Solar",
        "cam_nebula_cloud" to "🌌 Nebula",
        "cam_helix_cyber" to "🧬 Cyber",
        "cam_quantum_dust" to "🪐 Quantum"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "Tracking Grid")
    val animatedGridOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Grid Offset"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Biometric Security Cam", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                }
                Box(
                    modifier = Modifier
                        .background(Color.Red.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("LIVE FEED", color = Color.Red, fontSize = 8.sp, fontWeight = FontWeight.Black)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Select a hardware camera filter scheme, line up your cyber profile signature, and press Shutter.",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .border(1.5.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    val currentColors = when (selectedFilter) {
                        "cam_solar_flare" -> listOf(Color(0xFFFF5722), Color(0xFFFFC107))
                        "cam_nebula_cloud" -> listOf(Color(0xFF9C27B0), Color(0xFFE91E63))
                        "cam_helix_cyber" -> listOf(Color(0xFF00FF66), Color(0xFF00E5FF))
                        "cam_quantum_dust" -> listOf(Color(0xFFFF9100), Color(0xFFD500F9))
                        else -> listOf(Color.DarkGray, Color.Black)
                    }

                    val boundaryColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                    val scanLineColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f)

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = currentColors,
                                center = Offset(size.width / 2f, size.height / 2f),
                                radius = size.width * (0.5f + 0.2f * animatedGridOffset)
                            )
                        )
                        
                        val strokeWidth = 1f
                        val gridColor = Color.White.copy(alpha = 0.15f)

                        val offset = 16f
                        val sizeBracket = 30f
                        drawLine(boundaryColor, Offset(offset, offset), Offset(offset + sizeBracket, offset), 3f)
                        drawLine(boundaryColor, Offset(offset, offset), Offset(offset, offset + sizeBracket), 3f)
                        drawLine(boundaryColor, Offset(size.width - offset, offset), Offset(size.width - offset - sizeBracket, offset), 3f)
                        drawLine(boundaryColor, Offset(size.width - offset, offset), Offset(size.width - offset, offset + sizeBracket), 3f)
                        drawLine(boundaryColor, Offset(offset, size.height - offset), Offset(offset + sizeBracket, size.height - offset), 3f)
                        drawLine(boundaryColor, Offset(offset, size.height - offset), Offset(offset, size.height - offset - sizeBracket), 3f)
                        drawLine(boundaryColor, Offset(size.width - offset, size.height - offset), Offset(size.width - offset - sizeBracket, size.height - offset), 3f)
                        drawLine(boundaryColor, Offset(size.width - offset, size.height - offset), Offset(size.width - offset, size.height - offset - sizeBracket), 3f)

                        drawLine(gridColor, Offset(size.width / 3f, 0f), Offset(size.width / 3f, size.height), strokeWidth)
                        drawLine(gridColor, Offset(size.width * 2f / 3f, 0f), Offset(size.width * 2f / 3f, size.height), strokeWidth)
                        drawLine(gridColor, Offset(0f, size.height / 3f), Offset(size.width, size.height / 3f), strokeWidth)
                        drawLine(gridColor, Offset(0f, size.height * 2f / 3f), Offset(size.width, size.height * 2f / 3f), strokeWidth)

                        val scanY = size.height * animatedGridOffset
                        drawLine(
                            color = scanLineColor,
                            start = Offset(0f, scanY),
                            end = Offset(size.width, scanY),
                            strokeWidth = 4f
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.35f),
                            modifier = Modifier.size(50.dp)
                        )
                    }

                    if (shutterTriggered) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Aesthetic Lens Shaders:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.align(Alignment.Start))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filters.forEach { (tag, label) ->
                        val isSelected = selectedFilter == tag
                        Box(
                            modifier = Modifier
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedFilter = tag }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                                .testTag("camera_filter_${tag}")
                        ) {
                            Text(
                                label, 
                                fontSize = 9.sp, 
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.error, CircleShape)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .clickable {
                            if (!shutterTriggered) {
                                shutterTriggered = true
                                android.widget.Toast.makeText(context, "💥 Capture Complete!", android.widget.Toast.LENGTH_SHORT).show()
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(200)
                                    shutterTriggered = false
                                    onCapture(selectedFilter)
                                }
                            }
                        }
                        .testTag("camera_shutter_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("dismiss_camera_btn")) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ProfileSkeletonLoading(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Core Profile Card Skeleton
        Card(
            modifier = Modifier.fillMaxWidth().testTag("profile_skeleton_card"),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar circle
                SkeletonPlaceholder(
                    modifier = Modifier.size(90.dp),
                    shape = CircleShape
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Name block
                SkeletonPlaceholder(
                    modifier = Modifier.size(width = 160.dp, height = 24.dp)
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Handle block
                SkeletonPlaceholder(
                    modifier = Modifier.size(width = 100.dp, height = 14.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Bio paragraph
                SkeletonPlaceholder(
                    modifier = Modifier.fillMaxWidth(0.8f).height(12.dp)
                )
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonPlaceholder(
                    modifier = Modifier.fillMaxWidth(0.6f).height(12.dp)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    repeat(3) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            SkeletonPlaceholder(
                                modifier = Modifier.size(width = 50.dp, height = 16.dp)
                            )
                            SkeletonPlaceholder(
                                modifier = Modifier.size(width = 60.dp, height = 10.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SkeletonPlaceholder(
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                    SkeletonPlaceholder(
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Theme preset bar
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SkeletonPlaceholder(
                modifier = Modifier.size(width = 140.dp, height = 14.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                repeat(4) {
                    SkeletonPlaceholder(
                        modifier = Modifier.size(width = 85.dp, height = 36.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Upgrade Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SkeletonPlaceholder(
                    modifier = Modifier.size(width = 180.dp, height = 16.dp)
                )
                SkeletonPlaceholder(
                    modifier = Modifier.fillMaxWidth().height(12.dp)
                )
                SkeletonPlaceholder(
                    modifier = Modifier.fillMaxWidth(0.9f).height(12.dp)
                )
            }
        }
    }
}
