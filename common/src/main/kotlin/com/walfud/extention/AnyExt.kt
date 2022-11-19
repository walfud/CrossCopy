package com.walfud.extention

fun Any.toMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    this::class.java.declaredFields.forEach {
        val value = it.get(this)
        map[it.name] = when (it.type) {
            Iterable::class.java -> value?.toMap()
            else -> value
        }
    }
    return map
}