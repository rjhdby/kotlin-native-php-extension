package zend.api.mapper

import kotlinx.cinterop.*

fun getIniString(name: String): String = iniMapping[name]?.invoke()?.toKString() ?: ""