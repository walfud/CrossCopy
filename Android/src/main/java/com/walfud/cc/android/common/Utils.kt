package com.walfud.cc.android.common

import android.content.*
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.FileUtils
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.walfud.cc.android.BuildConfig
import com.walfud.cc.projectshare.util.stringToQrcodeImage
import java.io.File
import java.util.*

private lateinit var context: Context

fun setupUtils(ctx: Context) {
    context = ctx.applicationContext
}

fun Uri.toAbsoluteFile(): File {
    return when (scheme) {
        ContentResolver.SCHEME_FILE -> File(path!!)
        ContentResolver.SCHEME_CONTENT -> {
            val contentResolver = context.contentResolver
            val filename = UUID.randomUUID().toString()
            val extname = MimeTypeMap.getSingleton().getExtensionFromMimeType(contentResolver.getType(this))
            val tmpFile = File(context.cacheDir, "$filename.$extname")
            contentResolver.openInputStream(this).use { inputStream ->
                tmpFile.outputStream().use { outputStream ->
                    FileUtils.copy(inputStream!!, outputStream)
                }
            }
            tmpFile
        }

        else -> throw RuntimeException("`toAbsoluteFile`: unsupported scheme($scheme)")
    }
}

fun getCacheFile(filename: String): File {
    return File(context.cacheDir, filename)
}

const val DOWNLOAD_DIR = "CrossCopy"

fun copyPrivateFileToPublicDownloadDir(file: File): File {
    val contentValues = ContentValues().apply {
        put(MediaStore.Downloads.DISPLAY_NAME, file.name)
//        put(MediaStore.Downloads.MIME_TYPE, getMIMEType(file))
        put(MediaStore.Downloads.DATE_TAKEN, System.currentTimeMillis())
    }
    val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)!!
    context.contentResolver.openOutputStream(uri).use { outputStream ->
        file.inputStream().use { inputStream ->
            FileUtils.copy(inputStream, outputStream!!)
        }
    }
    return uri.toAbsoluteFile()
}

fun getMIMEType(file: File): String? {
    return MediaMetadataRetriever()
        .apply {
            setDataSource(file.absolutePath)
        }
        .use {
            it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
        }
}

fun copyFromClipboard(): String? {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    return clipboardManager.primaryClip?.let {
        if (it.itemCount > 0) {
            it.getItemAt(0).text.toString()
        } else {
            null
        }
    }
}

fun copyToClipboard(text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("from cc", text)
    clipboardManager.setPrimaryClip(clipData)
}

fun openFile(file: File) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    val uri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", file)
    intent.setDataAndType(uri, "application/vnd.android.package-archive")
    context.startActivity(intent)
}

fun stringToBitmap(text: String, size: Int = 300): Bitmap {
    val pixels = stringToQrcodeImage(text, size)
    return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        .apply {
            setPixels(pixels, 0, size, 0, 0, size, size)
        }
}