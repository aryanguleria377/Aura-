package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.SocialViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    viewModel: SocialViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var isLoginMode by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var loadingMessage by remember { mutableStateOf("") }

    // Input States
    var email by remember { mutableStateOf("aryanguleria377@gmail.com") }
    var password by remember { mutableStateOf("Aryan@122") }
    var name by remember { mutableStateOf("") }
    var handle by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }

    // Error States
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var handleError by remember { mutableStateOf<String?>(null) }

    // Visual preferences
    var isPasswordVisible by remember { mutableStateOf(false) }
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val surfaceColor = MaterialTheme.colorScheme.surface

    // Input validations
    fun validateEmail(mail: String): Boolean {
        val emailPattern = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$"
        return mail.isNotBlank() && java.util.regex.Pattern.compile(emailPattern).matcher(mail).matches()
    }

    fun handleSignIn() {
        var isValid = true
        if (!validateEmail(email)) {
            emailError = "Please enter a valid email address."
            isValid = false
        } else {
            emailError = null
        }

        if (password.length < 6) {
            passwordError = "Password must be at least 6 characters."
            isValid = false
        } else {
            passwordError = null
        }

        if (isValid) {
            coroutineScope.launch {
                isLoading = true
                loadingMessage = "Synchronizing secure identity coordinates..."
                viewModel.logInUser(email)
                isLoading = false
            }
        }
    }

    fun handleRegister() {
        var isValid = true
        if (!validateEmail(email)) {
            emailError = "Please enter a valid email address."
            isValid = false
        } else {
            emailError = null
        }

        if (password.length < 6) {
            passwordError = "Create a password of at least 6 characters."
            isValid = false
        } else {
            passwordError = null
        }

        if (name.isBlank()) {
            nameError = "Display name cannot be empty."
            isValid = false
        } else {
            nameError = null
        }

        if (handle.isBlank()) {
            handleError = "Unique handletag is required."
            isValid = false
        } else {
            handleError = null
        }

        if (isValid) {
            coroutineScope.launch {
                isLoading = true
                loadingMessage = "Minting cryptographically verified session state..."
                viewModel.authenticateUser(
                    name = name,
                    handle = handle,
                    bio = bio,
                    email = email
                )
                isLoading = false
            }
        }
    }

    fun handleGoogleSignIn() {
        coroutineScope.launch {
            isLoading = true
            loadingMessage = "Connecting Google Cryptographic handshakes..."
            viewModel.logInGoogle()
            isLoading = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Glowing Background Circles drawn using Compose Canvas for beautiful atmosphere
        AuraAtmosphericBackground(primaryColor = primaryColor)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Minimalist Brand Logo (Dynamic concentric rotating rings representation)
            AuraMinimalLogo()

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "AURA",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                letterSpacing = 6.sp,
                color = onSurfaceColor
            )

            Text(
                text = "SECURE NETWORK PROTOCOL",
                fontWeight = FontWeight.Medium,
                fontSize = 10.sp,
                letterSpacing = 2.sp,
                color = onSurfaceColor.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Segmented mode switcher
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, onSurfaceColor.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (isLoginMode) primaryColor else Color.Transparent)
                            .clickable { isLoginMode = true }
                            .testTag("toggle_login_tab_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isLoginMode) Color.Black else onSurfaceColor.copy(alpha = 0.6f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(22.dp))
                            .background(if (!isLoginMode) primaryColor else Color.Transparent)
                            .clickable { isLoginMode = false }
                            .testTag("toggle_register_tab_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Register",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (!isLoginMode) Color.Black else onSurfaceColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Smooth horizontal animated transitions using standard composable bounds switching
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow))
            ) {
                if (isLoginMode) {
                    // Sign In Screen Card Panel
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.85f)),
                        border = BorderStroke(1.dp, onSurfaceColor.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Welcome Back",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = onSurfaceColor
                            )

                            // Email text field
                            Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it; emailError = null },
                                    label = { Text("Network Email Identifier", fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Email Icon", modifier = Modifier.size(20.dp)) },
                                    isError = emailError != null,
                                    modifier = Modifier.fillMaxWidth().testTag("email_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.15f),
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                )
                                emailError?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 6.dp, top = 4.dp).testTag("email_error_label")
                                    )
                                }
                            }

                            // Password text field
                            Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it; passwordError = null },
                                    label = { Text("Cryptographic Password", fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Lock Icon", modifier = Modifier.size(20.dp)) },
                                    trailingIcon = {
                                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle password view",
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    isError = passwordError != null,
                                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth().testTag("password_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.15f),
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                                )
                                passwordError?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 6.dp, top = 4.dp).testTag("password_error_label")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Submit sign in
                            Button(
                                onClick = { handleSignIn() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("submit_auth_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "Authorize & Enter",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                } else {
                    // Registration Screen Card Panel
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = surfaceColor.copy(alpha = 0.85f)),
                        border = BorderStroke(1.dp, onSurfaceColor.copy(alpha = 0.08f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Register Protocol Node",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = onSurfaceColor
                            )

                            // Display Name field
                            Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it; nameError = null },
                                    label = { Text("Full Identity Name", fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = "User Icon", modifier = Modifier.size(20.dp)) },
                                    isError = nameError != null,
                                    modifier = Modifier.fillMaxWidth().testTag("name_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.15f),
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    )
                                )
                                nameError?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 6.dp, top = 4.dp).testTag("name_error_label")
                                    )
                                }
                            }

                            // Handle field
                            Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = handle,
                                    onValueChange = { handle = it; handleError = null },
                                    label = { Text("Unique Handletag (e.g. alex_dev)", fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Outlined.AlternateEmail, contentDescription = "@ Handle Icon", modifier = Modifier.size(20.dp)) },
                                    isError = handleError != null,
                                    modifier = Modifier.fillMaxWidth().testTag("handle_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.15f),
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    )
                                )
                                handleError?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 6.dp, top = 4.dp).testTag("handle_error_label")
                                    )
                                }
                            }

                            // Network Email Field
                            Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it; emailError = null },
                                    label = { Text("Network Email Identifier", fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = "Email Icon", modifier = Modifier.size(20.dp)) },
                                    isError = emailError != null,
                                    modifier = Modifier.fillMaxWidth().testTag("email_register_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.15f),
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                                )
                                emailError?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 6.dp, top = 4.dp).testTag("email_register_error_label")
                                    )
                                }
                            }

                            // Password field
                            Column(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it; passwordError = null },
                                    label = { Text("Secure Password Code (6+ chars)", fontSize = 12.sp) },
                                    leadingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Lock Icon", modifier = Modifier.size(20.dp)) },
                                    isError = passwordError != null,
                                    visualTransformation = PasswordVisualTransformation(),
                                    modifier = Modifier.fillMaxWidth().testTag("password_register_input"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = primaryColor,
                                        unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.15f),
                                        errorBorderColor = MaterialTheme.colorScheme.error
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                                )
                                passwordError?.let {
                                    Text(
                                        text = it,
                                        color = MaterialTheme.colorScheme.error,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 6.dp, top = 4.dp).testTag("password_register_error_label")
                                    )
                                }
                            }

                            // Bio field (Optional details)
                            OutlinedTextField(
                                value = bio,
                                onValueChange = { bio = it },
                                label = { Text("Identity Biography (Optional)", fontSize = 12.sp) },
                                leadingIcon = { Icon(Icons.Outlined.Notes, contentDescription = "Bio Icon", modifier = Modifier.size(20.dp)) },
                                modifier = Modifier.fillMaxWidth().testTag("bio_input"),
                                minLines = 2,
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = primaryColor,
                                    unfocusedBorderColor = onSurfaceColor.copy(alpha = 0.15f)
                                )
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Submit Registration
                            Button(
                                onClick = { handleRegister() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("submit_auth_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Text(
                                    text = "Generate Nodes & Join",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Or separator line
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = onSurfaceColor.copy(alpha = 0.1f))
                Text(
                    text = "OAUTH SIGN-IN SECURED",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = onSurfaceColor.copy(alpha = 0.4f),
                    modifier = Modifier.padding(horizontal = 14.dp)
                )
                Divider(modifier = Modifier.weight(1f), color = onSurfaceColor.copy(alpha = 0.1f))
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Beautiful custom Google Sign-In Button with high touch area
            OutlinedButton(
                onClick = { handleGoogleSignIn() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("google_signin_btn"),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, onSurfaceColor.copy(alpha = 0.15f)),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.Transparent)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Beautiful custom stylized bold colored G representing the classic Google Identity logo
                    Text(
                        text = "G",
                        color = Color(0xFF4285F4),
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = "Sign in with Google Account",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = onSurfaceColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Animated Loader Overlay for authentic authorization transition
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(animationSpec = tween(250)),
            exit = fadeOut(animationSpec = tween(250))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.82f))
                    .clickable(enabled = false) {}, // Intercept clicks during transit
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.padding(32.dp),
                    border = BorderStroke(1.dp, onSurfaceColor.copy(alpha = 0.1f))
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = primaryColor,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = loadingMessage,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = onSurfaceColor.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuraAtmosphericBackground(primaryColor: Color) {
    val gradientColors1 = remember(primaryColor) {
        listOf(primaryColor.copy(alpha = 0.1f), Color.Transparent)
    }
    val gradientColors2 = remember(primaryColor) {
        listOf(primaryColor.copy(alpha = 0.08f), Color.Transparent)
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Subtle soft upper-left and lower-right atmospheric deep space colors representing an executive minimal tech feel
        drawCircle(
            brush = Brush.radialGradient(
                colors = gradientColors1,
                center = Offset(x = 0f, y = size.height * 0.15f),
                radius = size.width * 0.9f
            )
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = gradientColors2,
                center = Offset(x = size.width, y = size.height * 0.85f),
                radius = size.width * 0.9f
            )
        )
    }
}

@Composable
fun AuraMinimalLogo() {
    val transition = rememberInfiniteTransition(label = "ring_rotation")
    val angleRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_value"
    )

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    Box(
        modifier = Modifier
            .size(72.dp)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Dual concentric rotating custom orbits
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 1.5.dp.toPx()
            val outerRadius = size.width * 0.45f
            val innerRadius = size.width * 0.3f
            
            // Outer concentric frame orbit
            drawCircle(
                color = primaryColor.copy(alpha = 0.1f),
                radius = outerRadius,
                style = Stroke(width = strokeWidth)
            )

            // Dynamic rotating indicator dot (Hardware rotated)
            val outerDotRadiusPx = 3.dp.toPx()
            withTransform({
                rotate(degrees = angleRotation, pivot = center)
            }) {
                drawCircle(
                    color = primaryColor,
                    radius = outerDotRadiusPx,
                    center = Offset(center.x + outerRadius, center.y)
                )
            }

            // Inner concentric frame orbit
            drawCircle(
                color = secondaryColor.copy(alpha = 0.12f),
                radius = innerRadius,
                style = Stroke(width = strokeWidth)
            )

            // Inner rotating indicator dot rotating counter-clockwise (Hardware rotated)
            val innerDotRadiusPx = 2.5.dp.toPx()
            withTransform({
                rotate(degrees = -angleRotation * 1.5f, pivot = center)
            }) {
                drawCircle(
                    color = secondaryColor,
                    radius = innerDotRadiusPx,
                    center = Offset(center.x + innerRadius, center.y)
                )
            }

            // Center solid glowing core
            drawCircle(
                color = primaryColor.copy(alpha = 0.25f),
                radius = 8.dp.toPx()
            )
            drawCircle(
                color = primaryColor,
                radius = 4.dp.toPx()
            )
        }
    }
}
