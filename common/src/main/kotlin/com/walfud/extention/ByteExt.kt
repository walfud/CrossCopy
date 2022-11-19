package com.walfud.extention

import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

fun ByteArray.base64(): String = Base64.getEncoder().encodeToString(this)
fun ByteArray.aes(sk: String = "walfud", iv: String? = null, algorithm: String = "AES/ECB/PKCS5Padding"): ByteArray? {
    try {
        val cipher = Cipher.getInstance(algorithm)
        val sks = SecretKeySpec(sk.toByteArray(), "AES")
        if (iv != null) {
            val ivs = IvParameterSpec(iv.toByteArray())
            cipher.init(Cipher.DECRYPT_MODE, sks, ivs)
        } else {
            cipher.init(Cipher.DECRYPT_MODE, sks)
        }
        return cipher.doFinal(this)
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return null
}
fun ByteArray.aesToString(sk: String = "walfud", iv: String? = null, algorithm: String = "AES/ECB/PKCS5Padding"): String? = aes(sk, iv, algorithm)?.let {
    String(it)
}
fun ByteArray.hex(upperOrLowerCase: Boolean = false): String = joinToString(separator = "") {
    val i = it.toInt() and 0xff
    val s = Integer.toHexString(i)
    if (s.length == 1) "0$s" else s
}
    .let {
        if (upperOrLowerCase) {
            it.uppercase(Locale.getDefault())
        } else {
            it.lowercase(Locale.getDefault())
        }
    }