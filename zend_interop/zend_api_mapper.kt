package zend.api.proxy

import kotlinx.cinterop.*

fun getIniString(name: String): String = iniMapping[name]?.invoke()?.toKString() ?: ""