package com.walfud.extention

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

private const val DATE_PATTERN = "yyyy-MM-dd"
private const val TIME_PATTERN_24 = "HH:mm:ss"
private const val DATE_TIME_PATTERN_24 = "$DATE_PATTERN $TIME_PATTERN_24"

/**
 * 2022-11-05
 */
fun LocalDate.toSimpleString(): String = DateTimeFormatter.ofPattern(DATE_PATTERN).format(this).toString()

/**
 * 00:30:25
 */
fun LocalTime.toSimpleString(): String = DateTimeFormatter.ofPattern(TIME_PATTERN_24).format(this).toString()

/***
 * 2022-11-05 00:30:25
 */
fun LocalDateTime.toSimpleString(): String = DateTimeFormatter.ofPattern(DATE_TIME_PATTERN_24).format(this).toString()

fun parseLocalDateTimeFromSimpleFormat(dateTimeStr: String): LocalDateTime = LocalDateTime.parse(dateTimeStr.replace(' ', 'T'))