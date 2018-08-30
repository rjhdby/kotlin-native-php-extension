package php.extension.share

fun String.fill(vararg map: Pair<String, String?>): String {
    var out = this
    map.filterNot { it.second == null }.forEach { out = out.replace("{${it.first}}", it.second ?: "{${it.first}}") }
    return out
}

fun <T> List<T>.joinIndent(indent: Int = 0, transform: (T) -> CharSequence = { it.toString() }) =
        joinToString(
                separator = "\n" + "    ".repeat(indent),
                transform = transform
        )