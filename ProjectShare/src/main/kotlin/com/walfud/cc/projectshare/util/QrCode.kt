package com.walfud.cc.projectshare.util

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.*

//fun stringToQrcodeFile(text: String, outPng: String): File {
//    val img = stringToQrcodeImage(text)
//    FileOutputStream(outPng).use {
//        ImageIO.write(img, "png", it)
//    }
//
//    return File(outPng)
//}

fun stringToQrcodeImage(text: String, imgSize: Int = 300): IntArray {
    // qrcode
    val hints = Hashtable<EncodeHintType, Any>()
    hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
    hints[EncodeHintType.CHARACTER_SET] = Charsets.UTF_8.name()
    hints[EncodeHintType.MARGIN] = 0
    val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, imgSize, imgSize, hints)

    // to image
    val width = bitMatrix.width
    val height = bitMatrix.height
    val image = IntArray(width * height)
    for (y in 0 until height) {
        for (x in 0 until width) {
            val color = (if (bitMatrix.get(x, y)) 0xFF000000 else 0xFFFFFFFF).toInt()
            image[y * width + x] = color
        }
    }

    return image
}