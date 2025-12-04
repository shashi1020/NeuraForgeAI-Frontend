package com.ai.neuraforge.util


import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MultipartBody
import java.io.InputStream

object FileUtils {
    fun uriToMultipart(contentResolver: ContentResolver, uri: Uri, partName: String = "file", filename: String = "upload.pdf"): MultipartBody.Part {
        val input: InputStream = contentResolver.openInputStream(uri) ?: throw IllegalArgumentException("Cannot open URI")
        val bytes = input.readBytes()
        input.close()
        val mediaType = "application/pdf".toMediaType()
        val body = bytes.toRequestBody(mediaType)
        return MultipartBody.Part.createFormData(partName, filename, body)
    }
}
