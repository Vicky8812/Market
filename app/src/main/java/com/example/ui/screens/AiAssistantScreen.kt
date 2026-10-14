package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.remote.ChatTurn
import com.example.data.repository.MarketRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun AiAssistantScreen(
    repository: MarketRepository,
    initialPrompt: String? = null,
    onStockClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = MessageSender.AI_ASSISTANT,
                text = "Welcome to MarketPulse AI Research. I'm connected to live US market data, financial models, and specialized cross-border guidance for Indian investors.\n\nAsk me about stock analysis, price forecasts, RBI LRS remittances, TCS tax credits, Form W-8BEN, or evening trading hours in IST."
            )
        )
    }

    var inputPrompt by remember { mutableStateOf(initialPrompt ?: "") }
    var isGenerating by remember { mutableStateOf(false) }

    val quickQuestions = listOf(
        "🇮🇳 How to invest in US stocks from India under LRS?",
        "Explain 20% TCS on remittances & how to claim ITR refund",
        "Form W-8BEN and US vs Indian capital gains taxation",
        "Which US stocks have the strongest fundamentals today?",
        "Compare NVDA and MSFT for long-term Indian investors",
        "US market hours in Indian Standard Time (IST) schedule",
        "Build a ₹5,00,000 diversified US portfolio blueprint"
    )

    fun sendQuestion(text: String) {
        if (text.isBlank() || isGenerating) return
        val userText = text.trim()
        val userMsg = ChatMessage(sender = MessageSender.USER, text = userText)
        messages.add(userMsg)
        inputPrompt = ""
        isGenerating = true

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)

            // Convert conversation history into multi-turn ChatTurn list
            val history = messages.dropLast(1).map { msg ->
                ChatTurn(
                    role = if (msg.sender == MessageSender.USER) "user" else "model",
                    content = msg.text
                )
            }

            val responseText = repository.askAiAssistantMultiTurn(history, userText)
            messages.add(ChatMessage(sender = MessageSender.AI_ASSISTANT, text = responseText))
            isGenerating = false
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Auto-send if initialPrompt was provided
    LaunchedEffect(initialPrompt) {
        if (!initialPrompt.isNullOrBlank() && messages.size == 1) {
            sendQuestion(initialPrompt)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Assistant Top Header with Model badge & Clear action
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "MarketPulse AI Research",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = CyanAccentBg,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "gemini-3.8-flash",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = CyanAccent
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Multi-Turn US & India Intelligence",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = TextMuted
                        )
                    }
                }
            }

            IconButton(
                onClick = {
                    messages.clear()
                    messages.add(
                        ChatMessage(
                            sender = MessageSender.AI_ASSISTANT,
                            text = "Conversation reset. How can I assist your US stock research or India-to-US investing plans today?"
                        )
                    )
                },
                modifier = Modifier.testTag("clear_chat_button")
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = TextMuted)
            }
        }

        HorizontalDivider(color = SurfaceCardBorder)

        // Conversation History (Multi-Turn Thread)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp)
        ) {
            items(messages) { message ->
                ChatMessageItem(message = message, onTickerClick = onStockClick)
            }

            if (isGenerating) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(SurfaceCard, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = CyanAccent,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Analyzing US market models, cross-border rules, and financial statements...",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips for Fast Multi-Turn Research
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickQuestions) { question ->
                SuggestionChip(
                    onClick = { sendQuestion(question) },
                    label = { Text(question, maxLines = 1, style = MaterialTheme.typography.labelSmall) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = SurfaceCard,
                        labelColor = TextSecondary
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        enabled = true,
                        borderColor = SurfaceCardBorder
                    )
                )
            }
        }

        // Bottom Input Field
        Surface(
            color = SurfaceDark,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputPrompt,
                    onValueChange = { inputPrompt = it },
                    placeholder = { Text("Ask about US stocks, LRS, taxes, or strategy...", color = TextMuted) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = SurfaceCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { sendQuestion(inputPrompt) },
                    enabled = inputPrompt.isNotBlank() && !isGenerating,
                    modifier = Modifier
                        .background(if (inputPrompt.isNotBlank()) CyanAccent else SurfaceCardElevated, RoundedCornerShape(20.dp))
                        .testTag("ai_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputPrompt.isNotBlank()) BackgroundDark else TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(
    message: ChatMessage,
    onTickerClick: (String) -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(CyanAccentBg, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) SurfaceCardElevated else SurfaceCard
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(if (isUser) CyanAccent.copy(alpha = 0.5f) else SurfaceCardBorder)
            ),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp),
                    color = TextPrimary
                )

                if (!isUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Informational research only • Not investment advice",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = TextMuted
                    )
                }
            }
        }
    }
}
