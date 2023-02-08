package com.walfud.cc.clientshare

import com.walfud.cc.projectshare.model.Thing
import com.walfud.cc.projectshare.model.ThingDownloadFileRequest
import com.walfud.cc.projectshare.model.ThingUploadTextRequest
import com.walfud.cc.projectshare.model.TokenResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.content.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.concurrent.TimeUnit

const val CC_URL = "https://cc.walfud.com"
const val NEW_TOKEN_URL = "$CC_URL/token/new"
const val THING_LIST_URL = "$CC_URL/thing/list"
const val THING_UPLOAD_TEXT_URL = "$CC_URL/thing/upload/text"
const val THING_UPLOAD_FILE_URL = "$CC_URL/thing/upload/file"
const val THING_DOWNLOAD_FILE_URL = "$CC_URL/thing/download/file"

const val HEADER_CC_SESSION = "CC-SESSION"

val httpClient = HttpClient(CIO) {
    install(ContentNegotiation) {
        json()
    }
    defaultRequest {
        headers {
            val ccConfig = getConfigFromLocal()
            if (ccConfig != null) {
                this[HEADER_CC_SESSION] = ccConfig.token
            }
        }
    }
    install(HttpTimeout)
}

fun getTokenFromServerSync(): String = runBlocking { getTokenFromServer() }

suspend fun getTokenFromServer(): String {
    val response = httpClient.post(NEW_TOKEN_URL)
    if (!response.status.isSuccess()) {
        throw RuntimeException("`getTokenFromServer`: fail($response)")
    }

    val newTokenResponse: TokenResponse = response.body()
    return newTokenResponse.token
}

fun getThingsFromServerSync(): List<Thing> = runBlocking { getThingsFromServer() }
suspend fun getThingsFromServer(): List<Thing> {
    val response = httpClient.post(THING_LIST_URL)
    if (!response.status.isSuccess()) {
        throw RuntimeException("`getThingsFromServer`: fail($response)")
    }

    return response.body()
}

fun uploadTextToServerSync(text: String): Thing = runBlocking { uploadTextToServer(text) }
suspend fun uploadTextToServer(text: String): Thing {
    val response = httpClient.post(THING_UPLOAD_TEXT_URL) {
        contentType(ContentType.Application.Json)
        setBody(ThingUploadTextRequest(text))
    }
    if (!response.status.isSuccess()) {
        throw RuntimeException("`uploadTextToServer`: fail($response)")
    }

    return response.body()
}

fun uploadFileToServerSync(name: String, file: File, listener: ProgressListener): Thing = runBlocking { uploadFileToServer(name, file, listener) }
suspend fun uploadFileToServer(name: String, file: File, listener: ProgressListener): Thing {
    val response = httpClient.post(THING_UPLOAD_FILE_URL) {
        setBody(
            MultiPartFormDataContent(
                formData {
                    append(name, file.readBytes(), Headers.build {
                        append(HttpHeaders.ContentDisposition, """ filename="${file.name}" """)
                    })
                },
            )
        )

        onUpload { bytesSentTotal, contentLength ->
            listener(bytesSentTotal, contentLength)
        }

        timeout {
            requestTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
        }
    }
    if (!response.status.isSuccess()) {
        throw RuntimeException("`uploadFileToServer`: fail($response)")
    }

    return response.body()
}

fun downloadFileFromServerSync(serverFilename: String, localFile: File, listener: ProgressListener) =
    runBlocking { downloadFileFromServer(serverFilename, localFile, listener) }

suspend fun downloadFileFromServer(serverFilename: String, localFile: File, listener: ProgressListener): File {
    val response = httpClient.post(THING_DOWNLOAD_FILE_URL) {
        contentType(ContentType.Application.Json)
        setBody(ThingDownloadFileRequest(serverFilename))

        onDownload { bytesSentTotal, contentLength ->
            listener(bytesSentTotal, contentLength)
        }

        timeout {
            requestTimeoutMillis = HttpTimeout.INFINITE_TIMEOUT_MS
        }
    }
    if (!response.status.isSuccess()) {
        throw RuntimeException("`downloadFileFromServer`: fail($response)")
    }

    val responseBody: ByteArray = response.body()
    localFile.writeBytes(responseBody)
    return localFile
}
