package com.walfud.extention

import java.security.MessageDigest
import java.util.*

fun Char.isChinese(): Boolean = Regex("""[\u4e00-\u9fa5]""").matches("$this")

fun String.camel(): String {
    val sb = StringBuilder()
    var toggle = false
    forEach {
        if (it == '_') {
            toggle = true
        } else {
            if (toggle) {
                sb.append(it.uppercaseChar())
                toggle = false
            } else {
                sb.append(it)
            }
        }
    }

    return sb.toString()
}
fun String.underscore(): String {
    val sb = StringBuilder()
    forEach {
        if (it.isUpperCase()) {
            sb.append('_').append(it.lowercaseChar())
        } else {
            sb.append(it)
        }
    }

    return sb.toString()
}
fun String.md5(): String {
    val digest = MessageDigest.getInstance("MD5")
    val result = digest.digest(toByteArray())
    return result.hex()
}
fun String.base64ToByteArray(): ByteArray = Base64.getDecoder().decode(this)
fun String.aes(sk: String = "walfud", iv: String? = null, algorithm: String = "AES/ECB/PKCS5Padding"): ByteArray? = toByteArray().aes(sk, iv, algorithm)
fun String.isNumber(): Boolean = all(Char::isDigit)
fun String.isAlphabet(lowerOrUpper: Boolean? = null): Boolean = all {
    return@all when (lowerOrUpper) {
        true -> it.isLowerCase()
        false -> it.isUpperCase()
        null -> it.isLowerCase() || it.isUpperCase()
    }
}

fun String.isUuid(): Boolean {
    if (length != 36) {
        return false
    }
    forEachIndexed { index, c ->
        when (index) {
            8, 13, 18, 23 -> {
                if (c != '-') {
                    return false
                }
            }
            else -> {
                if (!c.isDigit() && !c.isAlphabetic()) {
                    return false
                }
            }
        }
    }

    return true
}