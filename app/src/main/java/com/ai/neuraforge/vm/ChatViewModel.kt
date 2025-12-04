package com.ai.neuraforge.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.neuraforge.repository.ChatRepository
import com.ai.neuraforge.repository.LocalConversation
import com.ai.neuraforge.repository.LocalMessage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChatViewModel(private val repo: ChatRepository) : ViewModel() {

    // list of local conversations
    private val _conversations = MutableStateFlow<List<LocalConversation>>(emptyList())
    val conversations: StateFlow<List<LocalConversation>> = _conversations.asStateFlow()

    // currently selected conversation id (local)
    private val _activeConversationId = MutableStateFlow<Long?>(null)
    val activeConversationId: StateFlow<Long?> = _activeConversationId.asStateFlow()

    // messages for active conversation
    private val _messages = MutableStateFlow<List<LocalMessage>>(emptyList())
    val messages: StateFlow<List<LocalMessage>> = _messages.asStateFlow()

    // diagnostics from last analyze
    private val _diagnostics = MutableStateFlow<List<String>>(emptyList())
    val diagnostics: StateFlow<List<String>> = _diagnostics.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    init {
        // initial load if needed
        _conversations.value = repo.listConversations()
    }

    fun selectConversation(localId: Long) {
        _activeConversationId.value = localId
        val conv = repo.getConversation(localId)
        _messages.value = conv?.messages?.toList() ?: emptyList()
    }

    fun analyzePdf(uri: Uri) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _diagnostics.value = listOf("Started analysis request.")
            val res = repo.analyzePdf(uri)
            res.fold(onSuccess = { (conv, diagnostics) ->
                // insert into local list
                val current = repo.listConversations()
                _conversations.value = current + conv
                selectConversation(conv.localId)
                _diagnostics.value = diagnostics ?: listOf("No diagnostics returned.")
            }, onFailure = { e ->
                _diagnostics.value = listOf("Analyze failed: ${e.message}")
            })
            _isAnalyzing.value = false
        }
    }

    // In ChatViewModel.kt

    fun sendQuestion(text: String) {
        val localConvId = _activeConversationId.value ?: return
        viewModelScope.launch {
            _isSending.value = true
            // optimistic update locally
            val conv = repo.getConversation(localConvId)
            conv?.messages?.add(LocalMessage(role = "user", content = text))
            _messages.value = conv?.messages?.toList() ?: emptyList()

            val res = repo.sendChat(localConvId, text)

            // CHANGED HERE: We don't expect an Int (conversation_id) anymore
            res.fold(onSuccess = {
                // refresh messages after server reply appended by repository
                val updated = repo.getConversation(localConvId)
                _messages.value = updated?.messages?.toList() ?: emptyList()
            }, onFailure = { e ->
                conv?.messages?.add(LocalMessage(role = "assistant", content = "Error: ${e.message}"))
                _messages.value = conv?.messages?.toList() ?: emptyList()
            })
            _isSending.value = false
        }
    }
}
