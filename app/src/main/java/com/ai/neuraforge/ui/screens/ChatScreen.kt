package com.ai.neuraforge.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ai.neuraforge.repository.LocalMessage
import com.ai.neuraforge.viewmodel.ChatViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextAlign
import java.util.regex.Pattern

// Constants for layout
private val CHAT_MAX_WIDTH_DP = 720.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val messages by viewModel.messages.collectAsState()
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val diagnostics by viewModel.diagnostics.collectAsState()
    val isSending by viewModel.isSending.collectAsState()

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { viewModel.analyzePdf(it) }
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to latest
    LaunchedEffect(messages.size, isAnalyzing, isSending) {
        if (messages.isNotEmpty() || isAnalyzing || isSending) {
            delay(100) // Slight delay to allow layout to calculate
            val targetIndex = if (isAnalyzing || isSending) messages.size else messages.size - 1
            if (targetIndex >= 0) {
                try {
                    listState.animateScrollToItem(targetIndex)
                } catch (_: Exception) {
                    listState.scrollToItem(targetIndex)
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background, // Uses theme background
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "NeuraForge AI",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.5).sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    scrolledContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier.shadow(4.dp)
            )
        },
        bottomBar = {
            BottomInputBar(
                onSelectPdf = { launcher.launch("application/pdf") },
                onSend = { q -> viewModel.sendQuestion(q) },
                isBusy = isSending || isAnalyzing
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background), // Ensure background color
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = CHAT_MAX_WIDTH_DP)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                state = listState,
                contentPadding = PaddingValues(bottom = 16.dp, top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Intro / Empty State
                if (messages.isEmpty() && !isAnalyzing) {
                    item {
                        EmptyStatePlaceholder(onUploadClick = { launcher.launch("application/pdf") })
                    }
                }

                // Chat Messages
                itemsIndexed(messages) { _, msg ->
                    MessageBubble(message = msg)
                }

                // Thinking / Analyzing Indicator
                if (isAnalyzing || isSending) {
                    item {
                        ThinkingIndicator(
                            text = if (isAnalyzing) "Processing Document..." else "Thinking..."
                        )
                    }
                }

                // Diagnostics Banner (Optional, shows only if analyzing)
                if (isAnalyzing) {
                    item {
                        DiagnosticsBanner(diagnostics = diagnostics)
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// UI COMPONENTS
// -----------------------------------------------------------------------------

@Composable
fun EmptyStatePlaceholder(onUploadClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Description,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Upload a PDF to Start",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Get summaries, insights, and chat with your documents powered by Gemini AI.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onUploadClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Select Document")
        }
    }
}

@Composable
fun ThinkingIndicator(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar Placeholder for AI
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                Icons.Filled.AutoAwesome,
                contentDescription = "AI",
                modifier = Modifier.padding(6.dp),
                tint = Color.White
            )
        }
        Spacer(modifier = Modifier.width(12.dp))

        // Typing Bubble
        Surface(
            shape = RoundedCornerShape(topStart = 4.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.animateContentSize()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                TypingDots()
            }
        }
    }
}

@Composable
fun TypingDots() {
    val dots = listOf(
        remember { Animatable(0f) },
        remember { Animatable(0f) },
        remember { Animatable(0f) }
    )

    dots.forEachIndexed { index, animatable ->
        LaunchedEffect(animatable) {
            delay(index * 150L)
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = keyframes {
                        durationMillis = 1200
                        0f at 0 with LinearOutSlowInEasing
                        1f at 300 with LinearOutSlowInEasing
                        0f at 600 with LinearOutSlowInEasing
                        0f at 1200 with LinearOutSlowInEasing
                    },
                    repeatMode = RepeatMode.Restart
                )
            )
        }
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        dots.forEach { animatable ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .size(6.dp)
                    .graphicsLayer {
                        alpha = 0.4f + (animatable.value * 0.6f)
                        translationY = -animatable.value * 10f
                    }
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
fun DiagnosticsBanner(diagnostics: List<String>) {
    if (diagnostics.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Status Log:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = diagnostics.lastOrNull() ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun MessageBubble(message: LocalMessage) {
    val isUser = message.role.equals("user", ignoreCase = true)

    // Bubble Shapes
    val userShape = RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    val aiShape = RoundedCornerShape(4.dp, 20.dp, 20.dp, 20.dp)

    // Colors
    val containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
    val contentColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val elevation = if (isUser) 4.dp else 1.dp

    Column(
        modifier = Modifier.fillMaxWidth(),
        // FIX: Ensure messages are aligned correctly (End for user, Start for AI)
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Bottom,
            // FIX: Max width is the chat window max width minus padding to allow centering
            // Note: The parent LazyColumn is already constrained by CHAT_MAX_WIDTH_DP.
            // We constrain the Row slightly less to allow space for the avatar/end padding.
            modifier = Modifier.widthIn(max = CHAT_MAX_WIDTH_DP - 32.dp)
        ) {
            // AI Avatar (only for assistant)
            if (!isUser) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = "Bot",
                        modifier = Modifier.padding(6.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            // Message Body
            Surface(
                shape = if (isUser) userShape else aiShape,
                color = containerColor,
                shadowElevation = elevation,
                modifier = Modifier.widthIn(max = 280.dp) // Max width for bubble
            ) {
                SelectionContainer {
                    MarkdownText(
                        text = message.content,
                        color = contentColor,
                        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun MarkdownText(
    text: String,
    color: Color,
    style: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier
) {
    // Regex pattern for **bold** (handles **content**)
    val boldPattern = Pattern.compile("\\*\\*(.*?)\\*\\*")

    val annotatedString = buildAnnotatedString {
        var currentIndex = 0
        val matcher = boldPattern.matcher(text)

        while (matcher.find()) {
            // Append plain text before the match
            append(text.substring(currentIndex, matcher.start()))

            // Append bold text
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                // Group 1 is the content inside **...**
                append(matcher.group(1))
            }

            currentIndex = matcher.end()
        }

        // Append remaining plain text
        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }

        // Note: Newline characters (\n) are automatically respected by the Text composable
        // without manual modification of the AnnotatedString.
    }

    Text(
        text = annotatedString,
        color = color,
        style = style,
        modifier = modifier
    )
}


@Composable
fun BottomInputBar(onSelectPdf: () -> Unit, onSend: (String) -> Unit, isBusy: Boolean) {
    var text by remember { mutableStateOf("") }
    val isInputAllowed = !isBusy

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp, // slight lift from bottom
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Upload Button
            IconButton(
                onClick = onSelectPdf,
                enabled = isInputAllowed,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                    .size(44.dp)
            ) {
                Icon(
                    Icons.Outlined.Description,
                    contentDescription = "Upload PDF",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Text Input
            OutlinedTextField(
                value = text,
                onValueChange = { if (isInputAllowed) text = it },
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 50.dp, max = 120.dp), // Grows slightly
                placeholder = {
                    Text(
                        "Ask anything...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                enabled = isInputAllowed,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                maxLines = 4
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Send Button
            val sendButtonColor = if (text.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

            IconButton(
                onClick = {
                    if (text.isNotBlank() && isInputAllowed) {
                        onSend(text.trim())
                        text = ""
                    }
                },
                enabled = isInputAllowed && text.isNotBlank(),
                modifier = Modifier
                    .background(sendButtonColor, CircleShape)
                    .size(44.dp)
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.padding(start = 2.dp) // Visual centering
                    )
                }
            }
        }
    }
}