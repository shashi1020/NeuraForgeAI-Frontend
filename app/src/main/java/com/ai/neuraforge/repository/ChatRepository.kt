package com.ai.neuraforge.repository

import android.content.ContentResolver
import android.net.Uri
import com.ai.neuraforge.network.RetrofitClient
import com.ai.neuraforge.network.dto.ChatRequestDto
import com.ai.neuraforge.repository.LocalConversation
import com.ai.neuraforge.repository.LocalMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.InputStream
import java.util.concurrent.atomic.AtomicLong

// Local simple models
data class LocalMessage(val role: String, val content: String)
data class LocalConversation(
    val localId: Long,
    var docUuid: String? = null, // We only need docUuid now, serverConversationId is removed
    var title: String? = null,
    val messages: MutableList<LocalMessage> = mutableListOf()
)

class ChatRepository(private val contentResolver: ContentResolver) {
    private val api = RetrofitClient.apiService

    // in-memory "DB"
    private val conversations = mutableMapOf<Long, LocalConversation>()
    private val idGen = AtomicLong(1)

    fun listConversations(): List<LocalConversation> = synchronized(conversations) {
        conversations.values.toList().sortedByDescending { it.localId }
    }

    fun getConversation(localId: Long): LocalConversation? = synchronized(conversations) { conversations[localId] }

    private fun createLocalConversation(docUuid: String?, title: String?): LocalConversation {
        val localId = idGen.getAndIncrement()
        val conv = LocalConversation(localId = localId, docUuid = docUuid, title = title)
        synchronized(conversations) { conversations[localId] = conv }
        return conv
    }

    private suspend fun readBytesFromUri(uri: Uri): ByteArray = withContext(Dispatchers.IO) {
        val input: InputStream = contentResolver.openInputStream(uri) ?: throw IllegalArgumentException("Cannot open $uri")
        input.use { it.readBytes() }
    }

    private fun guessFileName(uri: Uri): String {
        var name = "upload.pdf"
        try {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    val n = cursor.getString(nameIndex)
                    if (!n.isNullOrBlank()) name = n
                }
            }
        } catch (_: Exception) {}
        return name
    }

    /**
     * Upload PDF to /analyze. Returns LocalConversation (created) and optional diagnostics string
     */
    suspend fun analyzePdf(uri: Uri): Result<Pair<LocalConversation, List<String>?>> = withContext(Dispatchers.IO) {
        try {
            val bytes = readBytesFromUri(uri)
            val filename = guessFileName(uri)
            val reqBody = RequestBody.create("application/pdf".toMediaType(), bytes)
            val part = MultipartBody.Part.createFormData("file", filename, reqBody)

            val resp = api.analyzePdf(part)
            if (resp.isSuccessful) {
                val body = resp.body()!!
                // We don't get a conversation_id from backend anymore, just doc_id
                val conv = createLocalConversation(docUuid = body.doc_id, title = "Chat — ${filename.take(12)}")

                // Add summary as the first message from Assistant
                if (body.summary.isNotBlank()) {
                    conv.messages.add(LocalMessage(role = "assistant", content = "Summary:\n${body.summary}"))
                }

                // Create a "diagnostic" string from the structured data just for UI feedback
                val diagInfo = listOfNotNull(
                    if (body.name != null) "Name detected: ${body.name}" else null,
                    "Topics: ${body.topics.size} found"
                )

                Result.success(conv to diagInfo)
            } else {
                Result.failure(Exception("Analyze failed: ${resp.code()} ${resp.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Send a chat question. Returns Unit on success.
     */
    suspend fun sendChat(localConvId: Long, question: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val conv = getConversation(localConvId) ?: return@withContext Result.failure(Exception("Local conversation not found"))

            // Ensure we have a doc_id to chat with
            val docId = conv.docUuid ?: return@withContext Result.failure(Exception("No Document ID associated with this chat"))

            // 1. Add user message locally
            // (ViewModel already adds it mostly, but good to ensure sync)
            if (conv.messages.lastOrNull()?.content != question) {
                conv.messages.add(LocalMessage(role = "user", content = question))
            }

            // 2. Send Request
            // Backend expects: doc_id, question
            val req = ChatRequestDto(doc_id = docId, question = question)

            val resp = api.chat(req)
            if (resp.isSuccessful) {
                val body = resp.body()!!

                // 3. Append Assistant Answer
                conv.messages.add(LocalMessage(role = "assistant", content = body.answer))

                Result.success(Unit)
            } else {
                Result.failure(Exception("Chat failed: ${resp.code()} ${resp.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}